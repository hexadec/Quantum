package hu.hexadecimal.quantum;

import android.util.Log;

import java.util.Random;

public class MultiqBitOperator extends VisualOperator {

    private int MATRIX_DIM = 4;
    protected Complex[][] matrix;
    private String name;
    private String[] symbols;
    private Random random;

    public static final MultiqBitOperator CNOT =
            new MultiqBitOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)}
                    }, "CNOT", new String[]{"●", "○"});

    public static final MultiqBitOperator SWAP =
            new MultiqBitOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1)}
                    }, "SWAP", new String[]{"✖", "✖"});

    public MultiqBitOperator(int MATRIX_DIM, Complex[][] M, String name, String[] symbols) {
        if (M == null) {
            throw new NullPointerException();
        }
        this.MATRIX_DIM = MATRIX_DIM;
        if (MATRIX_DIM != 4 && MATRIX_DIM != 8 && MATRIX_DIM != 16) {
            throw new NullPointerException("Invalid dimension");
        }
        for (int i = 0; i < MATRIX_DIM; i++) {
            if (!(i < M.length && M[i].length == MATRIX_DIM)) {
                throw new NullPointerException("Invalid array");
            }
        }
        if (!((symbols.length == 2 && MATRIX_DIM == 4) ||
                (symbols.length == 3 && MATRIX_DIM == 8) ||
                (symbols.length == 4 && MATRIX_DIM == 16))) {
            throw new NullPointerException("Invalid symbol");
        }
        this.name = name;
        this.symbols = symbols.clone();
        matrix = M;

        random = new Random();
    }

    public MultiqBitOperator(int MATRIX_DIM, Complex[][] M) {
        this(MATRIX_DIM, M, "Custom", MultiqBitOperator.generateSymbols(MATRIX_DIM));
        random = new Random();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getSymbols() {
        return symbols;
    }

    public boolean setSymbols(String[] symbols) {
        if (symbols.length == MATRIX_DIM) {
            this.symbols = symbols;
            return true;
        } else {
            return false;
        }
    }

    public void conjugate() {
        for (Complex[] ca : matrix) {
            for (Complex z : ca) {
                z.conjugate();
            }
        }
    }

    public static MultiqBitOperator conjugate(MultiqBitOperator multiqBitOperator) {
        MultiqBitOperator l = multiqBitOperator.copy();
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

    public static MultiqBitOperator transpose(MultiqBitOperator multiqBitOperator) {
        MultiqBitOperator t = multiqBitOperator.copy();
        t.transpose();
        return t;
    }

    public void hermitianConjugate() {
        transpose();
        conjugate();
    }

    public static MultiqBitOperator hermitianConjugate(MultiqBitOperator multiqBitOperator) {
        MultiqBitOperator t = multiqBitOperator.copy();
        t.hermitianConjugate();
        return t;
    }

    public boolean isHermitian() {
        MultiqBitOperator t = copy();
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

    public static MultiqBitOperator multiply(MultiqBitOperator t, Complex complex) {
        MultiqBitOperator multiqBitOperator = t.copy();
        multiqBitOperator.multiply(complex);
        return multiqBitOperator;
    }

    public MultiqBitOperator copy() {
        Complex[][] complex = new Complex[MATRIX_DIM][MATRIX_DIM];
        for (int i = 0; i < MATRIX_DIM; i++) {
            complex[i] = new Complex[MATRIX_DIM];
            for (int j = 0; j < MATRIX_DIM; j++) {
                matrix[i][j] = complex[i][j];
            }
        }
        return new MultiqBitOperator(MATRIX_DIM, complex, name, symbols);
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

    public boolean equals(MultiqBitOperator multiqBitOperator) {
        Complex[][] m = multiqBitOperator.matrix;
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                if (!matrix[i][j].equalsExact(multiqBitOperator.matrix[i][j])) {
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
        Log.i("MultiqBitOperator", resultMatrix[0] + "\n" + resultMatrix[1] + "\n" + resultMatrix[2] + "\n" + resultMatrix[3]);
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

    private static final String[] generateSymbols(int DIM) {
        String[] sym = new String[DIM];
        for (int i = 0; i < DIM; i++)
            sym[i] = "C" + i;
        return sym;
    }
}
