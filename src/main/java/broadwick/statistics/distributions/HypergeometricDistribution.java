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

/**
 *
 * <p>
 * The hypergeometric distribution applies to sampling without replacement from a finite population whose elements can
 * be classified into two mutually exclusive categories like Pass/Fail. As random selections are made from the
 * population, each subsequent draw decreases the population causing the probability of success to change with each
 * draw.
 * <p>
 * @see <a href="http://en.wikipedia.org/wiki/Hypergeometric_distribution">Hypergeometric distribution (Wikipedia)</a>
 * @see <a href="http://mathworld.wolfram.com/HypergeometricDistribution.html">Hypergeometric distribution
 * (MathWorld)</a>
 */
public class HypergeometricDistribution implements DiscreteDistribution {

    /**
     * A hypergeometric distribution gives the distribution of the number of successes in \(n\) draws from a population
     * of size \(N\) containing \(n_{succ}\) successes. \(N,n,n_{succ}\) can be any integers such that \(N\ge n>0\) and
     * \(N\ge n_{succ} \ge 0\).
     * @param popSize      the population size, \(N\).
     * @param sampleSize   the number of draws, \(n\).
     * @param numSuccesses the number of successes, \(n_{succ}\).
     */
    public HypergeometricDistribution(final int popSize, final int sampleSize, final int numSuccesses) {
        if (!(popSize >= sampleSize)) {
            throw new IllegalArgumentException(String.format("The population size (%d) MUST be greater than or equal to the sample size (%d).", popSize, sampleSize));
        }
        if (!(popSize >= numSuccesses)) {
            throw new IllegalArgumentException(String.format("The population size (%d) MUST be greater than or equal to the number of successes (%d).", popSize,numSuccesses));
        }
        if (!(sampleSize > 0)) {
            throw new IllegalArgumentException(String.format("The sample size (%d) MUST be greater than 0.", sampleSize));
        }
        if (!(numSuccesses >= 0)) {
            throw new IllegalArgumentException(String.format("The number of successes (%d) MUST be greater than or equal to 0", numSuccesses));
        }

        dist = new org.apache.commons.math3.distribution.HypergeometricDistribution(popSize,
                                                                                    numSuccesses,
                                                                                    sampleSize);
    }

    @Override
    public int sample() {
        return dist.sample(); //.inverseCumulativeProbability(RNG.getDouble());
    }

    /**
     * {@inheritDoc}
     * <p>
     * For population size {@code N}, number of successes {@code m}, and sample size {@code n}, the mean is
     * {@code n * m / N}.
     * @return
     */
    @Override
    public final double getMean() {
        return dist.getNumericalMean();
    }

    /**
     * {@inheritDoc}
     * <p>
     * For population size {@code N}, number of successes {@code m}, and sample size {@code n}, the variance is
     * {@code [n * m * (N - n) * (N - m)] / [N^2 * (N - 1)]}.
     * @return
     */
    @Override
    public final double getVariance() {
        return dist.getNumericalVariance();
    }

    private final org.apache.commons.math3.distribution.HypergeometricDistribution dist;
}
