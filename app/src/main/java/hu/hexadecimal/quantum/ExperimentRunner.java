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
        if (threads < 1 || threads > 1000) {
            threads = 4;
        }
        if (shots == 1 && threads > 1)
            threads = 1;
        int timestorun = shots / threads == 0 ? 1 : shots / threads;
        Thread[] t = new Thread[threads];
        status = 0;
        finished = false;
        int[][] sprobs = new int[(int) Math.pow(2, QuantumView.MAX_QUBITS)][threads];
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
            final int t_id = i;
            t[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    Qubit[] qubits = new Qubit[QuantumView.MAX_QUBITS];
                    VisualOperator vm = VisualOperator.CNOT.copy();
                    for (int j = 0; j < timestorun; j++) {
                        for (int k = 0; k < qubits.length; k++) {
                            qubits[k] = new Qubit();
                        }
                        Complex[] quArray = VisualOperator.toQubitArray(qubits);
                        for (int m = 0; m < v.size(); m++) {
                            quArray = v.get(m).operateOn(quArray, qubits.length);
                            /*for (int k = 0; k < quArray.length; k++) {
                                Log.w("X", quArray[k].toString3Decimals() + " - " + Integer.toBinaryString(k));
                            }*/
                        }
                        qubits = vm.measure(quArray, qubits.length);
                        int cprob = 0;
                        for (int k = 0; k < qubits.length; k++) {
                            cprob += qubits[k].measureZ() ? 1 << (qubits.length - k - 1) : 0;
                        }
                        sprobs[cprob][t_id]++;
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
            for (int j = 1; j < threads; j++) {
                sprobs[o][0] += sprobs[o][j];
            }
            nprobs[o] = sprobs[o][0] / (float) shots;
            if (shots == 1) {
                nprobs[o] /= threads;
            }
        }
        finished = true;
        return nprobs;
    }
}
