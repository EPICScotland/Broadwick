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

package seir;

import broadwick.annotations.Parameter;
import broadwick.annotations.Prior;
import broadwick.data.Movement;
import broadwick.model.Model;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple class that runs an SEIR model.
 */
@Slf4j
public class SeirModel extends Model {

    @Override
    public void init() {
    }

    @Override
    public void run() {
        // Simple example of how to use/obtain paremters and priors from the configuration file.
        // Member that are marked as priors and paramters are read from the config file, if the configuration
        // file does not specify values, default ones that are apecified in the annotation are used.
        //        for (Map.Entry<String, Parameters.Parameter> parameter : getParameters().entrySet()) {
        //            log.info("{} ({}): " + parameter.getValue().getValue(), parameter.getKey(), parameter.getValue().getHint());
        //        }

        if (this.getLookup() != null) {
            Collection<Movement> movements = this.getLookup().getMovements();
            for (Movement movement : movements) {
                log.info("Movement {}", movement.toString());
            }
        }
        
        log.info("XML = {}", this.getModel());
    }
    
    @Override
    public void finalise() {
    }

    @Parameter(hint = "Transmission term")
    @Prior(min = 1E-3, max = 1.0, distribution = "Uniform")
    private double beta;
}
