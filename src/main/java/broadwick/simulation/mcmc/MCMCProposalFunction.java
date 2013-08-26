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
 * Proposal function for MCMC sequences to obtain the next MCMCState in the chain.
 */
public interface MCMCProposalFunction {

    /**
     * Propose a new MCMCState from a probability distribution. This method is responsible for sampling from the
     * required probability distribution of states to obtain the next state in the chain of states that make up the
     * Markov Chain.
     * @param state the current (MCMC) state of the system.
     * @return the new proposed state.
     */
    MCMCState propose(MCMCState state);
}
