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

/**
 * MonteCarloAcceptor for a Monte Carlo step.
 */
public interface MonteCarloAcceptor {

    /**
     * Accept a step in a Monte Carlo path/chain based on the ratio of the results at each step.
     * @param oldResult the results at the previous step.
     * @param newResult the result at the current (proposed) step.
     * @return true if the step that generated newResults is to be accepted, false otherwise.
     */
    boolean accept(MonteCarloResults oldResult, MonteCarloResults newResult);
}
