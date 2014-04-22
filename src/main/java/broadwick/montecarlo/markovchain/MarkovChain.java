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
package broadwick.montecarlo.markovchain;

import broadwick.montecarlo.MonteCarloStep;
import broadwick.montecarlo.markovchain.proposal.MarkovProposalFunction;
import broadwick.montecarlo.markovchain.proposal.MarkovNormalProposal;
import lombok.Getter;
import lombok.Setter;

/**
 * An implementation of a Markov Chain which stores the history. Markov Chains have a current step and generates a new 
 * step based solely on the current one.
 */
public class MarkovChain implements MarkovStepGenerator {

    /**
     * Create a new Markov chain with an initial step.
     * @param initialStep the initial step.
     */
    public MarkovChain(final MonteCarloStep initialStep) {
        this.currentStep = initialStep;
        this.initialStep = initialStep;
        this.generator = new MarkovNormalProposal();
        this.chainLength = 1;
    }

    /**
     * Create a new Markov Chain with an initial step and proposal function.
     * @param initialStep the initial step.
     * @param generator   the generator object for proposing new steps.
     */
    public MarkovChain(final MonteCarloStep initialStep, final MarkovProposalFunction generator) {
        this.currentStep = initialStep;
        this.generator = generator;
        this.chainLength = 1;
    }

    @Override
    public final MonteCarloStep generateNextStep(final MonteCarloStep step) {
        chainLength++;
        currentStep = generator.generate(step);
        return currentStep;
    }

    @Override
    public final MonteCarloStep getInitialStep() {
        return initialStep;
    }
    @Getter
    @Setter
    private MonteCarloStep currentStep;
    private MonteCarloStep initialStep;
    private final MarkovProposalFunction generator;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private int chainLength;
}
