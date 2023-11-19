package com.teagang.projecttea;

import org.apache.commons.math3.fraction.Fraction;
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

    private double[][] augmentMatrix(double[][] matrix) {
        int n = matrix.length;
        double[][] augmentedMatrix = new double[n][2 * n];

        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, augmentedMatrix[i], 0, n);
            augmentedMatrix[i][i + n] = 1.0; // Augment with identity matrix
        }

        return augmentedMatrix;
    }

    private double[][] invertMatrix(double[][] matrix) {
        int n = matrix.length;
        double[][] augmentedMatrix = augmentMatrix(matrix);

        // Apply Gaussian elimination to get row-echelon form
        for (int i = 0; i < n; i++) {
            // Make the diagonal element 1
            double diagonalElement = augmentedMatrix[i][i];
            for (int j = 0; j < 2 * n; j++) {
                augmentedMatrix[i][j] /= diagonalElement;
            }

            // Make the elements above and below the diagonal 0
            for (int k = 0; k < n; k++) {
                if (k != i) {
                    double factor = augmentedMatrix[k][i];
                    for (int j = 0; j < 2 * n; j++) {
                        augmentedMatrix[k][j] -= factor * augmentedMatrix[i][j];
                    }
                }
            }
        }

        // Extract the inverted matrix
        double[][] invertedMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            System.arraycopy(augmentedMatrix[i], n, invertedMatrix[i], 0, n);
        }

        return invertedMatrix;
    }

    public String[][] solveInverse(String[][] data) throws SingularMatrixException {
        double[][] parsedData = parseData(data);
        RealMatrix matrix = MatrixUtils.createRealMatrix(parsedData);

        if (new LUDecomposition(matrix).getDeterminant() == 0) {
            throw new SingularMatrixException();
        }

        double[][] inverse = invertMatrix(parsedData);
        String[][] result = new String[data.length][data[0].length];

        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                Fraction fraction = new Fraction(inverse[i][j]);

                if (fraction.doubleValue() == inverse[i][j] && fraction.doubleValue() != 0) {
                    result[i][j] = fraction.toString();
                } else {
                    result[i][j] = Double.toString(inverse[i][j]);
                }
            }
        }

        return result;
    }
}
