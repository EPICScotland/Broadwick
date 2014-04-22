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
package broadwick.montecarlo.acceptor;

import broadwick.montecarlo.MonteCarloResults;
import broadwick.rng.RNG;

/**
 * A Metropolis-Hastings acceptor.
 */
public class MetropolisHastings implements Acceptor {
    
    /**
     * Create a MetropolisHastings acceptor with a given random seed.
     * @param seed the random seed to use in the acceptor.
     */
    public MetropolisHastings(final int seed) {
        GENERATOR.seed(seed);
    }

    @Override
    public final boolean accept(final MonteCarloResults oldResult, final MonteCarloResults newResult) {

        final double ratio = newResult.getExpectedValue() / oldResult.getExpectedValue();
        return GENERATOR.getDouble() < Math.min(ratio, 1);
    }
    
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
}
