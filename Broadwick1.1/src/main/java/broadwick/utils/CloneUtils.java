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

import com.google.common.base.Throwables;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
     * Provides a deep clone serializing/de-serializing
     * <code>objToClone</code>.
     * @param objToClone The object to be cloned.
     * @return The cloned object.
     */
    @SuppressWarnings("FinalStaticMethod")
    public static Object deepClone(final Object objToClone) {
        Object obj = null;
        try {
            // Write the object out to a byte array
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            try (ObjectOutputStream out = new ObjectOutputStream(bos)) {
                out.writeObject(objToClone);
                out.flush();
            }

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            final ObjectInputStream in = new ObjectInputStream(
                    new ByteArrayInputStream(bos.toByteArray()));
            obj = in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            log.error(Throwables.getStackTraceAsString(e));
        }
        return obj;
    }

}
