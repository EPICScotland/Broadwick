package broadwick;

/**
 * A general RuntimeException for the Broadwick framework.
 */
@SuppressWarnings("serial")
public class BroadwickException extends RuntimeException {

    /**
     * Constructs a new exception with null as its detail message.
     */
    public BroadwickException() {
        super();
    }

    /**
     * Constructs a new exception with the specified detail message.
     * @param msg the detail message. The detail message is saved for later
     * retrieval by the Throwable.getMessage() method.
     */
    public BroadwickException(final String msg) {
        super(msg);
    }
    
    /**
     * Constructs a new exception by wrapping a specified throwable.
     * @param throwable the exception that is wrapped.
     */
    public BroadwickException(final Throwable throwable) {
        super(throwable);
    }
}
