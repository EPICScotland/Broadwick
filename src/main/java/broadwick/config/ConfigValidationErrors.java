package broadwick.config;

import lombok.Getter;

/**
 * This class encapsulates errors found during the validation of configuration
 * files.
 */
@SuppressWarnings("serial")
public final class ConfigValidationErrors {

    /**
     * Constructs a new exception with null as its detail message.
     */
    public ConfigValidationErrors() {
    }

    /**
     * Add an error to the list of validation errors.
     *
     * @param error the error to be added to the list of errors.
     */
    public void addError(final String error) {
        numErrors++;
        errors.append(String.format(errorString,Status.MAJOR, error));
        if (!error.endsWith(NEWLINE)) {
            errors.append(NEWLINE);
        }
        valid = false;
    }

    /**
     * Add an error to the list of validation errors.
     *
     * @param status the error status of this validation error.
     * @param error the error to be added to the list of errors.
     */
    public void addError(final Status status, final String error) {
        numErrors++;
        errors.append(String.format(errorString,status, error));
        if (!error.endsWith(NEWLINE)) {
            errors.append(NEWLINE);
        }
        
        if (Status.MAJOR.equals(status)) {
            valid = false;
        }
    }

    /**
     * Get the validation errors.
     *
     * @return the list of validation errors.
     */
    public String getValidationErrors() {
        return errors.toString();
    }

    /**
     * Error status of the configuration status.
     */
    public static enum Status {

        /**
         * MINOR error, basically a warning, The model should still run but may
         * not be what the user intended.
         */
        MINOR,
        /**
         * MAJOR error, the model will not run.
         */
        MAJOR
    }

    private StringBuilder errors = new StringBuilder();
    @SuppressWarnings("PMD.UnusedPrivateField")
    @Getter
    private boolean valid;
    @SuppressWarnings("PMD.UnusedPrivateField")
    @Getter
    private int numErrors = 0;
    private String errorString = "\t %s - %s";
    private static final String NEWLINE = "\n";
}
