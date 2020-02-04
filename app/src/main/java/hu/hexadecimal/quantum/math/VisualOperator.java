package hu.hexadecimal.quantum.math;

import android.graphics.RectF;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;

public class VisualOperator implements Serializable {

    public static final long serialVersionUID = 2L;
    public static final transient long helpVersion = 31L;
    private Complex[][] matrix;
    //last one is to clarify meaning for navbar, so length is +1 to qubits
    private String[] symbols;
    private Random random;

    /**
     * Visual Quantum Gate
     */
    public static final String FILE_EXTENSION_LEGACY = ".vqg";
    /**
     * Quantum Gate File
     */
    public static final transient String FILE_EXTENSION = ".qgf";
    private final int NQBITS;
    public int color = 0xff000000;
    public String name;
    private transient LinkedList<RectF> rectangle;
    private final int MATRIX_DIM;
    private int[] qubit_ids;
    private transient double theta;
    private transient double phi;
    private transient double lambda;

    public static final int HTML_MODE_BODY = 0b1;
    public static final int HTML_MODE_CAPTION = 0b10;
    public static final int HTML_MODE_FAT = 0b100;
    public static final int HTML_MODE_BASIC = 0b0;

    private static final transient double NULL_ANGLE = -10E10;

    public static final transient VisualOperator CNOT =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)}
                    }, "CNOT", new String[]{"●", "⊕", "cX"}, 0xff009E5F);

    public static final transient VisualOperator CY =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(0, -1)},
                            {new Complex(0), new Complex(0), new Complex(0, 1), new Complex(0)}
                    }, "CY", new String[]{"●", "Y", "cY"}, 0xff009E5F);

    public static final transient VisualOperator CZ =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(-1)}
                    }, "CZ", new String[]{"●", "Z", "cZ"}, 0xff009E5F);

    public static final transient VisualOperator SWAP =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(1)}
                    }, "SWAP", new String[]{"✖", "✖", "SWAP"}, 0xffF28B00);

    public static final transient VisualOperator CS =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(-Math.PI / 4), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(Math.PI / 4)}
                    }, "Controlled π/2 shift", new String[]{"●", "S", "cS"}, 0xff21BAAB);

    public static final transient VisualOperator CT =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(-Math.PI / 8), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(0), new Complex(Math.PI / 8)}
                    }, "Controlled π/4 shift", new String[]{"●", "T", "cT"}, 0xffBA7021);

    public static final transient VisualOperator CH =
            new VisualOperator(4,
                    new Complex[][]{
                            {new Complex(1), new Complex(0), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(1), new Complex(0), new Complex(0)},
                            {new Complex(0), new Complex(0), new Complex(1.0 / Math.sqrt(2), 0), new Complex(1.0 / Math.sqrt(2), 0)},
                            {new Complex(0), new Complex(0), new Complex(1.0 / Math.sqrt(2), 0), new Complex(-1.0 / Math.sqrt(2), 0)}
                    }, "Controlled Hadamard", new String[]{"●", "H", "cH"}, 0xff2155BA);

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
                    }, "Toffoli", new String[]{"●", "●", "⊕", "TOF"}, 0xff9200D1);

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
                    }, "Fredkin", new String[]{"●", "✖", "✖", "FRE"}, 0xffD10075);

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

    /*public static final transient VisualOperator PI6_GATE =
            new VisualOperator(2, new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(Math.PI / 6)}
            }, "π/6 Phase-shift", new String[]{"π6"}, 0xffDCE117);*/

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
        if (symbols.length < NQBITS || symbols.length > NQBITS + 1 || (symbols.length == 2 && NQBITS == 1)) {
            throw new NullPointerException("Invalid symbol");
        }
        this.name = name;
        this.symbols = symbols.clone();
        matrix = M;

        random = new Random();
        qubit_ids = new int[NQBITS];
        theta = phi = lambda = NULL_ANGLE;
    }

    public VisualOperator(int MATRIX_DIM, Complex[][] M) {
        this(MATRIX_DIM, M, "Custom", VisualOperator.generateSymbols(MATRIX_DIM), 0xff000000);
        random = new Random();
        theta = phi = lambda = NULL_ANGLE;
    }

    public VisualOperator() {
        random = new Random();
        qubit_ids = new int[NQBITS = 2];
        rectangle = new LinkedList<>();
        MATRIX_DIM = 4;
        name = "";
        theta = phi = lambda = NULL_ANGLE;
    }

    public VisualOperator(double theta, double phi) {
        if (theta < -Math.PI || theta > Math.PI || phi < -Math.PI * 2 || phi > Math.PI * 2)
            throw new IllegalArgumentException("Invalid angle size! theta: " + theta + ", phi: " + phi);
        Complex[][] matrixTheta = new Complex[][]{
                new Complex[]{new Complex(Math.cos(theta / 2), 0), new Complex(0, -Math.sin(theta / 2))},
                new Complex[]{new Complex(0, -Math.sin(theta / 2)), new Complex(Math.cos(theta / 2), 0)}
        };
        Complex[][] matrixPhi = new Complex[][]{
                new Complex[]{new Complex(1), new Complex(0)},
                new Complex[]{new Complex(0), new Complex(phi)}
        };
        matrix = matrixProduct(matrixPhi, matrixTheta);
        MATRIX_DIM = 2;
        qubit_ids = new int[NQBITS = 1];
        rectangle = new LinkedList<>();
        color = 0xffDEAC38;
        symbols = new String[]{"R" + (theta == 0 ? "" : "\u03B8") + (phi == 0 ? "" : "\u03C6")};
        name = "CustRot";
        this.theta = theta;
        this.phi = phi;
        lambda = NULL_ANGLE;
    }

    public VisualOperator(double theta, double phi, double lambda) {
        matrix = new Complex[][]{
                new Complex[]{new Complex(Math.cos(theta / 2), 0), Complex.multiply(new Complex(lambda), new Complex(-Math.sin(theta / 2), 0))},
                new Complex[]{Complex.multiply(new Complex(phi), new Complex(Math.sin(theta / 2), 0)), Complex.multiply(new Complex(lambda + phi), new Complex(Math.cos(theta / 2), 0))}
        };
        MATRIX_DIM = 2;
        qubit_ids = new int[NQBITS = 1];
        rectangle = new LinkedList<>();
        color = 0xFFD12000;
        symbols = new String[]{"U3"};
        name = "U3";
        this.theta = theta;
        this.phi = phi;
        this.lambda = lambda;
    }

    public String[] getSymbols() {
        return symbols;
    }

    public boolean setSymbols(String[] symbols) {
        if (symbols.length == NQBITS) {
            this.symbols = symbols;
            return true;
        } else if (symbols.length == NQBITS + 1 && !(symbols.length == 2 && NQBITS == 1)) {
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
        for (int i = 0; i < symbols.length; i++) {
            symbols[i] += "†";
        }
        if (theta != NULL_ANGLE) {
            theta = -theta;
        }
        if (phi != NULL_ANGLE) {
            phi = -phi;
        }
        if (lambda != NULL_ANGLE) {
            lambda = -lambda;
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
        return equals3Decimals(t);
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

    public boolean isRotation() {
        return lambda == NULL_ANGLE && theta != NULL_ANGLE && phi != NULL_ANGLE && !isMultiQubit() && name.equals("CustRot");
    }

    public boolean isU3() {
        return lambda != NULL_ANGLE && theta != NULL_ANGLE && phi != NULL_ANGLE && !isMultiQubit() && name.equals("U3");
    }

    public double[] getAngles() {
        if (isU3() || isRotation()) {
            return new double[]{theta, phi, lambda};
        } else if (!isMultiQubit()) {
            VisualOperator operator = copy();
            if (!matrix[0][0].isReal()) {
                Log.d("Quantum VisOp", "Global phase conversion necessary...");
                operator.multiply(Complex.divide(new Complex(1, 0), new Complex(Math.atan(matrix[0][0].imaginary / matrix[0][0].real))));
            }
            Complex theta = Complex.multiply(new Complex(2, 0), Complex.acos(operator.matrix[0][0]));
            Complex lambda = Complex.divide(Complex.sub(Complex.log(new Complex(Math.E, 0), Complex.multiply(new Complex(-1, 0), operator.matrix[0][1])),
                    Complex.log(new Complex(Math.E, 0), Complex.sin(Complex.acos(operator.matrix[0][0])))), new Complex(0, 1));
            Complex phi = Complex.divide(Complex.sub(Complex.log(new Complex(Math.E, 0), operator.matrix[1][0]),
                    Complex.log(new Complex(Math.E, 0), Complex.sin(Complex.acos(operator.matrix[0][0])))), new Complex(0, 1));
            Complex phi2 = Complex.sub(Complex.divide(Complex.log(new Complex(Math.E, 0), operator.matrix[1][1]), Complex.multiply(operator.matrix[0][0], new Complex(0, 1))), lambda);
            Log.d("Quantum VisOp", "theta: " + theta.toString3Decimals() + ", lambda: " + lambda.toString3Decimals() + ", phi: " + phi.toString3Decimals() + ", phi2: " + phi2.toString3Decimals());
            return new double[]{theta.real, phi2.isReal() ? phi2.real : phi.real, lambda.real};
        } else {
            return new double[]{theta, phi, lambda};
        }
    }

    public VisualOperator copy() {
        Complex[][] complex = new Complex[MATRIX_DIM][MATRIX_DIM];
        for (int i = 0; i < MATRIX_DIM; i++) {
            complex[i] = new Complex[MATRIX_DIM];
            for (int j = 0; j < MATRIX_DIM; j++) {
                complex[i][j] = matrix[i][j].copy();
            }
        }
        String[] sym = new String[symbols.length];
        System.arraycopy(symbols, 0, sym, 0, symbols.length);
        VisualOperator v = new VisualOperator(MATRIX_DIM, complex, name, sym, color);
        v.theta = theta;
        v.phi = phi;
        v.lambda = lambda;
        v.qubit_ids = new int[qubit_ids.length];
        System.arraycopy(qubit_ids, 0, v.qubit_ids, 0, qubit_ids.length);
        return v;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Complex[] c : matrix) {
            for (Complex z : c) {
                sb.append(z.toString3Decimals());
                sb.append(", ");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append('\n');
        }
        return sb.toString();
    }

    public String toString(int decimals) {
        StringBuilder sb = new StringBuilder();
        for (Complex[] c : matrix) {
            for (Complex z : c) {
                sb.append(z.toString(decimals));
                sb.append(",");
            }
            sb.deleteCharAt(sb.length() - 1);
            sb.append('\n');
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public JSONObject toJSON() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("matrix_dim", MATRIX_DIM);
        jsonObject.put("color", color);
        jsonObject.put("qubit_count", NQBITS);
        JSONArray qubits = new JSONArray();
        JSONArray symbols = new JSONArray();
        for (int i = 0; i < qubit_ids.length; i++) {
            qubits.put(qubit_ids[i]);
        }
        for (int i = 0; i < this.symbols.length; i++) {
            symbols.put(this.symbols[i]);
        }
        if (isU3() || isRotation()) {
            JSONObject angles = new JSONObject();
            angles.put("theta", theta);
            angles.put("phi", phi);
            angles.put("lambda", lambda);
            jsonObject.put("angles", angles);
        }
        jsonObject.put("qubits", qubits);
        jsonObject.put("symbols", symbols);
        for (int i = 0; i < matrix.length; i++) {
            JSONArray jsonArray = new JSONArray();
            for (int j = 0; j < matrix.length; j++) {
                jsonArray.put(matrix[i][j].toString());
            }
            jsonObject.put("matrix_" + i, jsonArray);
        }
        return jsonObject;
    }

    public static VisualOperator fromJSON(JSONObject jsonObject) {
        try {
            String name = jsonObject.getString("name");
            int matrix_dim = jsonObject.getInt("matrix_dim");
            int color = jsonObject.getInt("color");
            int qubit_count = jsonObject.getInt("qubit_count");
            double theta = NULL_ANGLE;
            double phi = NULL_ANGLE;
            double lambda = NULL_ANGLE;
            try {
                JSONObject angles = jsonObject.getJSONObject("angles");
                theta = angles.getDouble("theta");
                phi = angles.getDouble("phi");
                lambda = angles.getDouble("lambda");
            } catch (Exception e) {
                Log.i("VisualOperator fromJSON", "No angles?");
            }
            JSONArray qubitsJson = jsonObject.getJSONArray("qubits");
            JSONArray symbolsJson = jsonObject.getJSONArray("symbols");
            int[] qubits = new int[qubitsJson.length()];
            String[] symbols = new String[symbolsJson.length()];
            for (int i = 0; i < qubitsJson.length(); i++) {
                qubits[i] = qubitsJson.getInt(i);
            }
            for (int i = 0; i < symbolsJson.length(); i++) {
                symbols[i] = symbolsJson.getString(i);
            }
            Complex[][] matrix = new Complex[matrix_dim][matrix_dim];
            for (int i = 0; i < matrix_dim; i++) {
                JSONArray row = jsonObject.getJSONArray("matrix_" + i);
                for (int j = 0; j < matrix_dim; j++) {
                    matrix[i][j] = Complex.parse(row.getString(j));
                }
            }
            VisualOperator visualOperator = new VisualOperator(matrix_dim, matrix, name, symbols, color);
            visualOperator.qubit_ids = qubits;
            visualOperator.theta = theta;
            visualOperator.phi = phi;
            visualOperator.lambda = lambda;
            return visualOperator;
        } catch (Exception e) {
            Log.e("VisualOperatorLoader", "Error while parsing:");
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Default behaviour, using HTML_MODE_BODY
     *
     * @return Matrix formatted to a table in HTML style
     */
    public String toStringHtmlTable() {
        return toStringHtmlTable(HTML_MODE_BODY);
    }

    public String toStringHtmlTable(int MODE) {
        StringBuilder sb = new StringBuilder();
        if ((MODE & HTML_MODE_BODY) > 0) {
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
        }
        sb.append("<table align=\"center\">\n");
        if ((MODE & HTML_MODE_CAPTION) > 0) {
            sb.append("<caption>");
            sb.append(name.replace("π", "&pi;"));
            sb.append("</caption>\n");
        }
        for (int i = 0; i < MATRIX_DIM; i++) {
            sb.append("<tr>\n");
            for (int j = 0; j < MATRIX_DIM; j++) {
                sb.append("<td>");
                String matrixString = matrix[i][j].toString3Decimals();
                if ((MODE & HTML_MODE_FAT) > 0 && matrixString.length() < 3)
                    sb.append("&ensp;").append(matrixString).append("&ensp;");
                else
                    sb.append(matrixString);
                sb.append("</td>\n");
            }
            sb.append("</tr>\n");
        }
        sb.append("</table>\n");
        if ((MODE & HTML_MODE_BODY) > 0) {
            sb.append("</body>\n");
        }
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
        if (MATRIX_DIM != visualOperator.MATRIX_DIM) {
            return false;
        }
        for (int i = 0; i < MATRIX_DIM; i++)
            for (int j = 0; j < MATRIX_DIM; j++)
                if (!matrix[i][j].equals3Decimals(visualOperator.matrix[i][j]))
                    return false;

        return true;
    }

    public static Complex[] toQubitArray(final Qubit[] qs) {
        Complex[] inputMatrix = new Complex[1 << qs.length];
        for (int i = 0; i < (1 << qs.length); i++)
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

    private static Complex[][] matrixProduct(Complex[][] first, Complex[][] second) {
        int dim1c = first.length;
        int dim2c = second.length;
        if (dim1c != dim2c || dim1c == 0)
            return null;
        int dim1r = first[0].length;
        int dim2r = second[0].length;
        if (dim1r != dim2r || dim1r != dim2c)
            return null;
        Complex[][] output = new Complex[dim1r][dim1r];
        for (int i = 0; i < dim1r; i++) {
            for (int j = 0; j < dim1r; j++) {
                output[i][j] = new Complex(0);
                for (int m = 0; m < dim1r; m++) {
                    output[i][j].add(Complex.multiply(first[i][m], second[m][j]));
                }
            }
        }
        return output;
    }

    public VisualOperator matrixMultiplication(VisualOperator second) {
        if (this.MATRIX_DIM != second.MATRIX_DIM)
            return this;
        this.matrix = matrixProduct(this.matrix, second.matrix);
        this.name = "MUL";
        return this;
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

    public int measureFromProbabilities(final float[] probabilities) {
        double subtrahend = 0;
        for (int i = 0; i < probabilities.length; i++) {
            double prob = random.nextDouble();
            if (probabilities[i] > prob * (1 - subtrahend)) {
                return i;
            } else {
                subtrahend += probabilities[i];
                if (i == probabilities.length - 2) {
                    subtrahend = 2;
                }
            }
        }
        return -1;
    }

    public Qubit[] measure(final Complex[] qubitArray, int qubits) {
        double[] probs = new double[qubitArray.length];
        double subtrahend = 0;
        for (int i = 0; i < qubitArray.length; i++) {
            probs[i] = Math.pow(qubitArray[i].mod(), 2);
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
        for (int i = 0; i < qubitArray.length; i++) {
            probs[i] = (float) Math.pow(qubitArray[i].mod(), 2);
            if (probs[i] < Math.pow(10, -20)) probs[i] = 0;
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
        int NQBITS;
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

    public static LinkedList<VisualOperator> getPredefinedGates(boolean singleOnly) {
        LinkedList<VisualOperator> list = new LinkedList<>();
        VisualOperator visualOperator = new VisualOperator();
        try {
            Field[] fields = visualOperator.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.get(visualOperator) instanceof VisualOperator) {
                    VisualOperator visualOperatorField = (VisualOperator) field.get(visualOperator);
                    if (!(visualOperatorField.isMultiQubit() && singleOnly)) {
                        if (visualOperatorField.isMultiQubit()) {
                            list.addLast(visualOperatorField);
                        } else {
                            list.add(0, visualOperatorField);
                        }
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
        Qubit[] qX = new Qubit[]{new Qubit(), new Qubit(), new Qubit(), new Qubit()};
        qX[1].applyOperator(VisualOperator.HADAMARD);
        qX[2].applyOperator(VisualOperator.HADAMARD);
        qX[2].applyOperator(VisualOperator.S_GATE);
        qX[3].applyOperator(VisualOperator.SQRT_NOT);
        Qubit[][] qAX = new Qubit[qX.length][NQBITS + 2];
        if (NQBITS != 1) {
            for (int i = 0; i < NQBITS + 2; i++) {
                if (i < NQBITS) qubit_ids[i] = i;
                for (int m = 0; m < qX.length; m++) {
                    qAX[m][i] = qX[m].copy();
                }
            }
            Complex[][] aX = new Complex[qX.length][];
            Complex[][] bX = new Complex[qX.length][];
            Complex[][] oX = new Complex[qX.length][];
            for (int m = 0; m < qX.length; m++) {
                aX[m] = toQubitArray(qAX[m]);
                bX[m] = toQubitArray(qAX[m]);
            }
            VisualOperator vo = hermitianConjugate(this);
            for (int m = 0; m < qX.length; m++) {
                oX[m] = vo.copy().operateOn(copy().operateOn(aX[m], NQBITS + 2), NQBITS + 2);
            }
            for (int m = qX.length - 1; m >= 0; m--) {
                for (int i = 0; i < oX[0].length; i++) {
                    if (!bX[m][i].equals3Decimals(oX[m][i])) return false;
                }
            }
        } else {
            for (int m = qX.length - 1; m >= 0; m--) {
                Qubit o = hermitianConjugate(this).operateOn(operateOn(new Qubit[]{qX[m].copy()}))[0];
                for (int j = 0; j < 2; j++) {
                    if (!o.matrix[j].equals3Decimals(qX[m].matrix[j])) return false;
                }
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

    public void addRect(@NonNull RectF rect) {
        if (rectangle == null) rectangle = new LinkedList<>();
        rectangle.add(rect);
    }

    public void resetRect() {
        if (rectangle == null) rectangle = new LinkedList<>();
        rectangle.clear();
    }

    public List<RectF> getRect() {
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

    public String getOpenQASMSymbol() {
        String line = "";
        if (equals3Decimals(HADAMARD)) {
            line += "h qubit[" + getQubitIDs()[0] + "];";
        } else if (equals3Decimals(PAULI_X)) {
            line += "x qubit[" + getQubitIDs()[0] + "];";
        } else if (equals3Decimals(PAULI_Y)) {
            line += "y qubit[" + getQubitIDs()[0] + "];";
        } else if (equals3Decimals(PAULI_Z)) {
            line += "z qubit[" + getQubitIDs()[0] + "];";
        } else if (equals3Decimals(ID)) {
            line += "id qubit[" + getQubitIDs()[0] + "];";
        } else if (equals3Decimals(T_GATE)) {
            line += "t qubit[" + getQubitIDs()[0] + "];";
        } else if (equals3Decimals(S_GATE)) {
            line += "s qubit[" + getQubitIDs()[0] + "];";
        } else if (equals3Decimals(CNOT)) {
            line += "cx qubit[" + getQubitIDs()[0] + "],qubit[" + getQubitIDs()[1] + "];";
        } else if (equals3Decimals(CY)) {
            line += "cy qubit[" + getQubitIDs()[0] + "],qubit[" + getQubitIDs()[1] + "];";
        } else if (equals3Decimals(CZ)) {
            line += "cz qubit[" + getQubitIDs()[0] + "],qubit[" + getQubitIDs()[1] + "];";
        } else if (equals3Decimals(CT)) {
            line += "crz(pi/4) qubit[" + getQubitIDs()[0] + "],qubit[" + getQubitIDs()[1] + "];";
        } else if (equals3Decimals(CS)) {
            line += "crz(pi/2) qubit[" + getQubitIDs()[0] + "],qubit[" + getQubitIDs()[1] + "];";
        } else if (equals3Decimals(CH)) {
            line += "ch qubit[" + getQubitIDs()[0] + "],qubit[" + getQubitIDs()[1] + "];";
        } else if (equals3Decimals(SWAP)) {
            line += "swap qubit[" + getQubitIDs()[0] + "],qubit[" + getQubitIDs()[1] + "];";
        } else if (equals3Decimals(TOFFOLI)) {
            line += "ccx qubit[" + getQubitIDs()[0] + "],qubit[" + getQubitIDs()[1] + "],qubit[" + getQubitIDs()[2] + "];";
        } else if (equals3Decimals(FREDKIN)) {
            line += "cswap qubit[" + getQubitIDs()[0] + "],qubit[" + getQubitIDs()[1] + "],qubit[" + getQubitIDs()[2] + "];";
        } else if (equals3Decimals(hermitianConjugate(T_GATE.copy()))) {
            line += "tdg qubit[" + getQubitIDs()[0] + "];";
        } else if (equals3Decimals(hermitianConjugate(S_GATE.copy()))) {
            line += "sdg qubit[" + getQubitIDs()[0] + "];";
        } else if (equals3Decimals(SQRT_NOT)) {
            line += "h qubit[" + getQubitIDs()[0] + "];\n";
            line += "sdg qubit[" + getQubitIDs()[0] + "];\n";
            line += "ry(pi/2) qubit[" + getQubitIDs()[0] + "];";
        } else if (isU3()) {
            line += "u3(" + theta + "," + phi + "," + lambda + ") qubit[" + getQubitIDs()[0] + "];";
        } else if (isRotation()) {
            line += "rx(" + theta + ") qubit[" + getQubitIDs()[0] + "];\n";
            line += "rz(" + phi + ") qubit[" + getQubitIDs()[0] + "];";
        } else if (!isMultiQubit()) {
            double[] angles = getAngles();
            line += "u3(" + angles[0] + "," + angles[1] + "," + angles[2] + ") qubit[" + getQubitIDs()[0] + "];\n";
            line += "//U3 autoconvert: " + getName();
        } else {
            line += "//The following gate cannot be exported into OpenQASM: " + getName();
        }
        return line;
    }
}
