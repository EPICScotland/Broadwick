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
 * Utility class for test data.
 */
@Data
@SuppressWarnings({"PMD.UnusedPrivateField","serial","squid:S1068"})
public class Test implements Serializable {
// TODO what about custom tags??????

    private final String id;
    private final String group;
    private final String location;
    private final Integer testDate;
    private final Boolean positiveResult;
    private final Boolean negativeResult;
    private static final long serialVersionUID = -6830452430310922412L;
}
