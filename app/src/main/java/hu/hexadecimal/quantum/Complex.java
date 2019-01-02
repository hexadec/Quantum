package hu.hexadecimal.quantum;

import java.text.DecimalFormat;

public class Complex {

    protected double real = 0;
    protected double imaginary = 0;

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
        if (Math.abs(real) < 0.0000001 && Math.abs(imaginary) < 0.0000001) return 0;
        if (Math.abs(real) < 0.0000001) return Math.PI / 2 * imaginary < 0 ? -1 : 1;
        if (Math.abs(imaginary) < 0.0000001) return real < 0 ? Math.PI : 0;
        return Math.atan(imaginary / real);
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
        imaginary /= -mod2;
    }

    public static Complex reciprocal(Complex z) {
        double mod2 = z.real * z.real + z.imaginary * z.imaginary;
        return new Complex(z.real / mod2, z.imaginary / -mod2);
    }

    public void divide(Complex complex) {
        multiply(Complex.reciprocal(complex));
    }

    public static Complex divide(Complex z, Complex w) {
        return Complex.multiply(z, Complex.reciprocal(w));
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(real);
        sb.append(imaginary < 0 ? " " : " + ");
        sb.append(imaginary);
        sb.append('i');
        return sb.toString();
    }

    public String toString3Decimals() {
        StringBuilder sb = new StringBuilder();
        sb.append(real < 0 ? " " : " +");
        sb.append(new DecimalFormat("0.000").format(real));
        sb.append(imaginary < 0 ? " - " : " + ");
        sb.append(new DecimalFormat("0.000").format(Math.abs(imaginary)));
        sb.append('i');
        return sb.toString();
    }

    public String toStringModArg() {
        StringBuilder sb = new StringBuilder();
        sb.append(new DecimalFormat("0.000").format(mod()));
        sb.append('*');
        sb.append("e^i");
        sb.append(new DecimalFormat("0.000").format(arg()));
        return sb.toString();
    }

    public boolean equalsExact(Complex complex) {
        return Math.abs(imaginary - complex.imaginary) < 0.0000001
                && Math.abs(real - complex.real) < 0.0000001;
    }

    public boolean equals3Decimals(Complex complex) {
        boolean realEq = Math.abs(real - complex.real) < 0.001;
        boolean imaginaryEq = Math.abs(imaginary - complex.imaginary) < 0.001;
        return realEq && imaginaryEq;
    }
}
