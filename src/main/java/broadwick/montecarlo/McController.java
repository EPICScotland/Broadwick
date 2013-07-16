package broadwick.montecarlo;


/**
 * Controller class for running monte carlo calculations. The controller (and its implementing classes) are responsible
 * for determining whether or not the calculation should end.
 */
public interface McController {

    /**
     * Returns whether or not to continue with the calculation.
     * @param mc the Monte Carlo process this object is to control.
     * @return true if the process can continue, false if it should stop after the current step.
     */
    boolean goOn(MonteCarlo mc);
}
/**
 * Create a default controller that run 1000 steps.
 */
class MonteCarloMaxNumStepController implements McController {

    /**
     * Create a Monte Carlo controller object that stops the run after a specified number of steps (a fixed path length).
     */
    MonteCarloMaxNumStepController() {
        this.maxSteps = 1000;
    }
    
    /**
     * Create a Monte Carlo controller object that stops the run after a specified number of steps (a fixed path length).
     * @param maxSteps the maximum length of the monte carlo chain.
     */
    MonteCarloMaxNumStepController(final int maxSteps) {
        this.maxSteps = maxSteps;
    }
    
    @Override
    public boolean goOn(final MonteCarlo mc) {
        return mc.getNumStepsTaken() <= maxSteps;
    }
    
    private int maxSteps;
};