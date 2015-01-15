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
package broadwick.model;

/**
 * This class defines a checked exception in dealing with model parameters.
 */
public class ModelParameterException extends RuntimeException {
    
    /**
     * Constructs a <code>ModelParameterException</code> with <tt>null</tt>
     * as its error message string.
     */
    public ModelParameterException() {
        super();
    }

    /**
     * Constructs a <code>ModelParameterException</code>, saving a reference
     * to the error message string <tt>s</tt> for later retrieval by the
     * <tt>getMessage</tt> method.
     *
     * @param   s   the detail message.
     */
    public ModelParameterException(final String s) {
        super(s);
    }
}
