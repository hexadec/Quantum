package hu.hexadecimal.quantum;

import java.text.DecimalFormat;

public class Complex {

    public double real = 0;
    public double imaginary = 0;

    public Complex(double realPart) {
        real = realPart;
    }

    public Complex(double realPart, double imaginaryPart) {
        real = realPart;
        imaginary = imaginaryPart;
    }

    public Complex(double mod, double arg, boolean flag) {
        real = Math.cos(arg) * mod;
        imaginary = Math.sin(arg) * mod;
    }

    public double mod() {
        return Math.sqrt(real * real + imaginary * imaginary);
    }

    public double arg() {
        if (Math.abs(real) < 0.000000001) {
            real = 0;
            if (Math.abs(imaginary) < 0.000000001) {
                imaginary = 0;
                return 0;
            } else return (imaginary >= 0 ? Math.PI / 2 : -1 * Math.PI / 2);
        }
        if (Math.abs(imaginary) < 0.000000001) imaginary = 0;
        return Math.atan(imaginary / real) + (real < 0 ? Math.PI : 0);
    }

    public void invert() {
        real = -real;
        imaginary = -imaginary;
    }

    public static Complex invert(Complex z) {
        return new Complex(-z.real, -z.imaginary);
    }

    public void add(Complex complex) {
        real += complex.real;
        imaginary += complex.imaginary;
    }

    public static Complex add(Complex z, Complex w) {
        return new Complex(z.real + w.real, z.imaginary + w.imaginary);
    }

    public void sub(Complex complex) {
        real -= complex.real;
        imaginary -= complex.imaginary;
    }

    public static Complex sub(Complex z, Complex w) {
        return new Complex(z.real - w.real, z.imaginary - w.imaginary);
    }

    public void multiply(Complex complex) {
        double arg = arg() + complex.arg();
        double mod = mod() * complex.mod();
        real = Math.cos(arg) * mod;
        imaginary = Math.sin(arg) * mod;
    }

    public static Complex multiply(Complex z, Complex w) {
        double arg = z.arg() + w.arg();
        double mod = z.mod() * w.mod();
        return new Complex(mod, arg, true);
    }

    public void conjugate() {
        imaginary = -imaginary;
    }

    public static Complex conjugate(Complex z) {
        return new Complex(z.real, -z.imaginary);
    }

    public void reciprocal() {
        double mod2 = real * real + imaginary * imaginary;
        real /= mod2;
        imaginary /= (-1) * mod2;
    }

    public static Complex reciprocal(Complex z) {
        double mod2 = z.real * z.real + z.imaginary * z.imaginary;
        return new Complex(z.real / mod2, (-1) * z.imaginary / mod2);
    }

    public void divide(Complex complex) {
        multiply(Complex.reciprocal(complex));
    }

    public static Complex divide(Complex z, Complex w) {
        return Complex.multiply(z, Complex.reciprocal(w));
    }

    public void exponent(Complex complex) {
        double realExpPart = Math.log(this.mod()) * complex.real - (complex.imaginary * this.arg());
        double imagExpPart = this.arg() * complex.real + complex.imaginary * Math.log(this.mod());
        double modulus = Math.exp(realExpPart);
        real = Math.cos(imagExpPart) * modulus;
        imaginary = Math.sin(imagExpPart) * modulus;
    }

    public static Complex exponent(Complex base, Complex power) {
        double realExpPart = Math.log(base.mod()) * power.real - (power.imaginary * base.arg());
        double imagExpPart = base.arg() * power.real + power.imaginary * Math.log(base.mod());
        double modulus = Math.exp(realExpPart);
        return new Complex(modulus, imagExpPart, false);
    }

    public static Complex sin(Complex complex) {
        Complex epz = Complex.exponent(new Complex(Math.E), new Complex((-1) * complex.imaginary, complex.real));
        Complex enz = Complex.exponent(new Complex(Math.E), new Complex(complex.imaginary, (-1) * complex.real));
        return Complex.divide(Complex.sub(epz, enz), new Complex(0, 2));
    }

    public static Complex cos(Complex complex) {
        Complex epz = Complex.exponent(new Complex(Math.E), new Complex((-1) * complex.imaginary, complex.real));
        Complex enz = Complex.exponent(new Complex(Math.E), new Complex(complex.imaginary, (-1) * complex.real));
        return Complex.multiply(Complex.add(epz, enz), new Complex(0.5, 0));
    }

    /**
     * Converts the complex number to a string with a precision of 3 decimal places
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(new DecimalFormat("0.000#####").format(real));
        sb.append(imaginary < 0 ? " - " : " + ");
        sb.append(new DecimalFormat("0.000#####").format(Math.abs(imaginary)));
        sb.append('i');
        return sb.toString();
    }

    /**
     * Converts the complex number to a string with a precision of 3 decimal places
     *
     * @return
     */
    public String toString3Decimals() {
        StringBuilder sb = new StringBuilder();
        sb.append(real < 0 ? " " : " +");
        sb.append(new DecimalFormat("0.000").format(real));
        sb.append(imaginary < 0 ? " - " : " + ");
        sb.append(new DecimalFormat("0.000").format(Math.abs(imaginary)));
        sb.append('i');
        return sb.toString();
    }

    /**
     * Converts the complex number to a string in modulus-argument form with a precision of 8 decimal places
     *
     * @return
     */
    public String toStringModArg() {
        StringBuilder sb = new StringBuilder();
        sb.append(new DecimalFormat("0.000#####").format(mod()));
        sb.append('*');
        sb.append("e^i");
        sb.append(new DecimalFormat("0.000#####").format(arg()));
        return sb.toString();
    }

    /**
     * Converts the complex number to a string in modulus-argument form with a precision of 3 decimal places
     *
     * @return
     */
    public String toStringModArg3Decimals() {
        StringBuilder sb = new StringBuilder();
        sb.append(new DecimalFormat("0.000").format(mod()));
        sb.append('*');
        sb.append("e^i");
        sb.append(new DecimalFormat("0.000").format(arg()));
        return sb.toString();
    }

    /**
     * Returns if the given complex number is equal to 8 decimal places
     *
     * @param complex a complex number
     * @return true if the both real and imaginary parts are equal to the corresponding one 8 decimal places
     */
    public boolean equalsExact(Complex complex) {
        return Math.abs(imaginary - complex.imaginary) < 0.00000001
                && Math.abs(real - complex.real) < 0.00000001;
    }

    /**
     * Returns if the given complex number is equal to 3 decimal places
     *
     * @param complex a complex number
     * @return true if the both real and imaginary parts are equal to the corresponding one 3 decimal places
     */
    public boolean equals3Decimals(Complex complex) {
        boolean realEq = Math.abs(real - complex.real) < 0.001;
        boolean imaginaryEq = Math.abs(imaginary - complex.imaginary) < 0.001;
        return realEq && imaginaryEq;
    }

    /**
     * Returns if the given complex number is equal to 8 decimal places
     *
     * @param complex a complex number
     * @return true if the both real and imaginary parts are equal to the corresponding one 8 decimal places false otherwise
     */
    @Override
    public boolean equals(Object complex) {
        if (!(complex instanceof Complex)) return false;
        else return equalsExact((Complex) complex);
    }
}
