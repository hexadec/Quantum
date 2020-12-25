package hu.hexadecimal.quantum.tools;

import android.os.Handler;
import android.util.Log;

import java.util.LinkedList;
import java.util.Random;

import hu.hexadecimal.quantum.graphics.QuantumView;
import hu.hexadecimal.quantum.math.Complex;
import hu.hexadecimal.quantum.math.Qubit;
import hu.hexadecimal.quantum.math.VisualOperator;

/**
 * Class responsible for running the circuits
 */
public class ExperimentRunner {
    private final LinkedList<VisualOperator> v;
    private final QuantumView quantumView;
    private final int MAX_QUBIT;
    public int status;
    private int opStatus;
    private boolean finished = false;

    private Complex[] quArray;

    public ExperimentRunner(QuantumView quantumView) {
        v = (LinkedList<VisualOperator>) quantumView.getOperators().clone();
        int lUsed = quantumView.getLastUsedQubit();
        //TODO check bug source (tensor!!)
        MAX_QUBIT = lUsed < 7 ? 7 : lUsed + 1;
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
        final int increment;
        if (shots < 10000) {
            increment = 10;
        } else if (shots < 100000) {
            increment = 100;
        } else {
            increment = 1000;
        }
        if (!probabilityMode) {
            Thread t0 = new Thread(() -> {
                if (shouldOptimizeFurther()) {
                    optimizeFurther();
                }
                quArray = getStateVector();
            });
            new Thread(() -> {
                while (status < shots2 && !finished && !quantumView.shouldStop) {
                    try {
                        Thread.sleep(250);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(status == 0 ? opStatus : status);
                }
            }).start();
            t0.start();
            for (int i = 0; i < threads; i++) {
                final int t_id = i;
                t[i] = new Thread(() -> {
                    try {
                        //To prevent each thread from using the same random numbers
                        Thread.sleep(0, new Random().nextInt(10000) * t_id);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    try {
                        while (t0.isAlive()) {
                            t0.join(500);
                            if (quantumView.shouldStop) {
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    VisualOperator vm = new VisualOperator();
                    float[] probs = VisualOperator.measureProbabilities(quArray);
                    for (int j = 0; j < timestorun; j++) {
                        int cprob = vm.measureFromProbabilities(probs);
                        sprobs[cprob][t_id]++;
                        if ((j + 1) % increment == 0) {
                            status += increment;
                            if (quantumView.shouldStop) {
                                return;
                            }
                        }
                    }
                });
                t[i].start();
            }
            for (int i = 0; i < threads; i++) {
                if (quantumView.shouldStop)
                    return null;
                try {
                    t[i].join();
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.e("ExperimentRunner", "Unknown error");
                }
            }
        } else {
            new Thread(() -> {
                while (!finished && !quantumView.shouldStop) {
                    try {
                        Thread.sleep(250);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    handler.sendEmptyMessage(opStatus);
                }
            }).start();
            if (shouldOptimizeFurther()) {
                optimizeFurther();
            }
            quArray = getStateVector();
            if (quantumView.shouldStop)
                return null;
            float[] probs = VisualOperator.measureProbabilities(quArray);
            finished = true;
            return probs;
        }
        float[] ordered_probs = new float[sprobs.length];
        for (int o = 0; o < sprobs.length; o++) {
            for (int j = 0; j < threads; j++) {
                ordered_probs[o] += sprobs[o][j];
            }
            ordered_probs[o] /= (float) shots;
            if (shots == 1) {
                ordered_probs[o] /= threads;
            }
        }
        finished = true;
        return ordered_probs;
    }

    public Complex[] getStateVector() {
        if (finished && quArray != null) return quArray;
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
        try {
            for (int m = 0; m < v.size(); m++) {
                quArray = v.get(m).operateOn(quArray, qubits.length);
                if (quantumView.shouldStop) {
                    return null;
                }
                opStatus = m;
            }
        } catch (Exception e) {
            Log.e("Quantum Exp Runner", "Unknown error while calculating state-vector");
            e.printStackTrace();
            return VisualOperator.toQubitArray(qubits);
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

    public boolean shouldOptimizeFurther() {
        int opLength = v.size();
        int qFactor = 1 << MAX_QUBIT;
        return opLength * qFactor >= 4096;
    }

    public void optimizeFurther() {
        for (int i = 0; i < MAX_QUBIT; i++) {
            VisualOperator last = null;
            for (int j = 0; j < v.size(); j++) {
                VisualOperator jThElement = v.get(j);
                if (jThElement.isMultiQubit()) {
                    for (int k = 0; k < jThElement.getQubitIDs().length; k++) {
                        if (jThElement.getQubitIDs()[k] == i) {
                            if (last != null) {
                                v.add(j, last.copy());
                                last = null;
                                j++;
                            }
                            break;
                        }
                    }
                } else if (jThElement.getQubitIDs()[0] == i) {
                    if (last == null) {
                        last = jThElement.copy();
                    } else {
                        last = jThElement.copy().matrixMultiplication(last);
                    }
                    v.remove(j);
                    j--;
                }
                if (j == v.size() - 1 && last != null) {
                    v.add(j, last.copy());
                    last = null;
                    j++;
                }
            }
        }
    }
}
