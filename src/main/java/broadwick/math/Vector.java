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

/**
 * Implementation of mathematical vector (not to be confused with the data structure). It is perhaps best to think of
 * this as a row-vector as several underlying operations (such as multiplying by a matrix) are implemented by casting
 * the vector into a 1D (row) matrix which can be transposed etc internally.
 */
public class Vector {

    /**
     * Create a vector of a given dimension. Each element will be set to 0.
     * @param dimension the dimension of the vector.
     */
    public Vector(final int dimension) {
        data = new double[dimension];

        for (int i = 0; i < dimension; i++) {
            data[i] = 0.0;
        }
    }

    /**
     * Copy constructor.
     * @param v the vector to be copied.
     */
    public Vector(final Vector v) {
        this(v.data);
    }

    /**
     * Copy constructor.
     * @param data an array that will be copied.
     */
    public Vector(final double[] data) {
        this.data = new double[data.length];

        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    /**
     * Get the element at the given index.
     * @param i the vector element to be found.
     * @return the vector element
     */
    public double element(final int i) {
        return data[i];
    }

    /**
     * Set the value of the vector at a given index.
     * @param i     the coordinate of the vector to be set
     * @param value the value of the vector element.
     * @return the updated value.
     */
    public double setEntry(final int i, final double value) {
        data[i] = value;
        return data[i];
    }

    /**
     * Return a copy of the vector as an array.
     * @return a copy of the vector as an array.
     */
    public double[] toArray() {
        return data.clone();
    }

    /**
     * Obtain the length, i.e. the number of components of this vector.
     * @return the number of elements in the vector.
     */
    public int length() {
        return data.length;
    }

    /**
     * Add a vector to this one.
     * @param v the vector to be added to this one.
     * @return this (updated) vector.
     */
    public Vector add(final Vector v) {
        Vector v1 = new Vector(data);

        for (int i = 0; i < data.length; i++) {
            v1.setEntry(i, v1.element(i) + v.element(i));
        }
        return v1;
    }

    /**
     * Subtract a vector from this one.
     * @param v the vector to be subtracted from this one.
     * @return this (updated) vector.
     */
    public Vector subtract(final Vector v) {
        Vector v1 = new Vector(data);

        for (int i = 0; i < data.length; i++) {
            v1.setEntry(i, v1.element(i) - v.element(i));
        }
        return v1;
    }

    /**
     * Calculate the dot product of this vector with v.
     * @param v the vector to be dotted.
     * @return this (updated) vector.
     */
    public double multiply(final Vector v) {
        double d = 0.0;

        for (int i = 0; i < data.length; i++) {
            d += data[i] * v.element(i);
        }
        return d;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("(");

        for (int i = 0; i < data.length; i++) {
            sb.append(data[i]).append(",");
        }
        sb.deleteCharAt(sb.length() - 1);
        sb.append(")");
        return sb.toString();
    }

    private final double[] data;
}
