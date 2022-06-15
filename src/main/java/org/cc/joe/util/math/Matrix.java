package org.cc.joe.util.math;

import static java.lang.Math.pow;

public record Matrix(double[][] data) {

    public double get(final int row, final int column) {
        return data[row][column];
    }

    public Matrix inverse() {
        return new Matrix(inverse(data));
    }

    private static double determinant(final double[][] matrix) {
        if (matrix.length == 2) {
            return matrix[0][0] * matrix[1][1] - matrix[0][1] * matrix[1][0];
        }
        var determinant = 0.0;
        for (var i = 0; i < matrix[0].length; i++) {
            determinant += pow(-1, i) * matrix[0][i] * determinant(subMatrix(matrix, 0, i));
        }
        return determinant;
    }

    private static double[][] inverse(final double[][] matrix) {
        final var inverse = new double[matrix.length][matrix.length];
        for (var i = 0; i < matrix.length; i++) {
            for (var j = 0; j < matrix[i].length; j++) {
                inverse[i][j] = pow(-1, i + j) * determinant(subMatrix(matrix, i, j));
            }
        }
        final var inverseDeterminant = 1.0 / determinant(matrix);
        for (var i = 0; i < inverse.length; i++) {
            for (var j = 0; j <= i; j++) {
                final var tmp = inverse[i][j];
                inverse[i][j] = inverse[j][i] * inverseDeterminant;
                inverse[j][i] = tmp * inverseDeterminant;
            }
        }
        return inverse;
    }

    private static double[][] subMatrix(final double[][] matrix, final int row, final int column) {
        final var result = new double[matrix.length - 1][matrix.length - 1];
        for (var i = 0; i < matrix.length; i++) {
            for (var j = 0; i != row && j < matrix[i].length; j++) {
                if (j != column) {
                    result[i < row ? i : i - 1][j < column ? j : j - 1] = matrix[i][j];
                }
            }
        }
        return result;
    }
}
