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
import lombok.extern.slf4j.Slf4j;

/**
 * Sample from a Uniform Distribution. This is a simple wrapper class for the RNG.getDouble method using a
 * Well19937c random number generator.
 * <p>
 * References:</p><p>
 * <ul>
 * <li><a href="http://mathworld.wolfram.com/UniformDistribution.html"> Normal Distribution</a></li>
 * </ul>
 * </p>
 */
@Slf4j
public class Uniform implements ContinuousDistribution {

    /**
     * Create a uniform distribution with a given min and max. The min and max parameters are checked so that
     * the distributions minimum is the minimum of the parameters and the maximum is the maximum of the parameters.
     * @param min the minimum value of the distribution.
     * @param max the maximum value of the distribution.
     */
    public Uniform(final double min, final double max) {
        this.min = Math.min(min, max);
        this.max = Math.max(min, max);
    }

    @Override
    public double sample() {
        return GENERATOR.getDouble(min, max);
    }

    @Getter
    private final double min;
    @Getter
    private final double max;
    
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
}
