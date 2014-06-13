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

/**
 * This interface encapsulates the results from a Monte Carlo run. All McModels are expected to create a class implementing
 * this interface.
 */
public interface McResults {
    
    /**
     * Get the score for the Monte Carlo simulation. The Acceptor interface and it's implementing classes require 
     * a known value in this class for their analysis, e.g. this method may return the likelihood of the simulation and
     * the Acceptor class (e.g. MetropolisHastingsAcceptor) will use the McResults.getScore() method to determine whether
     * or not to accept this proposed step.
     * @return the value of the score.
     */
    double getScore();
    
    /**
     * Get a CSV string of the Monte Carlo results stored in this class. This will be used for logging and for output to
     * file.
     * @return a CSV representation of the results. 
     */
    String toCsv();
}
