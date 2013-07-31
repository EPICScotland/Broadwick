package broadwick.montecarlo;

/**
 * Create a default controller that run 1000 steps.
 */
public class McMaxNumStepController implements McController {

    /**
     * Create a Monte Carlo controller object that stops the run after a specified number of steps (a fixed path
     * length).
     */
    public McMaxNumStepController() {
        this(1000);
    }

    /**
     * Create a Monte Carlo controller object that stops the run after a specified number of steps (a fixed path
     * length).
     * @param maxSteps the maximum length of the monte carlo chain.
     */
    public McMaxNumStepController(final int maxSteps) {
        this.maxSteps = maxSteps;
    }

    @Override
    public final boolean goOn(final MonteCarlo mc) {
        return mc.getNumStepsTaken() < maxSteps;
    }
    private int maxSteps;
}
