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
package broadwick.montecarlo;

import broadwick.statistics.Samples;

/**
 * This interface encapsulates the results from a Monte Carlo run. By Default a Monte Carlo object will run simulations 
 * and use implementations of this class to store the results. It does do by adding the simulation results to the 
 * Samples object that is used to obtain an expected value for the simulation.
 */
public interface MonteCarloResults extends Cloneable {

    /**
     * Get the score for the Monte Carlo simulation. The Acceptor interface and it's implementing classes require a
     * known value in this class for their analysis, e.g. this method may return the likelihood of the simulation and
     * the Acceptor class (e.g. MetropolisHastingsAcceptor) will use the McResults.getExpectedValue() method to
     * determine whether or not to accept this proposed step.
     * @return the value of the score.
     */
    double getExpectedValue();

    /**
     * Get the results of the Monte Carlo simulations as a Samples object.
     * @return A samples object that contains the results of each Monte Carlo Simulation.
     */
    Samples getSamples();

    /**
     * Get a CSV string of the Monte Carlo results stored in this class. This will be used for logging and for output to
     * file and can contain statistics about the results e.g. the mean, std deviation etc. as appropriate.
     * @return a CSV representation of the results.
     */
    String toCsv();

    /**
     * Join another Monte Carlo results object to this one. When running a Monte Carlo simulation it is often necessary
     * to append the results from a simulation to a 'master' results object.
     * @param results the results object to join.
     * @return the new/updated results object.
     */
    MonteCarloResults join(MonteCarloResults results);
    
    /**
     * Clear the results of the MonteCarloResults object. This will be called by several algorithms (e.g. MCMC) to 
     * clear any cached results that should be cleared between runs.
     */
    void reset();
}
