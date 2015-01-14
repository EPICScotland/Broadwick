/*
 * Copyright 2014 University of Glasgow.
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

import broadwick.statistics.Samples;

/**
 * A default implementation for the MonteCarloResults interface.
 */
public class MonteCarloDefaultResults implements MonteCarloResults {

    /**
     * Create a default implementation of the MonteCarloResults class. This class stores a single result from a Monte
     * Carlo simulation and returns the mean of this value over all simulations as the expected value.
     */
    MonteCarloDefaultResults() {
        this.samples = new Samples();
    }

    @Override
    public final double getExpectedValue() {
        return samples.getMean();
    }

    @Override
    public final Samples getSamples() {
        return samples;
    }

    @Override
    public final String toCsv() {
        return String.format("%f (%f)", samples.getMean(), samples.getStdDev());
    }

    @Override
    public final MonteCarloResults join(final MonteCarloResults results) {
        samples.add(results.getSamples());
        return this;
    }

    @Override
    public final void reset() {
        this.samples = new Samples();
    }

    private Samples samples;

}
