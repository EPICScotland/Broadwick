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
package broadwick.utils;

import com.rits.cloning.Cloner;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility for object cloning using serialization. This allows objects to be deep-copied
 * without the need for each object to implement the Cloneable interface.
 * <p>
 * Note, object being cloned MUST be serializable.
 */
@Slf4j
public final class CloneUtils {

    /**
     * Hide utility class constructor.
     */
    private CloneUtils() {
    }

    /**
     * Deep clones an object. The object to be clone does not need to implement Cloneable or be serializable
     * for this method to work.
     * @param <T> the type of "o"
     * @param o The object to be cloned.
     * @return The cloned object.
     */
    @SuppressWarnings("FinalStaticMethod")
    public static <T extends Object> T deepClone(final T o) {
        return (new Cloner()).deepClone(o);
    }

}
