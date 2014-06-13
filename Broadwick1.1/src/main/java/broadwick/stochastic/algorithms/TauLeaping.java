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
import broadwick.stochastic.SimulationEvent;
import broadwick.stochastic.TransitionKernel;

/**
 * Implementation of the tau-leaping algorithm that makes an approximation to the stochastic ODE by picking a reasonable
 * time-step and then performing all the reactions that occur in this step. It is faster than the Gillespie algorithm
 * for large population sizes.
 */
public class TauLeaping extends AbstractTauLeapingBase {

    /**
     * Create the tau-leaping object.
     * @param amountManager    the amount manager used in the simulator.
     * @param transitionKernel the transition kernel to be used with the stochastic solver.
     */
    public TauLeaping(final AmountManager amountManager, final TransitionKernel transitionKernel) {
        super(amountManager, transitionKernel);
    }

    @Override
    public final String getName() {
        return "Tau Leap";
    }

    @Override
    public final void setRngSeed(final int seed) {
        GENERATOR.seed(seed);
    }

    @Override
    public final void reinitialize() {
        // nothing to do
    }

    @Override
    public final void performStep() {

        final double tau = (1 / calculateRTotal()) * Math.log(1 / GENERATOR.getDouble());
        while (isThetaEventInCurrentStep(tau)) {
            doThetaEvent();
        }

        // Set the reactions
        updateStates(tau);

        // advance the time
        setCurrentTime(getCurrentTime() + tau);
    }

    /**
     * Calculate the h's described in (14) page 413 and the sum a (26) page 418.
     * @return the sum of the probabilities of all events.
     */
    private double calculateRTotal() {
        double rTotal = 0.0;

        for (final SimulationEvent event : getTransitionKernel().getTransitionEvents()) {
            rTotal += getTransitionKernel().getTransitionProbability(event);
        }
        return rTotal;
    }

}
