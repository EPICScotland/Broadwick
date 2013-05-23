package broadwick.abc;

/**
 * AbcController class for running abc. The controller (and its implementing classes) are responsible for determining
 * whether or not the calculation should end.
 */
public interface AbcController {

    /**
     * Returns whether or not to continue with the calculation.
     * @param process the stochastic process this object is to control.
     * @return true if the process can continue, false if it should stop after the current step.
     */
    boolean goOn(ApproxBayesianComp process);
}

/**
 * Create a default controller that run 1000 samples from the prior.
 * @author anthony
 */
class AbcMaxNumStepController implements AbcController {

    @Override
    public boolean goOn(final ApproxBayesianComp abc) {
        return abc.getNumSamplesTaken() <= 1000;
    }
};