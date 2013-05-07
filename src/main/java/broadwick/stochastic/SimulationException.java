package broadwick.stochastic;

/**
 * A RuntimeException for stochastic solvers.
 */
@SuppressWarnings("serial")
public class SimulationException extends RuntimeException {

    /**
     * Constructs a new math exception with null as its detail message.
     */
    public SimulationException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     * @param msg the detail message. The detail message is saved for later
     * retrieval by the Throwable.getMessage() method.
     */
    public SimulationException(final String msg) {
        super(msg);
    }
}
