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

import broadwick.math.Matrix;
import broadwick.math.Vector;

/**
 * Sample from a truncated Multivariate Normal (Gaussian) Distribution, i.e. a normal distribution whose value is
 * bounded either above, below or both.
 * <p>
 * References:</p><p>
 * <ul>
 * <li><a href="http://en.wikipedia.org/wiki/Multivariate_normal_distribution"> Multivariatenormal Distribution</a></li>
 * </ul>
 * </p>
 */
public class TruncatedMultivariateNormalDistribution implements ContinuousMultivariateDistribution {

    /**
     * Create an instance of a multivariate distribution that is bounded.
     * @param means       the [mathematical] vector of the means of each variable.
     * @param covariances the [mathematical] matrix of the covariances of each variable.
     * @param lb          the [mathematical] vector of the lower bounds of each variable.
     * @param ub          the [mathematical] vector of the upper bounds of each variable.
     */
    public TruncatedMultivariateNormalDistribution(final Vector means, final Matrix covariances,
                                                   final Vector lb, final Vector ub) {
        
        if (means.length() != lb.length() || lb.length() != ub.length() 
            || ub.length() != covariances.rows() || covariances.rows() != covariances.columns()) {
                throw new IllegalArgumentException("The lengths of the input vectors must be equal and the covariances matirx must be square with the same size as the input vectors.");
        }
        
        for (int i=0; i< means.length(); i++) {
            if (means.element(i) < lb.element(i) || means.element(i) > ub.element(i)) {
                throw new IllegalArgumentException("The means of the distribution must lie between the lower and upper bounds");
            }
            
            if (lb.element(i) > ub.element(i)) {
                throw new IllegalArgumentException("The lower bound of the distribution must be less than the upper bound");
            }
        }
        
        this.means = means;
        this.covariances = covariances;
        this.upperBounds = ub;
        this.lowerBounds = lb;
        this.n = means.toArray().length;
    }

    @Override
    public Vector sample() {

        Vector proposal = new Vector(n);

        // Gibbs sampler of Christian Robert (arxiv:0907.4010v1 [stat.CO])
        // Robert, C.P, "Simulation of truncated normal variables",
        //   Statistics and Computing, pp. 121-125 (1995).
        Matrix covInv = covariances.inverse();
        for (int i = 0; i < n; i++) {
            // get the (n-1) vector from the i-th column of the covariances matrix, removing the i-th row.
            Matrix sigmaI = new Matrix(n - 1, 1);
            for (int j = 0; j < n - 1; j++) {
                if (j != i) {
                    sigmaI.setEntry(j, 0, covariances.element(j, i));
                }
            }

            // Get the inverse of the (n-1)(n-1) matrix obtained from the covariance matrix removing the
            // ith row and column.
            Matrix sigmaIinv = new Matrix(n - 1, n - 1);
            for (int j = 0; j < n - 1; j++) {
                if (j != i) {
                    for (int k = 0; k < n - 1; k++) {
                        if (k != i) {
                            sigmaIinv.setEntry(j, k, covInv.element(j, k));
                        }
                    }
                }
            }

            // x_i is the (n-1) vector of components not being updated at this iteration.
            Matrix xI = new Matrix(1, n - 1);
            Matrix muI = new Matrix(1, n - 1);
            for (int j = 0; j < n - 1; j++) {
                if (j != i) {
                    xI.setEntry(0, j, means.element(j));
                    muI.setEntry(0, j, means.element(j));
                }
            }

            // mui is E(xi|x_i)
            //  mui = mu(i) + sigmai_i * sigma_i_iInv * (x_i - mu_i);
            Matrix diff = xI.transpose().subtract(muI.transpose());
            double mui = means.element(i) + sigmaI.transpose().multiply(sigmaIinv).multiply(diff).element(0, 0);
            double s2i = covariances.element(i, i) - sigmaI.transpose().multiply(sigmaIinv).multiply(sigmaI).element(0, 0);

            // now draw from the 1-d normal truncated to [lb, ub]
            TruncatedNormalDistribution dist = new TruncatedNormalDistribution(mui, Math.sqrt(s2i),
                                                                               lowerBounds.element(i),
                                                                               upperBounds.element(i));
            proposal.setEntry(i, dist.sample());

        }

        /*
         // Use rejection sampling to find the mvn variate.
         boolean proposedStepOutOfBounds;
         int attempts = 0;
         Vector proposal = new Vector(n);
         do {
         proposedStepOutOfBounds = false;
         proposal = mvn.sample();

         for (int i = 0; i < n; i++) {
         if (proposal.element(i) < lowerBounds.element(i)
         || proposal.element(i) > upperBounds.element(i)
         || Math.abs(proposal.element(i)) < 0.00001) {
         proposedStepOutOfBounds = true;
         System.out.println(String.format("Failed to pick proposal [attempt %d]. %f<%f<%f", attempts,
         lowerBounds.element(i), proposal.element(i), upperBounds.element(i)));
         break;
         }
         }
         attempts++;
         } while (proposedStepOutOfBounds);
         */
        return proposal;
    }

    private final int n;
    private final Vector lowerBounds;
    private final Vector upperBounds;
    private final Vector means;
    private final Matrix covariances;
}
