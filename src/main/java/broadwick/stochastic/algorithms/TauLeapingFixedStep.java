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
package broadwick.stochastic.algorithms;

import broadwick.stochastic.AmountManager;
import broadwick.stochastic.TransitionKernel;
import java.io.Serializable;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the tau-leaping algorithm that makes an approximation to the stochastic ODE by picking a reasonable
 * time-step and then performing all the reactions that occur in this step. It is faster than the Gillespie algorithm
 * for large population sizes. This class implements a fixed step size.
 */
@Slf4j
public class TauLeapingFixedStep extends AbstractTauLeapingBase implements Serializable {

    /**
     * Create the tau-leaping object.
     * @param amountManager    the amount manager used in the simulator.
     * @param transitionKernel the transition kernel to be used with the stochastic solver.
     * @param tau              the fixed step.
     */
    public TauLeapingFixedStep(final AmountManager amountManager, final TransitionKernel transitionKernel, final int tau) {
        super(amountManager, transitionKernel);
        this.tau = tau;
    }

    @Override
    public final String getName() {
        return "Tau Leap Fixed Step";
    }

    @Override
    public final void performStep() {

        log.trace("TauLeapingFixedStep: performStep at {}", getCurrentTime());
        while (isThetaEventInCurrentStep(tau)) {
            doThetaEvent();
        }

        // Set the reactions
        updateStates(this.tau);

        // advance the time
        setCurrentTime(getCurrentTime() + tau);
    }

    @Override
    public final void setRngSeed(final int seed) {
        GENERATOR.seed(seed);
    }

    @Override
    public final void reinitialize() {
        // nothing to do.
    }

    private final int tau;

    /**
     * The serialVersionUID.
     */
    private static final long serialVersionUID = -201226401874224152L;
}
