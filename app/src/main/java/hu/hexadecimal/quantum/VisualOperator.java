package hu.hexadecimal.quantum;

import android.graphics.Rect;
import android.util.Log;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;

public class VisualOperator implements Serializable {

    public static final long serialVersionUID = 2L;
    private Complex[][] matrix;
    private String[] symbols;
    private Random random;

    /**
     * Visual Quantum Gate
     */
    public static final String FILE_EXTENSION = ".vqg";
    private int NQBITS;
    public int color = 0xff000000;
    public String name;
    private transient LinkedList<Rect> rectangle;
    private int MATRIX_DIM;
    private int[] qubit_ids;

    public static final transient VisualOperator CNOT =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)}
                    }, "CNOT", new String[]{"●", "◯"}, 0xffE19417);

    public static final transient VisualOperator CY =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0, -1)},
                            {new Complex(0), new Complex(0), new Complex(0, 1), new Complex(0)}
                    }, "CY", new String[]{"●", "Y"}, 0xffE19417);

    public static final transient VisualOperator CZ =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(-1)}
                    }, "CZ", new String[]{"●", "Z"}, 0xffE19417);

    public static final transient VisualOperator SWAP =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1)}
                    }, "SWAP", new String[]{"✖", "✖"}, 0xffE19417);

    public static final transient VisualOperator CS =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(-Math.PI / 4), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(Math.PI / 4)}
                    }, "Controlled π/2 shift", new String[]{"●", "S"}, 0xff21BAAB);

    public static final transient VisualOperator CT =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(-Math.PI / 8), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(Math.PI / 8)}
                    }, "Controlled π/4 shift", new String[]{"●", "T"}, 0xffBA7021);

    public static final transient VisualOperator CH =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1.0 / Math.sqrt(2), 0), new Complex(1.0 / Math.sqrt(2), 0)},
                            {new Complex(0), new Complex(0), new Complex(1.0 / Math.sqrt(2), 0), new Complex(-1.0 / Math.sqrt(2), 0)}
                    }, "Controlled Hadamard", new String[]{"●", "H"}, 0xff2155BA);

    public static final transient VisualOperator TOFFOLI =
            new VisualOperator(8,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0)}
                    }, "Toffoli", new String[]{"●", "●", "◯"}, 0xff17DCE1);

    public static final transient VisualOperator FREDKIN =
            new VisualOperator(8,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(0), new Complex(1)}
                    }, "Fredkin", new String[]{"●", "✖", "✖"}, 0xff17DCE1);

    public static final transient VisualOperator HADAMARD =
            VisualOperator.multiply(
                    new VisualOperator(2, new Complex[][]{
                            new Complex[]{new Complex(1), new Complex(1)},
                            new Complex[]{new Complex(1), new Complex(-1)}
                    }, "Hadamard", new String[]{"H"}, 0xff2155BA), new Complex(1 / Math.sqrt(2), 0));

    public static final transient VisualOperator PAULI_Z =
            new VisualOperator(2, new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(-1)}
            }, "Pauli-Z", new String[]{"Z"}, 0xff60BA21);

    public static final transient VisualOperator PAULI_Y =
            new VisualOperator(2, new Complex[][]{
                    new Complex[]{new Complex(0), new Complex(0, -1)},
                    new Complex[]{new Complex(0, 1), new Complex(0)}
            }, "Pauli-Y", new String[]{"Y"}, 0xff60BA21);

    public static final transient VisualOperator PAULI_X =
            new VisualOperator(2, new Complex[][]{
                    new Complex[]{new Complex(0), new Complex(1)},
                    new Complex[]{new Complex(1), new Complex(0)}
            }, "Pauli-X", new String[]{"X"}, 0xff60BA21);

    public static final transient VisualOperator T_GATE =
            new VisualOperator(2, new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(Math.PI / 4)}
            }, "π/4 Phase-shift", new String[]{"T"}, 0xffBA7021);

    public static final transient VisualOperator S_GATE =
            new VisualOperator(2, new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(0, 1)}
            }, "π/2 Phase-shift", new String[]{"S"}, 0xff21BAAB);

    public static final transient VisualOperator PI6_GATE =
            new VisualOperator(2, new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(Math.PI / 6)}
            }, "π/6 Phase-shift", new String[]{"π6"}, 0xffDCE117);

    public static final transient VisualOperator SQRT_NOT =
            VisualOperator.multiply(new VisualOperator(2, new Complex[][]{
                    new Complex[]{new Complex(1, 1), new Complex(1, -1)},
                    new Complex[]{new Complex(1, -1), new Complex(1, 1)}
            }, "√NOT", new String[]{"√X"}, 0xff2155BA), new Complex(0.5, 0));

    public static final transient VisualOperator ID =
            new VisualOperator(2, new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(1)}
            }, "Identity", new String[]{"I"}, 0xff666666);

    public VisualOperator(int DIM, Complex[][] M, String name, String[] symbols, int color) {
        rectangle = new LinkedList<>();
        this.MATRIX_DIM = DIM;
        if (M == null) {
            throw new NullPointerException();
        }
        this.color = color;
        switch (DIM) {
            case 2:
                NQBITS = 1;
                break;
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
        for (int i = 0; i < DIM; i++) {
            if (!(i < M.length && M[i].length == DIM)) {
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
        qubit_ids = new int[NQBITS];
    }

    public VisualOperator(int MATRIX_DIM, Complex[][] M) {
        this(MATRIX_DIM, M, "Custom", VisualOperator.generateSymbols(MATRIX_DIM), 0xff000000);
        random = new Random();
    }

    public VisualOperator() {
        random = new Random();
        qubit_ids = new int[NQBITS];
        rectangle = new LinkedList<>();
        MATRIX_DIM = 4;
        name = "";
        NQBITS = 2;
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

    public static VisualOperator conjugate(VisualOperator visualOperator) {
        VisualOperator l = visualOperator.copy();
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

    public static VisualOperator transpose(VisualOperator visualOperator) {
        VisualOperator t = visualOperator.copy();
        t.transpose();
        return t;
    }

    public void hermitianConjugate() {
        transpose();
        conjugate();
        for (int i = 0; i < NQBITS; i++) {
            symbols[i] += "†";
        }
    }

    public static VisualOperator hermitianConjugate(VisualOperator visualOperator) {
        VisualOperator t = visualOperator.copy();
        t.hermitianConjugate();
        return t;
    }

    public boolean isHermitian() {
        VisualOperator t = copy();
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

    public static VisualOperator multiply(VisualOperator t, Complex complex) {
        VisualOperator visualOperator = t.copy();
        visualOperator.multiply(complex);
        return visualOperator;
    }

    public VisualOperator copy() {
        Complex[][] complex = new Complex[MATRIX_DIM][MATRIX_DIM];
        for (int i = 0; i < MATRIX_DIM; i++) {
            complex[i] = new Complex[MATRIX_DIM];
            for (int j = 0; j < MATRIX_DIM; j++) {
                complex[i][j] = matrix[i][j].copy();
            }
        }
        return new VisualOperator(MATRIX_DIM, complex, name, symbols, color);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Complex[] c : matrix) {
            for (Complex z : c) {
                sb.append(z.toString3Decimals());
                sb.append(", ");
            }
            sb.deleteCharAt(sb.length() - 2);
            sb.append('\n');
        }
        return sb.toString();
    }

    public String toStringHtmlTable() {
        return toStringHtmlTable(false);
    }

    public String toStringHtmlTable(boolean caption) {
        String[][] matrixString = new String[MATRIX_DIM][MATRIX_DIM];
        int[] colLength = new int[MATRIX_DIM];
        int sumLen = 0;
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                matrixString[i][j] = matrix[i][j].toString3Decimals();
                int len = matrixString[i][j].length();
                if (len > colLength[i]) colLength[i] = len;
                if (i == MATRIX_DIM - 1) sumLen += colLength[j];
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "<style>\n" +
                "table, th, td {\n" +
                "  border: 1px solid #BBB;\n" +
                "  border-collapse: collapse;\n" +
                "}\n" +
                "th, td {\n" +
                "  padding: 6px;\n" +
                "}\n" +
                "td {\n" +
                " text-align: center;\n" +
                "}\n" +
                "</style>\n" +
                "</head>\n" +
                "<body>");
        sb.append("<table align=\"center\">\n");
        if (caption) {
            sb.append("<caption>");
            sb.append(name);
            sb.append("</caption>");
        }
        for (int i = 0; i < MATRIX_DIM; i++) {
            sb.append("<tr>\n");
            for (int j = 0; j < MATRIX_DIM; j++) {
                sb.append("<td>");
                sb.append(matrixString[i][j]);
                sb.append("</td>\n");
            }
            sb.append("</tr>\n");
        }
        sb.append("</table>\n</body>");
        return sb.toString();
    }

    public boolean equals(VisualOperator visualOperator) {
        for (int i = 0; i < MATRIX_DIM; i++)
            for (int j = 0; j < MATRIX_DIM; j++)
                if (!matrix[i][j].equalsExact(visualOperator.matrix[i][j]))
                    return false;

        return true;
    }

    public boolean equals3Decimals(VisualOperator visualOperator) {
        for (int i = 0; i < MATRIX_DIM; i++)
            for (int j = 0; j < MATRIX_DIM; j++)
                if (!matrix[i][j].equals3Decimals(visualOperator.matrix[i][j]))
                    return false;

        return true;
    }

    public static Complex[] toQubitArray(final Qubit[] qs) {
        Complex[] inputMatrix = new Complex[(int) Math.pow(2, qs.length)];
        for (int i = 0; i < (int) Math.pow(2, qs.length); i++)
            for (int k = 0; k < qs.length; k++) {
                if (k == 0) {
                    inputMatrix[i] = Complex.multiply(qs[1].matrix[(i >> 1) % 2], qs[0].matrix[i % 2]);
                    k += 1;
                    continue;
                }
                inputMatrix[i] = Complex.multiply(inputMatrix[i], qs[k].matrix[(i >> k) % 2]);
            }

        return inputMatrix;
    }

    private static Complex[][] getQubitTensor(int qubits, VisualOperator v) {
        if (v.getQubitIDs().length != v.getQubits() || v.getQubits() < 1) return null;
        if (v.getQubits() == 1) return getSingleQubitTensor(qubits, v.getQubitIDs()[0], v);
        if (v.getQubits() == qubits) return v.matrix;
        Complex[][] tensor = new Complex[1][1];
        for (int i = 0; i < qubits; i++) {
            if (v.getQubits() == qubits) {
                return v.copy().matrix;
            }
            if (i + v.getQubits() == qubits - 1) {
                tensor = tensorProduct(tensor, v.matrix);
                i += v.getQubits();
            } else if (i == 0) {
                tensor = tensorProduct(ID.matrix, ID.matrix);
            } else
                tensor = tensorProduct(tensor, ID.matrix);
        }
        return tensor;
    }

    private static Complex[][] getSingleQubitTensor(int qubits, int which, VisualOperator v) {
        if (v.getQubits() != 1) return null;
        if (qubits < which || qubits < 1 || which < 0) return null;
        if (qubits == 1) return v.copy().matrix;
        Complex[][] temp = new Complex[1][1];
        for (int i = 0; i < qubits; i++) {
            if (i == 0) {
                temp = tensorProduct(which == qubits - 2 ? v.matrix : ID.matrix, which == (qubits - 1) ? v.matrix : ID.matrix);
                i++;
            } else temp = tensorProduct(which == (qubits - i - 1) ? v.matrix : ID.matrix, temp);
        }
        return temp;
    }

    private static Complex[][] tensorProduct(Complex[][] first, Complex[][] second) {
        int firstDim = first[0].length;
        int secondDim = second[0].length;
        final Complex[][] result = new Complex[firstDim * secondDim][];
        for (int m = 0; m < result.length; m++) {
            int col = firstDim * secondDim;
            result[m] = new Complex[col];
        }
        for (int m = 0; m < firstDim; m++)
            for (int n = 0; n < firstDim; n++)
                for (int o = 0; o < secondDim; o++)
                    for (int p = 0; p < secondDim; p++)
                        result[secondDim * m + o][secondDim * n + p] = Complex.multiply(first[m][n], second[o][p]);

        for (int i = 0; i < result[0].length; i++)
            for (int j = 0; j < result[0].length; j++)
                if (result[i][j] == null) result[i][j] = new Complex(0);

        return result;
    }

    private static Complex[] operateOn(final Complex[] qubitArray, final Complex[][] gateTensor) {
        Complex[] resultMatrix = new Complex[qubitArray.length];
        for (int i = 0; i < gateTensor[0].length; i++) {
            resultMatrix[i] = new Complex(0);
            for (int j = 0; j < gateTensor[0].length; j++) {
                resultMatrix[i].add(Complex.multiply(gateTensor[i][j], qubitArray[j]));
            }
        }
        return resultMatrix;
    }

    public Complex[] operateOn(final Complex[] qubitArray, int qubits) {
        if (NQBITS == 1) {
            return operateOn(qubitArray, getQubitTensor(qubits, this));
        }
        Complex[] inputMatrix = new Complex[qubitArray.length];
        for (int i = 0; i < qubitArray.length; i++) {
            inputMatrix[getPos(qubits, i)] = qubitArray[i].copy();
        }
        inputMatrix = operateOn(inputMatrix, getQubitTensor(qubits, this));
        for (int i = 0; i < qubitArray.length; i++) {
            qubitArray[i] = inputMatrix[getPos(qubits, i)].copy();
        }
        return qubitArray;
    }

    private int getPos(final int qubits, final int posNow) {
        int[] x = new int[qubits];
        for (int i = 0; i < qubits; i++) {
            x[i] = ((posNow) >> (qubits - i - 1)) % 2;
        }
        for (int i = 0; i < qubit_ids.length; i++) {
            int tmp = x[qubits - i - 1];
            x[qubits - i - 1] = x[qubit_ids[qubit_ids.length - i - 1]];
            x[qubit_ids[qubit_ids.length - i - 1]] = tmp;
        }
        int ret = 0;
        for (int i = 0; i < qubits; i++) {
            ret += x[i] << (qubits - i - 1);
        }
        return ret;
    }

    public Qubit[] measure(final Complex[] qubitArray, int qubits) {
        double[] probs = new double[qubitArray.length];
        double subtrahend = 0;
        for (int i = 0; i < qubitArray.length; i++) {
            probs[i] = Complex.multiply(Complex.conjugate(qubitArray[i]), qubitArray[i]).real;
        }
        for (int i = 0; i < qubitArray.length; i++) {
            double prob = random.nextDouble();
            if (probs[i] > prob * (1 - subtrahend)) {
                Qubit[] result = new Qubit[qubits];
                for (int j = 0; j < qubits; j++) {
                    result[j] = new Qubit();
                    if ((i >> (j)) % 2 == 1) result[j].prepare(true);
                }
                return result;
            } else {
                subtrahend += probs[i];
                if (i == qubitArray.length - 2) {
                    subtrahend = 2;
                }
            }
        }
        Log.e("VisualOperator", "NO RESULT");
        return null;
    }

    public static float[] measureProbabilities(final Complex[] qubitArray) {
        float[] probs = new float[qubitArray.length];
        Log.w("STATE", "The statevector of the system is the following");
        for (int i = 0; i < qubitArray.length; i++) {
            if (!qubitArray[i].isZero()) Log.w("STATE" + i, qubitArray[i].toString3Decimals());
            probs[i] = (float) Complex.multiply(Complex.conjugate(qubitArray[i]), qubitArray[i]).real;
        }
        return probs;
    }

    public Qubit[] operateOn(final Qubit[] qs) {
        if (qs.length != NQBITS) {
            Log.e("VisualOperator", "NO RESULT");
            return null;
        }
        if (NQBITS == 1) {
            Qubit q = qs[0].copy();
            q.matrix[0] = Complex.multiply(matrix[0][0], qs[0].matrix[0]);
            q.matrix[0].add(Complex.multiply(matrix[0][1], qs[0].matrix[1]));
            q.matrix[1] = Complex.multiply(matrix[1][0], qs[0].matrix[0]);
            q.matrix[1].add(Complex.multiply(matrix[1][1], qs[0].matrix[1]));
            return new Qubit[]{q};
        }
        Complex[] inputMatrix = toQubitArray(qs);
        Complex[] resultMatrix = new Complex[MATRIX_DIM];
        for (int i = 0; i < MATRIX_DIM; i++) {
            resultMatrix[i] = new Complex(0);
            for (int j = 0; j < MATRIX_DIM; j++) {
                resultMatrix[i].add(Complex.multiply(matrix[i][j], inputMatrix[j]));
            }
        }
        double[] probs = new double[MATRIX_DIM];
        double subtrahend = 0;
        for (int i = 0; i < MATRIX_DIM; i++) {
            probs[i] = Complex.multiply(Complex.conjugate(resultMatrix[i]), resultMatrix[i]).real;
            double prob = random.nextDouble();
            if (probs[i] > prob * (1 - subtrahend)) {
                Qubit[] result = new Qubit[NQBITS];
                for (int j = 0; j < NQBITS; j++) {
                    result[j] = new Qubit();
                    if ((i >> (NQBITS - j - 1)) % 2 == 1) result[j].prepare(true);
                }
                return result;
            } else {
                subtrahend += probs[i];
                if (i == MATRIX_DIM - 2) {
                    subtrahend = 2;
                }
            }
        }
        Log.e("VisualOperator", "NO RESULT");
        return null;
    }

    public static String[] generateSymbols(int DIM) {
        int NQBITS = 0;
        switch (DIM) {
            case 2:
                NQBITS = 1;
                break;
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
        String[] sym = new String[NQBITS];
        for (int i = 0; i < NQBITS; i++)
            sym[i] = "C" + i;
        return sym;
    }

    public static LinkedList<String> getPredefinedGateNames() {
        LinkedList<String> list = new LinkedList<>();
        VisualOperator visualOperator = new VisualOperator();
        try {
            Field[] fields = visualOperator.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.get(visualOperator) instanceof VisualOperator) {
                    list.add(((VisualOperator) field.get(visualOperator)).getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    public static LinkedList<String> getPredefinedGateNames(boolean singleOnly) {
        LinkedList<String> list = new LinkedList<>();
        VisualOperator visualOperator = new VisualOperator();
        try {
            Field[] fields = visualOperator.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.get(visualOperator) instanceof VisualOperator) {
                    if (!((VisualOperator) field.get(visualOperator)).isMultiQubit() == singleOnly) {
                        list.add(((VisualOperator) field.get(visualOperator)).getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    public static VisualOperator findGateByName(String name) {
        VisualOperator visualOperator = new VisualOperator();
        try {
            Field[] fields = visualOperator.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.get(visualOperator) instanceof VisualOperator) {
                    if (((VisualOperator) field.get(visualOperator)).getName().equals(name)) {
                        return ((VisualOperator) field.get(visualOperator));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public double determinantMod() {
        if (NQBITS == 1) {
            return Complex.sub(Complex.multiply(matrix[0][0], matrix[1][1]), Complex.multiply(matrix[0][1], matrix[1][0])).mod();
        } else {
            Qubit[] qs = new Qubit[NQBITS];
            for (int m = 0; m < NQBITS; m++) {
                qs[m] = new Qubit();
            }
            Complex[] inputMatrix = new Complex[MATRIX_DIM];
            Complex[] resultMatrix = new Complex[MATRIX_DIM];
            for (int i = 0; i < MATRIX_DIM; i++) {
                resultMatrix[i] = new Complex(0);
                for (int k = 0; k < NQBITS; k++) {
                    if (k == 0) {
                        inputMatrix[i] = Complex.multiply(qs[NQBITS - 2].matrix[(i >> 1) % 2], qs[NQBITS - 1].matrix[i % 2]);
                        k += 1;
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
            double prob = 0f;
            for (int i = 0; i < MATRIX_DIM; i++) {
                prob += Complex.multiply(Complex.conjugate(resultMatrix[i]), resultMatrix[i]).real;
            }
            return prob;
        }
    }

    public boolean isSpecial() {
        double detMod = determinantMod();
        return detMod < 1.0001 && detMod > 0.9999;
    }

    public boolean isUnitary() {
        Qubit q1 = new Qubit();
        Qubit q2 = new Qubit();
        Qubit q3 = new Qubit();
        q2.applyOperator(VisualOperator.HADAMARD);
        q3.applyOperator(VisualOperator.HADAMARD);
        q3.applyOperator(VisualOperator.S_GATE);
        Qubit[] qa = new Qubit[NQBITS + 2];
        Qubit[] qb = new Qubit[NQBITS + 2];
        Qubit[] qc = new Qubit[NQBITS + 2];
        if (NQBITS != 1) {
            for (int i = 0; i < NQBITS + 2; i++) {
                if (i < NQBITS) qubit_ids[i] = i;
                qa[i] = q1.copy();
                qb[i] = q2.copy();
                qc[i] = q3.copy();
            }
            Complex[] a1 = toQubitArray(qa);
            Complex[] a2 = toQubitArray(qb);
            Complex[] a3 = toQubitArray(qc);
            VisualOperator vo = hermitianConjugate(this);
            Complex[] o1 = vo.copy().operateOn(copy().operateOn(a1, NQBITS + 2), NQBITS + 2);
            Complex[] o2 = vo.copy().operateOn(copy().operateOn(a2, NQBITS + 2), NQBITS + 2);
            Complex[] o3 = vo.copy().operateOn(copy().operateOn(a3, NQBITS + 2), NQBITS + 2);
            Complex[] b1 = toQubitArray(qa);
            Complex[] b2 = toQubitArray(qb);
            Complex[] b3 = toQubitArray(qc);
            for (int i = 0; i < a1.length; i++) {
                Log.e("1", b1[i] + "--" + o1[i]);
                Log.e("2", b2[i] + "--" + o2[i]);
                Log.e("3", b3[i] + "--" + o3[i]);
                if (!b1[i].equals3Decimals(o1[i])) return false;
                if (!b2[i].equals3Decimals(o2[i])) return false;
                if (!b3[i].equals3Decimals(o3[i])) return false;
            }
        } else {
            Qubit o1 = hermitianConjugate(this).operateOn(operateOn(new Qubit[]{q1.copy()}))[0];
            Qubit o2 = hermitianConjugate(this).operateOn(operateOn(new Qubit[]{q2.copy()}))[0];
            Qubit o3 = hermitianConjugate(this).operateOn(operateOn(new Qubit[]{q3.copy()}))[0];
            for (int i = 0; i < 2; i++) {
                if (!o1.matrix[i].equals3Decimals(q1.matrix[i])) return false;
                if (!o2.matrix[i].equals3Decimals(q2.matrix[i])) return false;
                if (!o3.matrix[i].equals3Decimals(q3.matrix[i])) return false;
            }
        }
        return true;
    }

    public void setColor(int color1) {
        color = color1;
    }

    public int getColor() {
        return color;
    }

    public void addRect(@NonNull Rect rect) {
        if (rectangle == null) rectangle = new LinkedList<>();
        rectangle.add(rect);
    }

    public void resetRect() {
        if (rectangle == null) rectangle = new LinkedList<>();
        rectangle.clear();
    }

    public List<Rect> getRect() {
        if (rectangle == null) rectangle = new LinkedList<>();
        return rectangle;
    }

    public boolean isMultiQubit() {
        return MATRIX_DIM != 2;
    }

    public int getQubits() {
        return NQBITS;
    }

    public boolean setQubitIDs(int[] qubit_ids) {
        if (qubit_ids.length != this.qubit_ids.length) {
            return false;
        } else {
            this.qubit_ids = qubit_ids;
            return true;
        }
    }

    public int[] getQubitIDs() {
        return qubit_ids;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
