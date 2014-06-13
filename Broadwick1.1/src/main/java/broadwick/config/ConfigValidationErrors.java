/*
 * Copyright 2013 University of Glasgow.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
