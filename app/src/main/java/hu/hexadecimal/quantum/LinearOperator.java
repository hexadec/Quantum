package hu.hexadecimal.quantum;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.LinkedList;

public class LinearOperator extends VisualOperator implements Serializable {

    public static final long serialVersionUID = 1L;
    public Complex[][] matrix;
    public String symbol;
    public static final int MATRIX_DIM = 2;
    private int index[];

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
    public LinearOperator() {
        super(MATRIX_DIM);
        matrix = null;
        name = "Empty";
    }

    public String getSymbol() {
        return symbol;
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

    public boolean equals3Decimals(LinearOperator linearOperator) {
        Complex[][] m = linearOperator.matrix;
        for (int i = 0; i < MATRIX_DIM; i++) {
            for (int j = 0; j < MATRIX_DIM; j++) {
                if (!this.matrix[i][j].equals3Decimals(m[i][j])) {
                    return false;
                }
            }
        }
        return true;
    }

    public LinearOperator copy() {
        return new LinearOperator(new Complex[][]{
                new Complex[]{matrix[0][0].copy(), matrix[1][0].copy()},
                new Complex[]{matrix[0][1].copy(), matrix[1][1].copy()}
        }, name, symbol, color);
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

    public Qubit operateOn(final Qubit qbit) {
        Qubit q = qbit.copy();
        q.matrix[0] = Complex.multiply(matrix[0][0], qbit.matrix[0]);
        q.matrix[0].add(Complex.multiply(matrix[0][1], qbit.matrix[1]));
        q.matrix[1] = Complex.multiply(matrix[1][0], qbit.matrix[0]);
        q.matrix[1].add(Complex.multiply(matrix[1][1], qbit.matrix[1]));
        return q;
    }

    public static LinkedList<String> getPredefinedGateNames() {
        LinkedList<String> list = new LinkedList<>();
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

    public static LinkedList<String> getPredefinedGateSymbols() {
        LinkedList<String> list = new LinkedList<>();
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

    public Complex determinant() {
        return Complex.sub(Complex.multiply(matrix[0][0], matrix[1][1]), Complex.multiply(matrix[0][1], matrix[1][0]));
    }

    public boolean isSpecial() {
        return determinant().mod() < 1.0001 && determinant().mod() > 0.9999;
    }

    public LinearOperator inverse() {
        LinearOperator lcopy = copy();
        Complex[][] invm = LinearOperator.invert(copy().matrix);
        lcopy.matrix = invm;
        lcopy.transpose();
        return lcopy;
    }

    public boolean isUnitary() {
        return inverse().equals3Decimals(LinearOperator.hermitianConjugate(this));
    }

    public static Complex[][] invert(Complex a[][]) {
        int n = a.length;
        Complex x[][] = new Complex[n][n];
        Complex b[][] = new Complex[n][n];
        int index[] = new int[n];
        for (int i = 0; i < n; ++i)
            for (int j = 0; j < n; j++) {
                b[i][j] = new Complex(i == j ? 1 : 0);
            }
        gaussian(a, index);
        for (int i = 0; i < n - 1; ++i)
            for (int j = i + 1; j < n; ++j)
                for (int k = 0; k < n; ++k)
                    b[index[j]][k] = Complex.sub(b[index[j]][k], Complex.multiply(a[index[j]][i], b[index[i]][k]));

        for (int i = 0; i < n; ++i) {
            x[n - 1][i] = Complex.divide(b[index[n - 1]][i], a[index[n - 1]][n - 1]);
            for (int j = n - 2; j >= 0; --j) {
                x[j][i] = b[index[j]][i];
                for (int k = j + 1; k < n; ++k) {
                    x[j][i] = Complex.sub(x[j][i], Complex.multiply(a[index[j]][k], x[k][i]));
                }
                x[j][i] = Complex.divide(x[j][i], a[index[j]][j]);
            }
        }
        return x;
    }

    public static void gaussian(Complex a[][], int index[]) {
        int n = index.length;
        Complex c[] = new Complex[n];

        for (int i = 0; i < n; ++i)
            index[i] = i;

        for (int i = 0; i < n; ++i) {
            Complex c1 = new Complex(0);
            for (int j = 0; j < n; ++j) {
                Complex c0 = new Complex(a[i][j].mod());
                if (c0.mod() > c1.mod()) c1 = c0;
            }
            c[i] = c1;
        }
        int k = 0;
        for (int j = 0; j < n - 1; ++j) {
            Complex pi1 = new Complex(0);
            for (int i = j; i < n; ++i) {
                Complex pi0 = new Complex(a[index[i]][j].mod());
                pi0 = Complex.divide(pi0, c[index[i]]);
                if (pi0.mod() > pi1.mod()) {
                    pi1 = pi0;
                    k = i;
                }
            }

            int itmp = index[j];
            index[j] = index[k];
            index[k] = itmp;
            for (int i = j + 1; i < n; ++i) {
                Complex pj = Complex.divide(a[index[i]][j], a[index[j]][j]);
                a[index[i]][j] = pj;
                for (int l = j + 1; l < n; ++l)
                    a[index[i]][l] = Complex.sub(a[index[i]][l], Complex.multiply(pj, a[index[j]][l]));
            }
        }
    }
}
