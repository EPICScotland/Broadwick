/*
 * Copyright 2013 University of Glasgow.
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
package broadwick.statistics;

import java.io.Serializable;
import java.text.DecimalFormat;

/**
 * This class collects data values and calculates their mean and variance.
 */
public class Samples implements Serializable {

    /**
     * Empty object where the internal values are initialised to 0. Getting any statistics from this object will result
     * in a divide by zero exceptions since there are zero elements in the accumulator.
     */
    public Samples() {
        sum = 0.0;
        sumSqr = 0.0;
        product = 0.0;
        sumInv = 0.0;
        total = 0;
        max = Double.NEGATIVE_INFINITY;
        min = Double.POSITIVE_INFINITY;
    }

    /**
     * Initialise the internal values of the accumulator with the argument given.
     * @param value the value to initialise the internal variables.
     */
    public Samples(final double value) {
        sum = value;
        sumSqr = value * value;
        product = value;
        sumInv = 1.0 / value;
        max = Math.max(max, value);
        min = Math.min(min, value);
        total = 1;
    }

    /**
     * Add an observable (value) to the accumulator.
     * @param value add the value to the accumulated values.
     * @return the current accumulator object.
     */
    public final Samples add(final double value) {
        sum += value;
        sumSqr += (value * value);
        product = (product == 0) ? value : product * value;
        sumInv += (1.0 / value);
        max = Math.max(max, value);
        min = Math.min(min, value);
        total++;
        return this;
    }

    /**
     * Add the contents of anther accumulator to this accumulator.
     * @param value add the value to the accumulated values.
     * @return the current accumulator object.
     */
    public final Samples add(final Samples value) {
        sum += value.sum;
        sumSqr += value.sumSqr;
        product += value.product;
        sumInv += value.sumInv;
        total += value.getSize();
        max = Math.max(max, value.getMax());
        min = Math.min(min, value.getMin());
        return this;
    }

    /**
     * Obtain the mean of the set of observables in the accumulator.
     * @return double the mean in the observed values.
     */
    public final double getMean() {
        final int tot = (total == 0) ? 1 : total;
        return sum / tot;
    }

    /**
     * Obtain the root mean square of the set of observables in the accumulator.
     * @return double the root mean square in the observed values.
     */
    public final double getRootMeanSquare() {
        final int tot = (total == 0) ? 1 : total;
        return Math.sqrt(sumSqr / tot);
    }

    /**
     * Obtain the geometric mean of the set of observables in the accumulator.
     * @return double the geometric mean in the observed values.
     */
    public final double getGeometricMean() {
        final int tot = (total == 0) ? 1 : total;
        return Math.pow(product, 1.0 / tot);
    }

    /**
     * Obtain the harmonic mean of the set of observables in the accumulator.
     * @return double the harmonic mean in the observed values.
     */
    public final double getHarmonicMean() {
        final double inv = (sumInv == 0) ? 1 : sumInv;
        return total / inv;
    }

    /**
     * Get the sum of the values held in the accumulator.
     * @return the sum of all the values added to the accumulator.
     */
    public final double getSum() {
        return sum;
    }

    /**
     * Get the number of items added to the Accumulator.
     * @return the number of items added to the Accumulator.
     */
    public final int getSize() {
        return total;
    }
    
        /**
     * Obtain the maximum value in the of the set of observables.
     * @return double the mean in the observed values.
     */
    public final double getMax() {
        return max;
    }
    
        /**
     * Obtain the minimum value in the of the set of observables.
     * @return double the mean in the observed values.
     */
    public final double getMin() {
        return min;
    }

    /**
     * Default sample variance implementation based on the second moment \f$ M_n^{(2)} \f$ moment<2>, mean and count.
     * \f[ \sigma_n^2 = M_n^{(2)} - \mu_n^2. \f] where \f[ \mu_n = \frac{1}{n} \sum_{i = 1}^n x_i. \f] is the estimate
     * of the sample mean and \f$n\f$ is the number of samples.
     * @return double the variance of the observed values.
     */
    public final double getVariance() {
        final int tot = (total == 0) ? 1 : total;
        final double mn = sum / tot;

        return (sumSqr / tot - (mn * mn)) / tot;
    }

    /**
     * Obtain the standard deviation (error in the mean) of the set of observeables in the accumulator.
     * @return double the standard deviation in the observed values.
     */
    public final double getStdDev() {
        return Math.sqrt(this.getVariance());
    }

    /**
     * Clear the contents of the accumulator. Sets all the internal counts back to zero.
     */
    public final void clear() {
        sum = 0.0;
        sumSqr = 0.0;
        product = 0.0;
        sumInv = 0.0;
        total = 0;
    }

    /**
     * Get a tab delimited string of the mean and standard deviation of the values in the accumulator.
     * @return a string in the form "\tmean\tdev"
     */
    public final String getSummary() {
        final StringBuilder sb = new StringBuilder(10);
        sb.append("\t").append(new DecimalFormat("0.###E0").format(getMean()));
        sb.append("\t").append(new DecimalFormat("0.###E0").format(getStdDev()));
        return sb.toString();
    }

    private double sum = 0.0; // The sum of all values added to the accumulator.
    private double sumSqr = 0.0; // The sum of all values squared added to the accumulator.
    private double product = 0.0; // The product of all values added to the accumulator.
    private double sumInv = 0.0; // The sum of the 1/value.
    private int total = 0; // The total number of values added to the accumulator.
    private double max = Double.NEGATIVE_INFINITY;
    private double min = Double.POSITIVE_INFINITY;
    private static final long serialVersionUID = -555916427676926813L;
}
