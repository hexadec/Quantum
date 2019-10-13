package hu.hexadecimal.quantum;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class LinearOperator extends VisualOperator {

    protected Complex[][] matrix;
    private String name;
    private String symbol;
    public static final int MATRIX_DIM = 2;

    public static final LinearOperator HADAMARD =
            LinearOperator.multiply(
                    new LinearOperator(new Complex[][]{
                            new Complex[]{new Complex(1), new Complex(1)},
                            new Complex[]{new Complex(1), new Complex(-1)}
                    }, "Hadamard", "ℍ", 0xff2155BA), new Complex(1 / Math.sqrt(2)));

    public static final LinearOperator PAULI_Z =
            new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(-1)}
            }, "Pauli-Z", "ℤ", 0xff60BA21);

    public static final LinearOperator PAULI_Y =
            new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(0), new Complex(0, -1)},
                    new Complex[]{new Complex(0, 1), new Complex(0)}
            }, "Pauli-Y", "\uD835\uDD50", 0xff60BA21);

    public static final LinearOperator PAULI_X =
            new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(0), new Complex(1)},
                    new Complex[]{new Complex(1), new Complex(0)}
            }, "Pauli-X", "\uD835\uDD4F", 0xff60BA21);

    public static final LinearOperator T_GATE =
            new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(1, Math.PI / 4, true)}
            }, "PI/4 Phase-shift", "\uD835\uDD4B", 0xffBA7021);

    public static final LinearOperator S_GATE =
            new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(0, 1)}
            }, "PI/2 Phase-shift", "\uD835\uDD4A", 0xff21BAAB);

    public static final LinearOperator PI6_GATE =
            new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(1, Math.PI / 6, true)}
            }, "PI/6 Phase-shift", "\u03C06", 0xffDCE117);

    public static final LinearOperator SQRT_NOT =
            LinearOperator.multiply(new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(1, 1), new Complex(1, -1)},
                    new Complex[]{new Complex(1, -1), new Complex(1, 1)}
            }, "√NOT", "√\uD835\uDD4F", 0xff2155BA), new Complex(0.5));

    public LinearOperator(Complex[][] M, String name, String symbol, int color) {
        super(MATRIX_DIM);
        if (M == null) {
            throw new NullPointerException();
        }
        if (M.length == MATRIX_DIM && M[0].length == MATRIX_DIM && M[1].length == MATRIX_DIM) {
            this.name = name;
            this.symbol = symbol;
            this.color = color;
            matrix = M;
        } else {
            throw new NullPointerException("Invalid array");
        }
    }

    public LinearOperator(Complex[][] M, String name, String symbol) {
        super(MATRIX_DIM);
        if (M == null) {
            throw new NullPointerException();
        }
        if (M.length == MATRIX_DIM && M[0].length == MATRIX_DIM && M[1].length == MATRIX_DIM) {
            this.name = name;
            this.symbol = symbol;
            matrix = M;
        } else {
            throw new NullPointerException("Invalid array");
        }
    }

    public LinearOperator(Complex[][] M) {
        this(M, "Custom", "CU");
    }

    /**
     * Avoid using this constructor whenever possible
     */
    protected LinearOperator() {
        super(MATRIX_DIM);
        matrix = null;
        name = "Empty";
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void transpose() {
        Complex tmp = matrix[0][1];
        matrix[0][1] = matrix[1][0];
        matrix[1][0] = tmp;
    }

    public static LinearOperator transpose(LinearOperator linearOperator) {
        return new LinearOperator(new Complex[][]{
                new Complex[]{linearOperator.matrix[0][0], linearOperator.matrix[1][0]},
                new Complex[]{linearOperator.matrix[0][1], linearOperator.matrix[1][1]}
        });
    }

    public void conjugate() {
        for (Complex[] ca : matrix) {
            for (Complex z : ca) {
                z.conjugate();
            }
        }
    }

    public static LinearOperator conjugate(LinearOperator linearOperator) {
        LinearOperator l = linearOperator.copy();
        for (Complex[] ca : l.matrix) {
            for (Complex z : ca) {
                z.conjugate();
            }
        }
        return l;
    }

    public void hermitianConjugate() {
        transpose();
        conjugate();
        symbol += "†";
    }

    public static LinearOperator hermitianConjugate(LinearOperator linearOperator) {
        LinearOperator l = linearOperator.copy();
        l.hermitianConjugate();
        return l;
    }

    public boolean isHermitian() {
        LinearOperator hermiConj = LinearOperator.hermitianConjugate(this);
        return equals(hermiConj);
    }

    public void multiply(Complex complex) {
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                matrix[i][j].multiply(complex);
            }
        }
    }

    public static LinearOperator multiply(LinearOperator l, Complex complex) {
        LinearOperator linearOperator = l.copy();
        linearOperator.multiply(complex);
        return linearOperator;
    }

    public boolean equals(LinearOperator linearOperator) {
        Complex[][] m = linearOperator.matrix;
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                if (!this.matrix[i][j].equalsExact(m[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    public LinearOperator copy() {
        return new LinearOperator(new Complex[][]{
                new Complex[]{matrix[0][0], matrix[1][0]},
                new Complex[]{matrix[0][1], matrix[1][1]}
        }, name, symbol, color);
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

    public Qubit operateOn(final Qubit qbit) {
        Qubit q = qbit.copy();
        q.matrix[0] = Complex.multiply(matrix[0][0], qbit.matrix[0]);
        q.matrix[0].add(Complex.multiply(matrix[0][1], qbit.matrix[1]));
        q.matrix[1] = Complex.multiply(matrix[1][0], qbit.matrix[0]);
        q.matrix[1].add(Complex.multiply(matrix[1][1], qbit.matrix[1]));
        return q;
    }

    public static List<String> getPredefinedGateNames() {
        List<String> list = new ArrayList<>();
        LinearOperator linearOperator = new LinearOperator();
        try {
            Field[] fields = linearOperator.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.get(linearOperator) instanceof LinearOperator) {
                    list.add(((LinearOperator) field.get(linearOperator)).getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    public static LinearOperator findGateByName(String name) {
        LinearOperator linearOperator = new LinearOperator();
        try {
            Field[] fields = linearOperator.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.get(linearOperator) instanceof LinearOperator) {
                    if (((LinearOperator) field.get(linearOperator)).getName().equals(name)) {
                        return ((LinearOperator) field.get(linearOperator));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    public static List<String> getPredefinedGateSymbols() {
        List<String> list = new ArrayList<>();
        LinearOperator linearOperator = new LinearOperator();
        try {
            Field[] fields = linearOperator.getClass().getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (java.lang.reflect.Modifier.isStatic(field.getModifiers())
                        && field.get(linearOperator) instanceof LinearOperator) {
                    list.add(((LinearOperator) field.get(linearOperator)).getSymbol());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }
}
