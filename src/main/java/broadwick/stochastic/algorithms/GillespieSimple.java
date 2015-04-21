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

import broadwick.rng.RNG;
import broadwick.stochastic.AmountManager;
import broadwick.stochastic.SimulationEvent;
import broadwick.stochastic.StochasticSimulator;
import broadwick.stochastic.SimulationException;
import broadwick.stochastic.TransitionKernel;
import lombok.ToString;

/**
 * Implementation of Gillespie's Direct method. It is a simple Monte-Carlo algorithm which draws from a from Gillspie
 * derived distribution a reaction that will fire and a time at which the reaction will fire. <p> For reference see
 * Daniel T. Gillespie., A General Method for Numerically Simulating the Stochastic Time Evolution of Coupled Chemical
 * Reactions, J.Comp.Phys. 22, 403 (1976)
 */
@ToString
public class GillespieSimple extends StochasticSimulator {

    /**
     * Implementation of Gillespie's Direct method.
     * @param amountManager the amount manager used in the simulator.
     * @param transitionKernel the transition kernel to be used with the stochastic solver.
     */
    public GillespieSimple(final AmountManager amountManager, final TransitionKernel transitionKernel) {
        this(amountManager, transitionKernel, false);
    }

    /**
     * Implementation of Gillespie's Direct method.
     * @param amountManager the amount manager used in the simulator.
     * @param transitionKernel the transition kernel to be used with the stochastic solver.
     * @param reverseTime true if we wish to go backwards in time.
     */
    public GillespieSimple(final AmountManager amountManager, 
                           final TransitionKernel transitionKernel, final boolean reverseTime) {
        super(amountManager, transitionKernel, reverseTime);
    }

    @Override
    public final void reinitialize() {
        changed = true;
    }

    @Override
    public final void performStep() {
        final double rTotal = calculateRTotal();

        // obtain mu and tau by the direct method described in chapter 5A page 417ff
        final double tau = directMCTau(rTotal);
        if ((tau != Double.NEGATIVE_INFINITY) && (tau != Double.POSITIVE_INFINITY)) {
            changed = false;
            while (isThetaEventInCurrentStep(tau) && !changed) {
                doThetaEvent();
            }

            if (changed) {
                performStep();
                return;
            }

            if (Double.compare(rTotal, 0.0) != 0) {
                final SimulationEvent mu = directMCReaction();
                doEvent(mu, getCurrentTime() + tau);
            }
        }

        // advance the time
        setCurrentTime(getCurrentTime() + tau);

        if ((tau != Double.NEGATIVE_INFINITY) && (tau != Double.POSITIVE_INFINITY)) {
            doThetaEvent();
        }
    }

    /**
     * Calculate the h's described in (14) page 413 and the sum a (26) page 418.
     * @return the sum of the probabilities of all events.
     */
    private double calculateRTotal() {
        double rTotal = 0.0;
        for (SimulationEvent event : this.getTransitionKernel().getTransitionEvents()) {
            rTotal += this.getTransitionKernel().getTransitionProbability(event);
        }
        return rTotal;
    }

    /**
     * Determine of there is a theta event in the current time step, taking into account possible reverse time jumps.
     * @param tau the time interval (will be negative for reverse time).
     * @return true if a theta event is found.
     */
    private boolean isThetaEventInCurrentStep(final double tau) {
        final double nextEventTime = getNextThetaEventTime();
        if (reverseTime) {
            return getCurrentTime() >= nextEventTime && getCurrentTime() + tau < nextEventTime;
        } else {
            return getCurrentTime() <= nextEventTime && getCurrentTime() + tau > nextEventTime;
        }
    }

    @Override
    public final String getName() {
        return "Gillespie Direct Method";
    }

    @Override
    public final void setRngSeed(final int seed) {
        GENERATOR.seed(seed);
    }

    /**
     * obtains a random (but following a specific distribution) reaction as described by the direct method in chapter 5A
     * page 417ff.
     * @return the simulation event selected.
     */
    private SimulationEvent directMCReaction() {
        final double rTotal = calculateRTotal();
        final double test = GENERATOR.getDouble() * rTotal;

        double sum = 0;
        for (SimulationEvent event : getTransitionKernel().getTransitionEvents()) {
            sum += this.getTransitionKernel().getTransitionProbability(event);
            if (test <= sum) {
                return event;
            }
        }
        final StringBuilder sb = new StringBuilder(100);
        sb.append("No reaction could be selected!\nreaction = ");
        sb.append(test).append("\n");
        sb.append(this.getTransitionKernel().getTransitionEvents().toString());
        throw new SimulationException(sb.toString());
    }

    /**
     * obtains a random (but following a specific distribution) timestep as described by the direct method in chapter 5A
     * page 417ff.
     * @param sum sum of the propensities
     * @return    tau
     */
    protected final double directMCTau(final double sum) {
        if (Double.compare(sum, 0.0) == 0) {
            return getNextThetaEventTime();
        }
        final double r1 = GENERATOR.getDouble();
        final double tau = (1 / sum) * Math.log(1 / r1);
        if (reverseTime) {
            return -1.0 * tau;
        }
        return tau;
    }

    private boolean changed = false;
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
}
