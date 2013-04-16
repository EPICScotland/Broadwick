package broadwick.stochastic.algorithms;

import broadwick.rng.RNG;
import broadwick.stochastic.AmountManager;
import broadwick.stochastic.SimulationEvent;
import broadwick.stochastic.StochasticSimulator;
import broadwick.stochastic.TransitionKernel;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Abstract base class for tau leaping classes.
 */
@Slf4j
public abstract class AbstractTauLeapingBase extends StochasticSimulator {

    /**
     * Create the tau-leaping object.
     * @param amountManager the amount manager used in the simulator.
     * @param transitionKernel the transition kernel to be used with the stochastic solver.
     */
    public AbstractTauLeapingBase(final AmountManager amountManager, final TransitionKernel transitionKernel) {
        super(amountManager, transitionKernel);
    }

    /**
     * Update all the reactions/transitions that have occurred in the tau step.
     * @param tau the step size.
     */
    protected final void updateStates(final double tau) {
        log.trace("AbstractTauLeapingBase: Updating states at {} using a step size of {}", getCurrentTime(), tau);
        final Set<SimulationEvent> transitionEvents = new LinkedHashSet<>();
        transitionEvents.addAll(getTransitionKernel().getTransitionEvents());
        
        for (SimulationEvent event : transitionEvents) {
            if (getTransitionKernel().getTransitionEvents().contains(event)) {
                final double rate = getTransitionKernel().getTransitionProbability(event);
                final double times = GENERATOR.getPoisson(rate * tau);
                if (times > 0) {
                    log.trace("Updating states {}", new StringBuilder()
                            .append("for event ").append(event.toString())
                            .append(", rate = ").append(rate)
                            .append(", times = ").append(times)
                            .toString());
                    doEvent(event, getCurrentTime(), ((int) Math.round(times)));
                    log.trace("fired event {} {} time(s)", event, times);
                }
            }
        }
    }

    /**
     * Determine of there is a theta event in the current time step, taking into
     * account possible reverse time jumps.
     * @param tau the time interval (will be negative for reverse time).
     * @return true if a theta event is found.
     */
    protected final boolean isThetaEventInCurrentStep(final double tau) {
        final double nextEventTime = getNextThetaEventTime();
        if (reverseTime) {
            return getCurrentTime() >= nextEventTime && getCurrentTime() + tau < nextEventTime;
        } else {
            return getCurrentTime() <= nextEventTime && getCurrentTime() + tau > nextEventTime;
        }
    }
    protected static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
}
