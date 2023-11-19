package com.teagang.projecttea;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularMatrixException;

public class Solver {

    private double[][] parseData(String[][] data) {
        double[][] result = new double[data.length][data[0].length];

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (!data[i][j].contains("/")) {
                    result[i][j] = Double.parseDouble(data[i][j]);
                    continue;
                }

                String[] parts = data[i][j].split("/");

                try {
                    // Parse the numerator and denominator as doubles
                    double numerator = Double.parseDouble(parts[0]);
                    double denominator = Double.parseDouble(parts[1]);

                    // Perform the division
                    result[i][j] = numerator / denominator;
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing numerator or denominator as doubles.");
                }
            }
        }

        return result;
    }

    public double[][] solveInverse(String[][] data) throws SingularMatrixException {
        double[][] parsedData = parseData(data);
        RealMatrix inputMatrix = MatrixUtils.createRealMatrix(parsedData);
        RealMatrix inverse = new LUDecomposition(inputMatrix).getSolver().getInverse();
        return inverse.getData();
    }
}
