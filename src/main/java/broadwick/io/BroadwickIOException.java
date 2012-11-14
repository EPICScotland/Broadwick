package broadwick.io;

/**
 * A RuntimeException for I/O classes.
 */
@SuppressWarnings("serial")
public class BroadwickIOException extends RuntimeException {

    /**
     * Constructs a new io exception with null as its detail message.
     */
    public BroadwickIOException() {
        super();
    }

    /**
     * Constructs a new io exception with the specified detail message.
     * @param msg the detail message. The detail message is saved for later
     * retrieval by the Throwable.getMessage() method.
     */
    public BroadwickIOException(final String msg) {
        super(msg);
    }
}
