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
package broadwick.statistics.distributions;

import broadwick.rng.RNG;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Sample fro a Normal (Gaussian) Distribution. This is a simple wrapper class for the RNG.getGaussian method using a
 * Well19937c random number generator.
 * <p>
 * References:</p><p>
 * <ul>
 * <li><a href="http://mathworld.wolfram.com/NormalDistribution.html"> Normal Distribution</a></li>
 * </ul>
 * </p>
 */
@Slf4j
public class Normal implements ContinuousDistribution {

    /**
     * Create a normal distribution of zero mean and unit standard deviation.
     */
    public Normal() {
    }

    /**
     * Create a normal distribution with a given mean and standard deviation.
     * @param mean   the mean vlaue of the distribution.
     * @param stdDev the stand deviation of the distribution.
     */
    public Normal(final double mean, final double stdDev) {
        this.mean = mean;
        this.stdDev = stdDev;
    }

    @Override
    public final double sample() {
        return GENERATOR.getGaussian(mean, stdDev);
    }

    /**
     * Reseed the random number generator used.
     * @param seed the new seed to use.
     */
    @Override
    public final void reseed(final int seed) {
        GENERATOR.seed(seed);
    }

    @Getter
    @Setter
    private double mean = 0.0;
    @Getter
    @Setter
    private double stdDev = 1.0;
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
}
