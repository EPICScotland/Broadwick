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
package broadwick.data;

import java.io.Serializable;
import lombok.Data;

/**
 * Utility class for location data.
 */
@Data
@SuppressWarnings({"PMD.UnusedPrivateField","serial","squid:S1068"})
public class Animal implements Serializable {
// TODO what about cutom tags??????

    private final String id;
    private final String species;
    private final Integer dateOfBirth;
    private final String locationOfBirth;
    private final Integer dateOfDeath;
    private final String locationOfDeath;
    private static final long serialVersionUID = 1358067210450945701L;
}
