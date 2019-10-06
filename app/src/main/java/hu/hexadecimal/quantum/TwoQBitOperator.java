package hu.hexadecimal.quantum;

import android.util.Log;

import java.util.Random;

public class TwoQBitOperator extends GeneralOperator {

    private static final int MATRIX_DIM = 4;
    protected Complex[][] matrix;
    private String name;
    private String symbol;
    private String symbol2;
    private Random random;

    public static final TwoQBitOperator CNOT =
            new TwoQBitOperator(
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)}
                    }, "CNOT", "●", "○");

    public static final TwoQBitOperator SWAP =
            new TwoQBitOperator(
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1)}
                    }, "SWAP", "✖", "✖");

    public TwoQBitOperator(Complex[][] M, String name, String symbol, String symbol2) {
        if (M == null) {
            throw new NullPointerException();
        }
        for (int i = 0; i < MATRIX_DIM; i++) {
            if (!(i < M.length && M[i].length == MATRIX_DIM)) {
                throw new NullPointerException("Invalid array");
            }
        }
        this.name = name;
        this.symbol = symbol;
        this.symbol2 = symbol2;
        matrix = M;

        random = new Random();
    }

    public TwoQBitOperator(Complex[][] M) {
        this(M, "Custom", "C1", "C2");
        random = new Random();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void conjugate() {
        for (Complex[] ca : matrix) {
            for (Complex z : ca) {
                z.conjugate();
            }
        }
    }

    public static TwoQBitOperator conjugate(TwoQBitOperator twoQBitOperator) {
        TwoQBitOperator l = twoQBitOperator.copy();
        for (Complex[] ca : l.matrix) {
            for (Complex z : ca) {
                z.conjugate();
            }
        }
        return l;
    }

    public void transpose() {
        Complex[][] tmp = new Complex[MATRIX_DIM][MATRIX_DIM];
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                tmp[i][j] = matrix[j][i];
            }
        }
        matrix = tmp;
    }

    public static TwoQBitOperator transpose(TwoQBitOperator twoQBitOperator) {
        TwoQBitOperator t = twoQBitOperator.copy();
        t.transpose();
        return t;
    }

    public void hermitianConjugate() {
        transpose();
        conjugate();
    }

    public static TwoQBitOperator hermitianConjugate(TwoQBitOperator twoQBitOperator) {
        TwoQBitOperator t = twoQBitOperator.copy();
        t.hermitianConjugate();
        return t;
    }

    public boolean isHermitian() {
        TwoQBitOperator t = copy();
        t.hermitianConjugate();
        return equals(t);
    }

    public void multiply(Complex complex) {
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                matrix[i][j].multiply(complex);
            }
        }
    }

    public static TwoQBitOperator multiply(TwoQBitOperator t, Complex complex) {
        TwoQBitOperator twoQBitOperator = t.copy();
        twoQBitOperator.multiply(complex);
        return twoQBitOperator;
    }

    public TwoQBitOperator copy() {
        Complex[][] complex = new Complex[][]{
                new Complex[MATRIX_DIM],
                new Complex[MATRIX_DIM],
                new Complex[MATRIX_DIM],
                new Complex[MATRIX_DIM]
        };
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                matrix[i][j] = complex[i][j];
            }
        }
        return new TwoQBitOperator(complex, name, symbol, symbol2);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Complex[] c : matrix) {
            for (Complex z : c) {
                sb.append(z.toString3Decimals());
                sb.append('\t');
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public boolean equals(TwoQBitOperator twoQBitOperator) {
        Complex[][] m = twoQBitOperator.matrix;
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                if (!matrix[i][j].equalsExact(twoQBitOperator.matrix[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    public QBit[] operateOn(final QBit first, final QBit second) {
        QBit q = new QBit();
        q.zeroVector();
        Complex[] inputMatrix = new Complex[MATRIX_DIM];
        Complex[] resultMatrix = new Complex[]{
                new Complex(0), new Complex(0),
                new Complex(0), new Complex(0)};
        for (int i = 0; i < MATRIX_DIM; i++) {
            inputMatrix[i] = Complex.multiply(first.matrix[i / 2], second.matrix[i % 2]);
        }
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                resultMatrix[i].add(Complex.multiply(matrix[i][j], inputMatrix[j]));
            }
        }
        Log.i("TwoQBitOperator", resultMatrix[0] + "\n" + resultMatrix[1] + "\n" + resultMatrix[2] + "\n" + resultMatrix[3]);
        double prob00 = Complex.multiply(Complex.conjugate(inputMatrix[0]), inputMatrix[0]).real;
        double prob01 = Complex.multiply(Complex.conjugate(inputMatrix[1]), inputMatrix[1]).real;
        double prob10 = Complex.multiply(Complex.conjugate(inputMatrix[2]), inputMatrix[2]).real;
        double prob11 = Complex.multiply(Complex.conjugate(inputMatrix[3]), inputMatrix[3]).real;
        if (prob00 + prob01 > random.nextDouble()) {
            QBit result0 = new QBit();
            QBit result1 = new QBit();
            if (prob01 > random.nextDouble()) {
                result1.prepare(true);
            }
            return new QBit[]{result0, result1};
        } else {
            QBit result0 = new QBit();
            QBit result1 = new QBit();
            result0.prepare(true);
            if (prob11 < random.nextDouble()) {
                result1.prepare(true);
            }
            return new QBit[]{result0, result1};
        }
    }
}
