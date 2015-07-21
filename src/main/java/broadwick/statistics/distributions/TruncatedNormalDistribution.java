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
package broadwick.statistics.distributions;

import broadwick.rng.RNG;
import lombok.Getter;

/**
 * An implementation of the truncated normal distribution, i.e. a normal distribution whose value is bounded either
 * above, below or both.
 * <p>
 * References:</p><p>
 * <ul>
 * <li><a href="http://mathworld.wolfram.com/NormalDistribution.html"> Normal Distribution</a></li>
 * </ul>
 */
public class TruncatedNormalDistribution implements ContinuousDistribution {

    /**
     * Create an instance of the normal distribution distribution truncated to given limits.
     * @param mean the mean of the normal distribution.
     * @param sd   the standard deviation of the distribution.
     * @param lb   the lower bound of the distribution, no values lower than this will be returned.
     * @param ub   the upper bound of the distribution, no values higher1 than this will be returned.
     */
    public TruncatedNormalDistribution(final double mean, final double sd,
                                       final double lb, final double ub) {

        if (mean == Double.NaN || sd == Double.NaN
            || lb == Double.NaN || ub == Double.NaN) {
            throw new IllegalArgumentException("Invalid argument: TruncatedNormalDistribution cannot take NaN as argument");
        }

        if (lb > ub) {
            throw new IllegalArgumentException("Invalid argument: lower bound greater than upper bound");
        }

        this.mean = mean;
        this.sd = sd;
        this.lower = lb;
        this.upper = ub;
        this.generator = new RNG(RNG.Generator.Well19937c);
    }

    @Override
    public double sample() {

        // Sample using the method of C.P. Robert (doi: 10.1007/BF00143942, arXiv:0907.4010 [stat.CO])
        double x = Double.NaN;
        double rho = 0.0;
        double u = generator.getDouble();

        while (u > rho) {
            double z = generator.getDouble(lower, upper);
            if (0 > lower && 0 < upper) {
                rho = Math.exp(-z * z / 2.0);
            } else if (upper < 0) {
                rho = Math.exp(((upper * upper) - (z * z)) / 2.0);
            } else if (0 < lower) {
                rho = Math.exp(((lower * lower) - (z * z)) / 2.0);
            }

            u = generator.getDouble();
            x = z;
        }

        return x;
    }

    public double rejectionSample() {
        // use a simple rejection sampling
        final Normal dist = new Normal(mean, sd);
        double val = dist.sample();

        while (val < lower || val > upper) {
            val = dist.sample();
        }
        return val;
    }

    @Getter
    private double mean;
    @Getter
    private double sd;
    private double lower;
    private double upper;
    private RNG generator;
}
