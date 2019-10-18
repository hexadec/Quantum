package hu.hexadecimal.quantum;

import android.util.Log;

import java.util.LinkedList;

import hu.hexadecimal.quantum.graphics.QuantumView;

public class ExperimentRunner {
    private final LinkedList<VisualOperator> v;

    public ExperimentRunner(LinkedList<VisualOperator> visualOperators) {
        v = visualOperators;
    }

    public float[] runExperiment(int shots, int threads) {
        int timestorun = shots / threads == 0 ? 1 : shots / threads;
        Thread[] t = new Thread[threads];
        int[] sprobs = new int[(int) Math.pow(2, QuantumView.MAX_QUBITS)];
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
                                    //Log.e("-", n + "-" + quids[n] + "-" + subqubits[n].toString());
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
                    }
                }
            });
            t[i].run();
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
        return nprobs;
    }
}
