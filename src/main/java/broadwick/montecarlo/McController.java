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