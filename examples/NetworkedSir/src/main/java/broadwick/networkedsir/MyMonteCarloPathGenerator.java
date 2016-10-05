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
package broadwick.networkedsir;

import broadwick.config.generated.Prior;
import broadwick.config.generated.UniformPrior;
import broadwick.montecarlo.MonteCarloStep;
import broadwick.montecarlo.markovchain.MarkovStepGenerator;
import broadwick.statistics.distributions.TruncatedNormalDistribution;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/**
 * Generates a [Markov] path through parameter space of the BtbIbmClusterDynamics.
 */
public class MyMonteCarloPathGenerator implements MarkovStepGenerator {

    /**
     * Create a path generator object that proposes a new step according to a given set of priors.
     * @param priors        the priors to be applied in proposing a new step.
     */
    public MyMonteCarloPathGenerator(final Collection<Prior> priors) {
        final Map<String, Double> initialVals = new LinkedHashMap<>();
        for (final Prior prior : priors) {
            //Note - this only works for uniform priors 
            // list of priors a list of actual distribution objects e.g. Uniform, TruncatedNormalDistribution
            final UniformPrior uniformPrior = (UniformPrior) prior;
            this.priors.put(uniformPrior.getId(), uniformPrior);
            initialVals.put(uniformPrior.getId(), uniformPrior.getInitialVal());

            if (uniformPrior.getInitialVal() > uniformPrior.getMax() || uniformPrior.getInitialVal() < uniformPrior.getMin()) {
                throw new IllegalArgumentException(String.format("Invalid prior [%s]. Initial value [%f] not in range [%f,%f]",
                                                                 uniformPrior.getId(), uniformPrior.getInitialVal(), uniformPrior.getMin(), uniformPrior.getMax()));
            }
        }
        this.initialStep = new MonteCarloStep(initialVals);
    }

    @Override
    public final MonteCarloStep generateNextStep(MonteCarloStep step) {

        final Map<String, Double> proposedStep = new LinkedHashMap<>(step.getCoordinates().size());
        for (final Map.Entry<String, Double> entry : step.getCoordinates().entrySet()) {

            final UniformPrior prior = (UniformPrior) (priors.get(entry.getKey()));

            final double proposedVal = new TruncatedNormalDistribution(entry.getValue(), 10.0, prior.getMin(), prior.getMax()).sample();

            proposedStep.put(entry.getKey(), Math.round(proposedVal * precision) / precision);
        }

        return new MonteCarloStep(proposedStep);
    }

    private final Map<String, UniformPrior> priors = new HashMap<>();
    @Getter
    private final MonteCarloStep initialStep;
    private final double precision = 10000.0;
}
