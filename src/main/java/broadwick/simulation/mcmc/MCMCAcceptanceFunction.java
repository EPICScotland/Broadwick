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
package broadwick.simulation.mcmc;

/**
 * Acceptance function for MCMC that determines whether or not a proposed state should be accepted or not.
 */
public interface MCMCAcceptanceFunction {

    /**
     * Determine whether or not to accept the proposed state in the Markov Chain.
     * @param state the current (MCMC) state of the system.
     * @param proposedState the proposed state in the chain.
     * @return true if the proposed step is to be accepted.
     */
    boolean isAccepted(final MCMCState state, final MCMCState proposedState);
}
