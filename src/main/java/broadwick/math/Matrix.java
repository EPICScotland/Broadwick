/*
 * Copyright 2015 University of Glasgow.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package broadwick.math;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;

/**
 * Implementation of real valued Matrix.
 */
public class Matrix {

    /**
     * Create a matrix from the internal representation of the matrix (copy construtor). This constructor
     * is intentionally private to hide the internal representation of the matrix.
     * @param data the data to be copied.
     */
    private Matrix(final Array2DRowRealMatrix data) {
        this.numRows = data.getRowDimension();
        this.numCols = data.getColumnDimension();
        this.data = (Array2DRowRealMatrix) data.copy();
    }

    /** 
     * Create a matrix using the data in an array (copy constructor). This constructor will copy the contents
     * of the argument.
     * @param data the data array to be copied.
     */
    private Matrix(final double[][] data) {
        this.data = new Array2DRowRealMatrix(data);
        this.numRows = this.data.getRowDimension();
        this.numCols = this.data.getColumnDimension();
    }

    /**
     * Create a matrix with the given dimensions. 
     * @param rowDimension the number of rows in the matrix.
     * @param columnDimension the number of columns in the matrix.
     */
    public Matrix(final int rowDimension, final int columnDimension) {
        this.numRows = rowDimension;
        this.numCols = columnDimension;
        data = new Array2DRowRealMatrix(rowDimension, columnDimension);

        for (int i = 0; i < rowDimension; i++) {
            for (int j = 0; j < columnDimension; j++) {
                data.setEntry(i, j, 0.0);
            }
        }
    }

    /**
     * Get the contents of the matrix. The (i,j) element of the matrix is returned where i is the row index and j 
     * is the column index.
     * @param row the row index.
     * @param column the column index.
     * @return the entry at eh requiered index.
     */
    public final double element(final int row, final int column) {
        return data.getEntry(row, column);
    }

    /**
     * Set the matrix element at a given point.
     * @param row the row index of the new element.
     * @param column the column element of the new element.
     * @param value the value to be set.
     * @return the new matrix element at the given coordinates.
     */
    public final double setEntry(final int row, final int column, final double value) {
        data.setEntry(row, column, value);
        return data.getEntry(row, column);
    }

    /**
     * Return an array representation of this matrix.
     * @return a double[][] containing the values of this matrix.
     */
    public final double[][] toArray() {
        return data.getData();
    }

    /**
     * Create a copy of the matrix. A new matrix is returned with the same contents of this one.
     * @return a copy of this matrix.
     */
    public final Matrix copy() {
        return new Matrix(data);
    }

    /**
     * Get the number of rows in the matrix.
     * @return the number of rows.
     */
    public final int rows() {
        return numRows;
    }

    /**
     * Get the number of columns in the matrix.
     * @return the number of columns.
     */
    public final int columns() {
        return numCols;
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();

        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                sb.append(data.getEntry(i, j)).append(",");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Get the inverse of the matrix. This will return a new matrix object that is the inverse.
     * @return a Matrix object that is the inverse of this matrix.
     */
    public final Matrix inverse() {
        return new Matrix(MatrixUtils.inverse(data).getData());
    }

    /**
     * Get the transpose of the matrix. This will return a new matrix object that is the transpose.
     * @return a Matrix object that is the transpose of this matrix.
     */
    public final Matrix transpose() {
        return new Matrix(data.transpose().getData());
    }

    /**
     * Returns the result of subtracting m from this.
     * @param m matrix to be subtracted
     * @return this-m
     */
    public final Matrix subtract(final Matrix m) {
        return new Matrix(data.subtract(m.data));
    }

    /**
     * Returns the result of postmultiplying this by m.
     * @param m matrix to postmultiply by
     * @return this*m
     */
    public final Matrix multiply(final Matrix m) {
        return new Matrix(data.multiply(m.data));
    }
    
        /**
     * Returns the result of postmultiplying this by a scalar.
     * @param d the scalar with which we will multiply this matrix
     * @return this*d
     */
    public final Matrix multiply(final double d) {
        return new Matrix(data.scalarMultiply(d).getData());
    }

    /**
     * Returns the result of multiplying this by the vector v.
     * @param v the vector.
     * @return this*v
     */
    public final Vector multiply(final Vector v) {
        return new Vector(data.operate(v.toArray()));
    }

    /**
     * Returns the (row) vector result of premultiplying this by the vector v.
     * @param v the row vector to premultiply by
     * @return v*this
     */
    public final Vector preMultiply(final Vector v) {
        return new Vector(data.preMultiply(v.toArray()));
    }

    private final int numRows;
    private final int numCols;
    private final Array2DRowRealMatrix data;
}
