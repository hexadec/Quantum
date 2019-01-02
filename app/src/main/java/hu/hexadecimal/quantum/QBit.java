package hu.hexadecimal.quantum;

import android.util.Log;

import java.util.Random;

public class QBit {

    protected Complex[] matrix;
    private Random random;

    QBit() {
        prepare(false);
        random = new Random();
    }

    QBit(Complex[] complex) {
        if (complex != null && complex.length == 2) {
            matrix = complex;
            random = new Random();
        } else {
            throw new IllegalArgumentException();
        }
    }

    public void prepare(boolean value) {
        matrix = new Complex[] {new Complex(value ? 0 : 1), new Complex(value ? 1 : 0)};
    }

    public void zeroVector() {
        matrix = new Complex[] {new Complex(0), new Complex(0)};
    }

    public boolean measureZ() {
        double prob0 = Complex.multiply(Complex.conjugate(matrix[0]), matrix[0]).real;
        double prob1 = Complex.multiply(Complex.conjugate(matrix[1]), matrix[1]).real;
        if (prob0 + prob1 < 0.999) {
            Log.e("QBIT Error", "Too low probability sum:\t" + (prob0 + prob1));
        }
        boolean value = random.nextDouble() > prob0;
        prepare(value);
        return value;
    }

    public void applyOperator(LinearOperator linearOperator) {
        matrix = linearOperator.operateOn(this).copy().matrix;
    }

    public String toString() {
        return matrix[0].toString3Decimals() + "\n" + matrix[1].toString3Decimals();
    }

    public String toStringModArg() {
        return matrix[0].toStringModArg() + "\n" + matrix[1].toStringModArg();
    }

    public QBit copy() {
        Complex[] matrix2 = new Complex[2];
        matrix2[0] = matrix[0];
        matrix2[1] = matrix[1];
        return new QBit(matrix2);
    }
}
