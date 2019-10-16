package hu.hexadecimal.quantum;

import android.util.Log;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MultiQubitOperator extends VisualOperator {

    public final int NQBITS;
    protected Complex[][] matrix;
    private String name;
    private String[] symbols;
    private Random random;

    public static final MultiQubitOperator CNOT =
            new MultiQubitOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)}
                    }, "CNOT", new String[]{"●", "○"}, 0xffE19417);

    public static final MultiQubitOperator SWAP =
            new MultiQubitOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1)}
                    }, "SWAP", new String[]{"✖", "✖"}, 0xffE19417);

    public static final MultiQubitOperator TOFFOLI =
            new MultiQubitOperator(8,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0)}
                    }, "Toffoli", new String[]{"●", "●", "○"}, 0xff17DCE1);

    public static final MultiQubitOperator FREDKIN =
            new MultiQubitOperator(8,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1)}
                    }, "Toffoli", new String[]{"●", "✖", "✖"}, 0xff17DCE1);

    public MultiQubitOperator(int MATRIX_DIM, Complex[][] M, String name, String[] symbols, int color) {
        super(MATRIX_DIM);
        if (M == null) {
            throw new NullPointerException();
        }
        this.color = color;
        switch (MATRIX_DIM) {
            case 4:
                NQBITS = 2;
                break;
            case 8:
                NQBITS = 3;
                break;
            case 16:
                NQBITS = 4;
                break;
            default:
                throw new NullPointerException("Invalid dimension");
        }
        for (int i = 0; i < MATRIX_DIM; i++) {
            if (!(i < M.length && M[i].length == MATRIX_DIM)) {
                throw new NullPointerException("Invalid array");
            }
        }
        if (symbols.length != NQBITS) {
            throw new NullPointerException("Invalid symbol");
        }
        this.name = name;
        this.symbols = symbols.clone();
        matrix = M;

        random = new Random();
    }

    public MultiQubitOperator(int MATRIX_DIM, Complex[][] M) {
        this(MATRIX_DIM, M, "Custom", MultiQubitOperator.generateSymbols(MATRIX_DIM), 0xff000000);
        random = new Random();
    }

    private MultiQubitOperator() {
        super(4);
        name = "";
        NQBITS = 2;
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

    public static MultiQubitOperator conjugate(MultiQubitOperator multiQubitOperator) {
        MultiQubitOperator l = multiQubitOperator.copy();
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

    public static MultiQubitOperator transpose(MultiQubitOperator multiQubitOperator) {
        MultiQubitOperator t = multiQubitOperator.copy();
        t.transpose();
        return t;
    }

    public void hermitianConjugate() {
        transpose();
        conjugate();
    }

    public static MultiQubitOperator hermitianConjugate(MultiQubitOperator multiQubitOperator) {
        MultiQubitOperator t = multiQubitOperator.copy();
        t.hermitianConjugate();
        return t;
    }

    public boolean isHermitian() {
        MultiQubitOperator t = copy();
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

    public static MultiQubitOperator multiply(MultiQubitOperator t, Complex complex) {
        MultiQubitOperator multiQubitOperator = t.copy();
        multiQubitOperator.multiply(complex);
        return multiQubitOperator;
    }

    public MultiQubitOperator copy() {
        Complex[][] complex = new Complex[MATRIX_DIM][MATRIX_DIM];
        for (int i = 0; i < MATRIX_DIM; i++) {
            complex[i] = new Complex[MATRIX_DIM];
            for (int j = 0; j < MATRIX_DIM; j++) {
                matrix[i][j] = complex[i][j];
            }
        }
        return new MultiQubitOperator(MATRIX_DIM, complex, name, symbols, color);
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

    public boolean equals(MultiQubitOperator multiQubitOperator) {
        Complex[][] m = multiQubitOperator.matrix;
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                if (!matrix[i][j].equalsExact(multiQubitOperator.matrix[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    public Qubit[] operateOn(final Qubit[] qs) {
        if (qs.length != NQBITS) {
            Log.e("MultiQubitOperator", "NO RESULT");
            return null;
        }
        Complex[] inputMatrix = new Complex[MATRIX_DIM];
        Complex[] resultMatrix = new Complex[MATRIX_DIM];
        for (int i = 0; i < MATRIX_DIM; i++) {
            resultMatrix[i] = new Complex(0);
            for (int k = 0; k < NQBITS; k++) {
                if (k == 0) {
                    inputMatrix[i] = Complex.multiply(qs[NQBITS - 2].matrix[(i >> 1) % 2], qs[NQBITS - 1].matrix[i % 2]);
                    k += 2;
                    continue;
                }
                inputMatrix[i] = Complex.multiply(inputMatrix[i], qs[NQBITS - k - 1].matrix[(i >> k) % 2]);
            }
        }
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                resultMatrix[i].add(Complex.multiply(matrix[i][j], inputMatrix[j]));
            }
        }
        //Log.i("MultiQubitOperator", resultMatrix[0] + "\n" + resultMatrix[1] + "\n" + resultMatrix[2] + "\n" + resultMatrix[3]);
        double[] probs = new double[MATRIX_DIM];
        double subtrahend = 0;
        for (int i = 0; i < MATRIX_DIM; i++) {
            probs[i] = Complex.multiply(Complex.conjugate(resultMatrix[i]), resultMatrix[i]).real;
            //Log.i("MultiQubitOperator", "Probs[" + i + "]:" + probs[i]);
            double prob = random.nextDouble();
            if (probs[i] > (prob - subtrahend < 0 ? 0 : prob - subtrahend)) {
                //Log.i("MultiQubitOperator", "CASE: " + i);
                Qubit[] result = new Qubit[NQBITS];
                for (int j = 0; j < NQBITS; j++) {
                    result[j] = new Qubit();
                    if ((i >> j) == 1) result[j].prepare(true);
                }
                return result;
            } else {
                subtrahend += probs[i];
                if (i == MATRIX_DIM - 2) {
                    subtrahend += 1;
                }
            }
        }
        Log.e("MultiQubitOperator", "NO RESULT");
        return null;
    }

    public static String[] generateSymbols(int DIM) {
        String[] sym = new String[DIM];
        for (int i = 0; i < DIM; i++)
            sym[i] = "C" + i;
        return sym;
    }

    public static List<String> getPredefinedGateNames() {
        List<String> list = new ArrayList<>();
        MultiQubitOperator multiQubitOperator = new MultiQubitOperator();
        try {
            Field[] fields = multiQubitOperator.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.get(multiQubitOperator) instanceof MultiQubitOperator) {
                    list.add(((MultiQubitOperator) field.get(multiQubitOperator)).getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    public static MultiQubitOperator findGateByName(String name) {
        MultiQubitOperator multiQubitOperator = new MultiQubitOperator();
        try {
            Field[] fields = multiQubitOperator.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.get(multiQubitOperator) instanceof MultiQubitOperator) {
                    if (((MultiQubitOperator) field.get(multiQubitOperator)).getName().equals(name)) {
                        return ((MultiQubitOperator) field.get(multiQubitOperator));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }
}
