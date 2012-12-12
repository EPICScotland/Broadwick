package broadwick.statistics;

import java.text.DecimalFormat;

/**
 * This class collects data values and calculates their mean and variance.
 */
public class Samples {

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
        sb.append("\t").append(new DecimalFormat("0.0000E0").format(getMean()));
        sb.append("\t").append(new DecimalFormat("0.0000E0").format(getStdDev()));
        return sb.toString();
    }

    private double sum = 0.0; // The sum of all values added to the accumulator.
    private double sumSqr = 0.0; // The sum of all values squared added to the accumulator.
    private double product = 0.0; // The product of all values added to the accumulator.
    private double sumInv = 0.0; // The sum of the 1/value.
    private int total = 0; // The total number of values added to the accumulator.
}
