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

import broadwick.math.Vector;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.math3.random.Well44497b;

/**
 * Sample from a Multivariate Normal (Gaussian) Distribution, a generalization of the one-dimensional (univariate) 
 * normal distribution to higher dimensions. This is a simple wrapper class for the Apache Commons Math 
 * multivariate normal library.
 * <p>
 * References:</p><p>
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Multivariate_normal_distribution"> Multivariatenormal Distribution</a></li>
 * </ul>
 * </p>
 */
@Slf4j
public class MultivariateNormalDistribution implements ContinuousMultivariateDistribution {

    /**
     * Create a normal distribution with a given mean and standard deviation.
     * @param means  the [mathematical] vector of means.
     * @param covariances the covariance matrix.
     */
    public MultivariateNormalDistribution(final double[] means, final double[][] covariances) {
        this.mnd = new org.apache.commons.math3.distribution.MultivariateNormalDistribution(
                new Well44497b(System.currentTimeMillis() * Thread.currentThread().getId()),
                means, covariances);
        
//        Find any real matrix A such that A AT = Σ. When Σ is positive-definite, the Cholesky decomposition is typically used,                                                                                                      
//        Let z = (z1, …, zN)T be a vector whose components are N independent standard normal variates .
//        Let x be μ + Az. This has the desired distribution due to the affine transformation property.
    }

    @Override
    public final Vector sample() {
        return new Vector(mnd.sample());
    }

    /**
     * Reseed the random number generator used. In this case reseeding the distribution has no effect.
     * @param seed the new seed to use.
     */
    @Override
    public void reseed(final int seed) { 
    }
    
    org.apache.commons.math3.distribution.MultivariateNormalDistribution mnd;
}
