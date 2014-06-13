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

import lombok.Getter;
import lombok.Setter;

/**
 * An implementation of a Markov Chain. Markov Chains have a current step and generates a new step based solely on the
 * current one.
 */
public class MarkovChain implements PathGenerator {

    /**
     * Create a new Markov chain with an initial step.
     * @param initialStep the initial step.
     */
    public MarkovChain(final Step initialStep) {
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
    public MarkovChain(final Step initialStep, final MarkovProposalFunction generator) {
        this.currentStep = initialStep;
        this.generator = generator;
        this.chainLength = 1;
    }

    @Override
    public final Step generateNextStep(final Step step) {
        chainLength++;
        currentStep = generator.generate(step);
        return currentStep;
    }

    @Override
    public final Step getInitialStep() {
        return initialStep;
    }
    @Getter
    @Setter
    private Step currentStep;
    private Step initialStep;
    private MarkovProposalFunction generator;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private int chainLength;
}
