package broadwick.stochastic;

/**
 * A SimulationController (and its implementing classes) are responsible for controlling the simulation in the sense
 * that the StochasticSimulator checks with the controller object whether to go on or not.
 */
public interface SimulationController {

    /**
     * Returns whether or not to go on with the given simulation.
     * @param process the stochastic process this object is to control.
     * @return true if the process can continue, false if it should stop after the current step.
     */
    boolean goOn(StochasticSimulator process);
}
