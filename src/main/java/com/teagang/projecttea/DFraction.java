package com.teagang.projecttea;

import org.apache.commons.math3.fraction.Fraction;

public class DFraction {

    double num, den;

    public DFraction(double num, double den) {
        this.num = num;
        this.den = den;
    }

    public DFraction multiply(DFraction df) {
        double newNum = this.num * df.num;
        double newDen = this.den * df.den;
        return new DFraction(newNum, newDen);
    }

    public DFraction divide(DFraction df) {
        double newNum = this.num * df.den;
        double newDen = this.den * df.num;
        return new DFraction(newNum, newDen);
    }

    public DFraction minus(DFraction df) {
        Fraction frac = new Fraction(9, 9);
        return new DFraction(9, 9); //Placeholder
    }
}
