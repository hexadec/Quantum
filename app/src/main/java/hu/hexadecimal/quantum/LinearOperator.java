package hu.hexadecimal.quantum;

public class LinearOperator {

    protected Complex[][] matrix;
    private String name;
    private static final int MATRIX_DIM = 2;

    public static final LinearOperator HADAMARD =
            LinearOperator.multiply(
                    new LinearOperator(new Complex[][]{
                            new Complex[]{new Complex(1), new Complex(1)},
                            new Complex[]{new Complex(1), new Complex(-1)}
                    }, "Hadamard"), new Complex(1 / Math.sqrt(2)));

    public static final LinearOperator PAULI_Z =
            new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(-1)}
            }, "Pauli-Z");

    public static final LinearOperator PAULI_Y =
            new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(0), new Complex(0, -1)},
                    new Complex[]{new Complex(0, 1), new Complex(0)}
            }, "Pauli-Y");

    public static final LinearOperator PAULI_X =
            new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(0), new Complex(1)},
                    new Complex[]{new Complex(1), new Complex(0)}
            }, "Pauli-X");

    public static final LinearOperator T_GATE =
            new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(1, Math.PI / 4 ,true)}
            }, "PI/4 Phase-shift");

    public static final LinearOperator S_GATE =
            new LinearOperator(new Complex[][]{
                    new Complex[]{new Complex(1), new Complex(0)},
                    new Complex[]{new Complex(0), new Complex(0, 1)}
            }, "PI/2 Phase-shift");

    public LinearOperator(Complex[][] M, String name) {
        if (M == null) {
            throw new NullPointerException();
        }
        if (M.length == MATRIX_DIM && M[0].length == MATRIX_DIM && M[1].length == MATRIX_DIM) {
            this.name = name;
            matrix = M;
        } else {
            throw new NullPointerException("Invalid array");
        }
    }

    public LinearOperator(Complex[][] M) {
        this(M, "Custom");
    }

    /**
     * Avoid using this constructor whenever possible
     */
    protected LinearOperator() {
        matrix = null;
        name = "Empty";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
    }

    public static LinearOperator hermitianCOnjugate(LinearOperator linearOperator) {
        LinearOperator l = linearOperator.copy();
        l.hermitianConjugate();
        return l;
    }

    public boolean isHermitian() {
        LinearOperator hermiConj = LinearOperator.transpose(this);
        hermiConj.conjugate();
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
                if (!matrix[i][j].equalsExact(linearOperator.matrix[i][j])) {
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
        }, name);
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

    public QBit operateOn(final QBit qbit) {
        QBit q = qbit.copy();
        q.matrix[0] = Complex.multiply(matrix[0][0], qbit.matrix[0]);
        q.matrix[0].add(Complex.multiply(matrix[0][1], qbit.matrix[1]));
        q.matrix[1] = Complex.multiply(matrix[1][0], qbit.matrix[0]);
        q.matrix[1].add(Complex.multiply(matrix[1][1], qbit.matrix[1]));
        return q;
    }
}
