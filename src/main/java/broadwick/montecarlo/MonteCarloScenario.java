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

import broadwick.utils.CloneUtils;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * A simple model class thats runs a model and creates named quantities, i.e. a map containing the vlaues and their
 * names.
 */
public abstract class MonteCarloScenario implements Serializable {

    /**
     * Compute the value of the Monte Carlo model/simulation at the given set of coordinates.
     * @param seed a seed for the random number generator in the scenario. Scenarios are created so quickly, relying on
     * the clock as a seed may lead to a lot of generator with the same sequence.
     * @return a Monte Carlo Results object.
     */
    public abstract MonteCarloResults run(final int seed);
    
    
    /**
     * Create a copy of the MonteCarloScenario object. This is used by the MonteCarlo producer object to create a 
     * copy of each scenario object, allowing for each scenario to control which attributes are deep or shallow copied.
     * @return a (deep) copy of the MonteCarloScenario object.
     */
    public MonteCarloScenario copyOf() {
        return (MonteCarloScenario) CloneUtils.deepClone(this);
    }
    
    @Getter
    @Setter
    protected MonteCarloStep step;
    @Getter
    @Setter
    protected Integer id;
}
