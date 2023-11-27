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

                // Parse the numerator and denominator as doubles
                double numerator = Double.parseDouble(parts[0]);
                double denominator = Double.parseDouble(parts[1]);

                // Perform the division
                result[i][j] = numerator / denominator;
            }
        }

        return result;
    }

    private DFraction[][] parseFinalData(String[][] data) {
        int n = data.length;
        DFraction[][] result = new DFraction[n][n];

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (data[i][j].contains("/")) {
                    String[] part = data[i][j].split("/");
                    result[i][j] = new DFraction(part[0], part[1]);
                    continue;
                }

                result[i][j] = new DFraction(data[i][j], "1");
            }
        }

        return result;
    }

    private DFraction[][] augmentMatrix(DFraction[][] matrix) {
        int n = matrix.length;
        DFraction[][] augmentedMatrix = new DFraction[n][2 * n];

        // Augment with identity matrix
        for (int i = 0; i < n; i++) {
            System.arraycopy(matrix[i], 0, augmentedMatrix[i], 0, n);

            for (int j = 0; j < n; j++) {
                augmentedMatrix[i][j + n] = (i == j) ? DFraction.ONE : DFraction.ZERO;
            }
        }

        return augmentedMatrix;
    }

    private String[][] invertMatrix(DFraction[][] matrix) {
        int n = matrix.length;
        DFraction[][] augmentedMatrix = augmentMatrix(matrix);

        // Apply Gaussian elimination to get row-echelon form
        for (int i = 0; i < n; i++) {
            // Find pivot
            if (augmentedMatrix[i][i].isZero()) {
                for (int j = i + 1; j < n; j++) {
                    if (augmentedMatrix[j][i].isZero())
                        continue;

                    DFraction[] temp = new DFraction[2 * n - i];
                    System.arraycopy(augmentedMatrix[i], i, temp, 0, 2 * n - i);
                    System.arraycopy(augmentedMatrix[j], i, augmentedMatrix[i], i, 2 * n - i);
                    System.arraycopy(temp, 0, augmentedMatrix[j], i, 2 * n - i);
                }
            }

            // Make the diagonal element 1
            DFraction diagonalElement = augmentedMatrix[i][i];
            for (int j = i; j < 2 * n; j++) {
                augmentedMatrix[i][j] = augmentedMatrix[i][j].divide(diagonalElement);
            }

            // Make the elements above and below the diagonal 0
            for (int k = 0; k < n; k++) {
                if (k != i && !augmentedMatrix[k][i].isZero()) {
                    DFraction factor = augmentedMatrix[k][i];
                    for (int j = i; j < 2 * n; j++) {
                        DFraction operand = factor.multiply(augmentedMatrix[i][j]);
                        augmentedMatrix[k][j] = augmentedMatrix[k][j].minus(operand);
                    }
                }
            }
        }

        // Extract the inverted matrix
        String[][] invertedMatrix = new String[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                invertedMatrix[i][j] = augmentedMatrix[i][j + n].toString();
            }
        }

        return invertedMatrix;
    }

    public String[][] solveInverse(String[][] data) throws SingularMatrixException {
        double[][] parsedData = parseData(data);
        RealMatrix matrix = MatrixUtils.createRealMatrix(parsedData);

        if (new LUDecomposition(matrix).getDeterminant() == 0) {
            throw new SingularMatrixException();
        }

        DFraction[][] finalParsedData = parseFinalData(data);
        return invertMatrix(finalParsedData);
    }
}
