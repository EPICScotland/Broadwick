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
 * Interface defining all continuous distribution classes.
 */
public interface DiscreteDistribution {

    /**
    * Generate a random value sampled from this distribution.
    * 
     * @return the sampled value from the distribution.
     */
    int sample();
    
    
    /**
     * Get the numerical value of the mean of this distribution.
     *
     * @return the mean or {@code Double.NaN} if it is not defined
     */
    double getMean();
    
     /**
     * Get the numerical value of the variance of this distribution.
     *
     * @return the variance (possibly {@code Double.POSITIVE_INFINITY} or
     * {@code Double.NaN} if it is not defined)
     */
    double getVariance();
}
