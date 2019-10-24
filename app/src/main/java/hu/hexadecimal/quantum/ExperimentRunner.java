package hu.hexadecimal.quantum;

import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;

import hu.hexadecimal.quantum.graphics.QuantumView;

public class ExperimentRunner {
    private final LinkedList<VisualOperator> v;
    private int status;
    boolean finished = false;

    public ExperimentRunner(LinkedList<VisualOperator> visualOperators) {
        v = visualOperators;
    }

    public float[] runExperiment(int shots, int threads, Handler handler) {
        int timestorun = shots / threads == 0 ? 1 : shots / threads;
        Thread[] t = new Thread[threads];
        status = 0;
        finished = false;
        int[] sprobs = new int[(int) Math.pow(2, QuantumView.MAX_QUBITS)];
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (status < shots && !finished) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(status);
                }
            }
        }).start();
        for (int i = 0; i < threads; i++) {
            t[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    Qubit[] qubits = new Qubit[QuantumView.MAX_QUBITS];
                    for (int j = 0; j < timestorun; j++) {
                        for (int k = 0; k < qubits.length; k++) {
                            qubits[k] = new Qubit();
                        }
                        for (int m = 0; m < v.size(); m++) {
                            if (v.get(m) instanceof LinearOperator) {
                                qubits[v.get(m).getQubitIDs()[0]].applyOperator((LinearOperator) v.get(m));
                            } else {
                                int[] quids = v.get(m).getQubitIDs();
                                Qubit[] subqubits = new Qubit[quids.length];
                                for (int n = 0; n < subqubits.length; n++) {
                                    subqubits[n] = qubits[quids[n]];
                                }
                                Qubit[] resultqubits = ((MultiQubitOperator) v.get(m)).operateOn(subqubits);
                                for (int n = 0; n < subqubits.length; n++) {
                                    qubits[quids[n]] = resultqubits[n];
                                }
                            }
                        }
                        int cprob = 0;
                        for (int k = 0; k < qubits.length; k++) {
                            cprob += qubits[k].measureZ() ? 1 << k : 0;
                        }
                        sprobs[cprob]++;
                        status++;
                    }
                }
            });
            t[i].start();
        }
        Log.i("ExperimentRunner", "threads: " + t.length);
        for (int i = 0; i < threads; i++) {
            try {
                t[i].join();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("ExperimentRunner", "Unknown error");
            }
        }
        float[] nprobs = new float[sprobs.length];
        for (int o = 0; o < sprobs.length; o++) {
            nprobs[o] = sprobs[o] / (float) shots;
            if (shots == 1) {
                nprobs[o] /= threads;
            }
        }
        finished = true;
        return nprobs;
    }
}
