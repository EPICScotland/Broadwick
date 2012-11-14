package broadwick.stochastic.algorithms;

import broadwick.stochastic.AmountManager;
import broadwick.stochastic.TransitionKernel;
import lombok.extern.slf4j.Slf4j;

/**
 * Implementation of the tau-leaping algorithm that makes an approximation to the stochastic ODE by picking a reasonable
 * time-step and then performing all the reactions that occur in this step. It is faster than the Gillespie algorithm
 * for large population sizes. This class implements a fixed step size.
 */
@Slf4j
public class TauLeapingFixedStep extends AbstractTauLeapingBase {

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

        log.trace("TauLeapingFixedStep: performStep at {}", getCurrentTime() + tau);
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
}
