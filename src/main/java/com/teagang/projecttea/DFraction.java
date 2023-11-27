package com.teagang.projecttea;

import java.math.BigDecimal;
import java.math.MathContext;

public class DFraction {
    private BigDecimal num, den;

    public static final DFraction ZERO = new DFraction("0", "1");
    public static final DFraction ONE = new DFraction("1", "1");

    public DFraction(String num, String den) {
        this.num = new BigDecimal(num);
        this.den = new BigDecimal(den);

        boolean numIsPos = this.num.compareTo(BigDecimal.ZERO) >= 0;
        boolean denIsPos = this.den.compareTo(BigDecimal.ZERO) >= 0;

        if (numIsPos == denIsPos) {
            this.num = this.num.abs();
        } else {
            this.num = this.num.abs().negate();
        }

        this.den = this.den.abs();
        reduce();
    }

    public boolean isZero() {
        return this.num.compareTo(BigDecimal.ZERO) == 0;
    }

    private BigDecimal gcd(BigDecimal a, BigDecimal b) {
        if (b.compareTo(BigDecimal.ZERO) == 0) {
            return a;
        }
        return gcd(b, a.remainder(b));
    }

    private void reduce() {
        if (this.num.equals(BigDecimal.ZERO) || this.den.equals(BigDecimal.ONE)) {
            return;
        }

        try {
            int a = this.num.intValueExact();
            int b = this.den.intValueExact();
        } catch (ArithmeticException e) {
            this.num = this.num.divide(this.den, MathContext.DECIMAL128);
            this.den = BigDecimal.ONE;
            return;
        }

        BigDecimal gcdVal = gcd(this.num.abs(), this.den);
        this.num = this.num.divide(gcdVal, MathContext.DECIMAL128);
        this.den = this.den.divide(gcdVal, MathContext.DECIMAL128);
    }

    public DFraction multiply(DFraction df) {
        String newNum = this.num.multiply(df.num).toPlainString();
        String newDen = this.den.multiply(df.den).toPlainString();
        return new DFraction(newNum, newDen);
    }

    public DFraction divide(DFraction df) {
        String newNum = this.num.multiply(df.den).toPlainString();
        String newDen = this.den.multiply(df.num).toPlainString();
        return new DFraction(newNum, newDen);
    }

    public DFraction minus(DFraction df) {
        BigDecimal op1 = this.num.multiply(df.den);
        BigDecimal op2 = this.den.multiply(df.num);
        String newNum = op1.subtract(op2).toPlainString();
        String newDen = this.den.multiply(df.den).toPlainString();
        return new DFraction(newNum, newDen);
    }

    @Override
    public String toString() {
        if (this.num.equals(BigDecimal.ZERO))
            return "0";

        if (this.den.equals(BigDecimal.ONE))
            return this.num.toString();

        try {
            int a = this.num.intValueExact();

            if (Integer.toString(a).toLowerCase().contains("e") || this.den.toString().toLowerCase().contains("e")
                    || this.num.toPlainString().length() > 7 || this.den.toPlainString().length() > 7) {
                throw new ArithmeticException();
            }
        } catch (ArithmeticException e) {
            return this.num.divide(this.den, MathContext.DECIMAL128).toString();
        }

        return this.num.toPlainString() + "/" + this.den.toPlainString();
    }
}
