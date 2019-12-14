package hu.hexadecimal.quantum;

import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;
import java.util.Random;

import hu.hexadecimal.quantum.graphics.QuantumView;

public class ExperimentRunner {
    private final LinkedList<VisualOperator> v;
    private int status;
    private boolean finished = false;

    public ExperimentRunner(LinkedList<VisualOperator> visualOperators) {
        v = visualOperators;
    }

    public float[] runExperiment(int shots, int threads, Handler handler, boolean probabilityMode) {
        if (threads < 1 || threads > 1000) {
            threads = 4;
        }
        if ((((shots == 0 && (probabilityMode = true))) || shots == 1 || probabilityMode) && threads > 1)
            threads = shots = 1;
        int timestorun = shots / threads == 0 ? 1 : shots / threads;
        Thread[] t = new Thread[threads];
        final int shots2 = shots;
        status = 0;
        finished = false;
        int[][] sprobs = new int[(int) Math.pow(2, QuantumView.MAX_QUBITS)][threads];
        if (!probabilityMode) {
            new Thread(() -> {
                while (status < shots2 && !finished) {
                    try {
                        Thread.sleep(500);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(status);
                }
            }).start();
            for (int i = 0; i < threads; i++) {
                final int t_id = i;
                t[i] = new Thread(() -> {
                    Qubit[] qubits = new Qubit[QuantumView.MAX_QUBITS];
                    try {
                        //To prevent each thread from using the same random numbers
                        Thread.sleep(0, new Random().nextInt(1000) * t_id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    VisualOperator vm = new VisualOperator();
                    for (int k = 0; k < qubits.length; k++) {
                        qubits[k] = new Qubit();
                    }
                    Complex[] quArray = VisualOperator.toQubitArray(qubits);
                    for (int m = 0; m < v.size(); m++) {
                        quArray = v.get(m).operateOn(quArray, qubits.length);
                    }
                    for (int j = 0; j < timestorun; j++) {
                        qubits = vm.measure(quArray, qubits.length);
                        int cprob = 0;
                        for (int k = 0; k < qubits.length; k++) {
                            cprob += qubits[k].dirtyStateCheck() ? 1 << (qubits.length - k - 1) : 0;
                        }
                        sprobs[cprob][t_id]++;
                        if ((j + 1) % 10 == 0) status += 10;
                    }
                });
                t[i].start();
            }
            for (int i = 0; i < threads; i++) {
                try {
                    t[i].join();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("ExperimentRunner", "Unknown error");
                }
            }
        } else {
            Qubit[] qubits = new Qubit[QuantumView.MAX_QUBITS];
            try {
                Thread.sleep(0, new Random().nextInt(1000));
            } catch (Exception e) {
                e.printStackTrace();
            }
            for (int k = 0; k < qubits.length; k++) {
                qubits[k] = new Qubit();
            }
            Complex[] quArray = VisualOperator.toQubitArray(qubits);
            for (int m = 0; m < v.size(); m++) {
                quArray = v.get(m).operateOn(quArray, qubits.length);
            }
            float[] nprobs = new float[sprobs.length];
            float[] probs = VisualOperator.measureProbabilities(quArray);
            for (int m = 0; m < probs.length; m++) {
                int[] x = new int[qubits.length];
                for (int i = 0; i < qubits.length; i++) {
                    x[i] = ((m) >> (qubits.length - i - 1)) % 2;
                }
                int ret = 0;
                for (int i = 0; i < qubits.length; i++) {
                    ret += x[i] << (i);
                }
                nprobs[m] = probs[ret];
            }
            finished = true;
            return nprobs;
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

    public Complex[] getStateVector() {
        Qubit[] qubits = new Qubit[QuantumView.MAX_QUBITS];
        for (int k = 0; k < qubits.length; k++) {
            qubits[k] = new Qubit();
        }
        Complex[] quArray = VisualOperator.toQubitArray(qubits);
        for (int m = 0; m < v.size(); m++) {
            quArray = v.get(m).operateOn(quArray, qubits.length);
        }
        Complex[] orderedQuArray = new Complex[quArray.length];
        for (int m = 0; m < quArray.length; m++) {
            int[] x = new int[qubits.length];
            for (int i = 0; i < qubits.length; i++) {
                x[i] = ((m) >> (qubits.length - i - 1)) % 2;
            }
            int ret = 0;
            for (int i = 0; i < qubits.length; i++) {
                ret += x[i] << (i);
            }
            orderedQuArray[m] = quArray[ret];
        }
        return orderedQuArray;
    }
}
