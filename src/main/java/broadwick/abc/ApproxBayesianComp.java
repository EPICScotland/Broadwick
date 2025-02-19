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
import java.util.LinkedList;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

/**
 * This class runs an approximate Bayesian computation, sampling from the posterior.
 */
public class ApproxBayesianComp {

    /**
     * Create an ABC instance. The created instance will be ready to run a calculation using the default controller and
     * distance measure, these will need to be set if custom ones are required.
     * @param observedData the observed data from which the distance will be calculated.
     * @param model        the model that generates the data.
     * @param priors       the sampler objec that samples from the priors.
     * @param sensitivity  the distance cutoff, distance greater than this value will be ignored.
     */
    public ApproxBayesianComp(final AbcNamedQuantity observedData, final AbcModel model,
                              final AbcPriorsSampler priors, final double sensitivity) {
        this.posteriors = new LinkedHashMap<>();
        this.observedData = observedData;
        this.model = model;
        this.priors = priors;
        this.epsilon = sensitivity;
        numSamplesTaken = 0;
    }

    /**
     * Run the ABC algorithm.
     */
    public final void run() {

        while (controller.goOn(this)) {

            final AbcNamedQuantity parameters = priors.sample();
            numSamplesTaken++;
            final AbcNamedQuantity generatedData = model.run(parameters);

            if (distance.calculate(generatedData, observedData) < epsilon) {
                save(parameters);
            }
        }
    }

    /**
     * Save the sample taken from the prior as it meets the criteria set for being a posterior sample.
     * @param prior the sampled values from the prior.
     */
    private void save(final AbcNamedQuantity prior) {

        if (posteriors.isEmpty()) {
            for (final Map.Entry<String, Double> entry : prior.getParameters().entrySet()) {
                final LinkedList<Double> vals = new LinkedList<>();
                vals.add(entry.getValue());
                posteriors.put(entry.getKey(), vals);
            }
        } else {
            for (final Map.Entry<String, Double> entry : prior.getParameters().entrySet()) {
                posteriors.get(entry.getKey()).add(entry.getValue());
            }
        }
    }
    @Setter
    private AbcController controller = new AbcMaxNumStepController();
    @Setter
    private AbcDistance distance = new AbcAbsDistance();
    private final AbcModel model;
    private final AbcPriorsSampler priors;
    @Getter
    private final Map<String, LinkedList<Double>> posteriors;
    private final AbcNamedQuantity observedData;
    private final double epsilon;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private int numSamplesTaken;
}
