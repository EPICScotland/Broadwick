package broadwick.rng;

/**
 * A RuntimeException for random number generators.
 */
public class RngException extends RuntimeException {

    /**
     * Constructs a new RNG exception with null as its detail message.
     */
    public RngException() {
        super();
    }

    /**
     * Constructs a new RNG exception with the specified detail message.
     * @param msg the detail message. The detail message is saved for later
     * retrieval by the Throwable.getMessage() method.
     */
    public RngException(final String msg) {
        super(msg);
    }
}
