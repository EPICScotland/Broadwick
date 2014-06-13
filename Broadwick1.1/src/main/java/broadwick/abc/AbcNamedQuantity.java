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
package broadwick.abc;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/**
 * An ABC named quantity is an order maintaining store of key-value pairs that can be used to store parameters
 * for a model or the summary statistic calculated by a model.
 */
public class AbcNamedQuantity {

    /**
     * Create a single set of named quantities.
     * @param parameters the collection of named values.
     */
    public AbcNamedQuantity(final Map<String, Double> parameters) {
        if (parameters instanceof LinkedHashMap) {
            this.parameters = new LinkedHashMap<>();
            this.parameters.putAll(parameters);
        } else {
            throw new IllegalArgumentException("The parameters of an ABC Model must be a LinkedHashMap");
        }
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : parameters.entrySet()) {
            sb.append(entry.getValue()).append(',');
        }

        // remove the last value separator.
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    @Getter
    private Map<String, Double> parameters;
}
