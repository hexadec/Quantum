package hu.hexadecimal.quantum;

import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;
import java.util.Random;

import hu.hexadecimal.quantum.graphics.QuantumView;

public class ExperimentRunner {
    private final LinkedList<VisualOperator> v;
    private final QuantumView quantumView;
    private final int MAX_QUBIT;
    private int status;
    private boolean finished = false;

    private Complex[] quArray;

    public ExperimentRunner(QuantumView quantumView) {
        v = quantumView.getOperators();
        int lUsed = quantumView.getLastUsedQubit();
        MAX_QUBIT = lUsed < 2 ? 2 : lUsed + 1;
        this.quantumView = quantumView;
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
        int[][] sprobs = new int[1 << MAX_QUBIT][threads];
        if (!probabilityMode) {
            Thread t0 = new Thread(() -> {
                Qubit[] qubits = new Qubit[MAX_QUBIT];
                for (int k = 0; k < MAX_QUBIT; k++) {
                    qubits[k] = new Qubit();
                }
                quArray = VisualOperator.toQubitArray(qubits);
                for (int m = 0; m < v.size(); m++) {
                    quArray = v.get(m).operateOn(quArray, MAX_QUBIT);
                }
            });
            t0.start();
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
                    try {
                        //To prevent each thread from using the same random numbers
                        Thread.sleep(0, new Random().nextInt(1000) * t_id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        t0.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    VisualOperator vm = new VisualOperator();
                    float[] probs = VisualOperator.measureProbabilities(quArray);
                    for (int j = 0; j < timestorun; j++) {
                        int cprob = vm.measureFromProbabilities(probs);
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
            Qubit[] qubits = new Qubit[MAX_QUBIT];
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
        float[] ordered_probs = new float[sprobs.length];
        for (int o = 0; o < sprobs.length; o++) {
            int correct_position = Integer.reverse(o << (32-MAX_QUBIT)) & 0xFF;
            for (int j = 0; j < threads; j++) {
                ordered_probs[correct_position] += sprobs[o][j];
            }
            ordered_probs[correct_position] /= (float) shots;
            if (shots == 1) {
                ordered_probs[correct_position] /= threads;
            }
        }
        finished = true;
        return ordered_probs;
    }

    public Complex[] getStateVector() {
        return getStateVector(-1);
    }

    private Complex[] getStateVector(int which) {
        Qubit[] qubits = new Qubit[MAX_QUBIT];
        for (int k = 0; k < qubits.length; k++) {
            qubits[k] = new Qubit();
        }
        Complex[] quArray = VisualOperator.toQubitArray(qubits);
        if (which >= 0 && which < quArray.length) {
            quArray[which] = new Complex(1);
        }
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

    public Complex[][] getFinalUnitaryMatrix() {
        final int DIM = 1 << MAX_QUBIT;
        Complex[][] matrix = new Complex[DIM][DIM];
        for (int i = 0; i < DIM; i++) {
            Complex[] state = getStateVector(i);
            for (int j = 0; j < DIM; j++) {
                matrix[j][i] = state[j];
                Log.w(i + "-" + j, state[j].toString(2));
            }
        }
        return matrix;
    }
}
