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
package broadwick.montecarlo.path;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/**
 * A representation of the current step in a Markov Chain. The step is represented by a set of coordinates in the
 * parameter space spanning the possible set of steps.
 */
public class Step {

    /**
     * Create a single step in a Markov Chain.
     * @param coordinates the coordinates of the step in the parameter space of the chain.
     */
    public Step(final Map<String, Double> coordinates) {
        if (coordinates instanceof LinkedHashMap) {
            this.coordinates = new LinkedHashMap<>();
            this.coordinates.putAll(coordinates);
        } else {
            throw new IllegalArgumentException("The coordinates of the Monte Carlo Path step must be a LinkedHashMap");
        }
    }
    
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : coordinates.entrySet()) {
            sb.append(entry.getValue()).append(',');
        }
        
        // remove the last value separator.
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    @Getter
    Map<String, Double> coordinates;
}
