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
package broadwick.montecarlo.path;

/**
 * A Markov proposal distribution generates a new step based only on information from the current step.
 */
public interface MarkovProposalFunction {

    /** 
     * Generate a new step in the Markov Chain based only on the supplied step.
     * @param step the current step in the Markov Chain
     * @return the proposed new step.
     */
    Step generate(final Step step);
}
