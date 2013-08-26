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
package broadwick.rng;

/**
 * A RuntimeException for random number generators.
 */
@SuppressWarnings("serial")
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
