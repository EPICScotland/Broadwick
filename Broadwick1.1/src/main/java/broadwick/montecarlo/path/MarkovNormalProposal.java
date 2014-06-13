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

import broadwick.rng.RNG;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A proposal class that samples each new parameter in the steps coordinate from a normal distribution.
 */
public class MarkovNormalProposal implements MarkovProposalFunction {

    @Override
    public final Step generate(final Step step) {

        final Map<String, Double> proposedStep = new LinkedHashMap<>(step.getCoordinates().size());
        for (Map.Entry<String, Double> entry : step.getCoordinates().entrySet()) {
            proposedStep.put(entry.getKey(), GENERATOR.getGaussian(entry.getValue(), 1.0));
        }
        
        return new Step(proposedStep);
    }
    
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
}
