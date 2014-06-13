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
package broadwick.montecarlo;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/**
 * The coordinates of a Monte Carlo step. These are an order maintaining store of key-value pairs that can be used to 
 * store parameters for a model and where the toString() method returns a CSV list of the values in a consistent order.
 */
class McStepCoordinates {
    
    /**
     * Create an object to store the coordinates of a Monte Carlo step.
     * @param parameters the collection of named values.
     */
    public McStepCoordinates(final Map<String, Double> parameters) {
        if (parameters instanceof LinkedHashMap) {
            this.parameters = new LinkedHashMap<>();
            this.parameters.putAll(parameters);
        } else {
            throw new IllegalArgumentException("The parameters of McStepCoordinates must be a LinkedHashMap");
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