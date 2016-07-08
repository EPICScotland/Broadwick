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
 */package broadwick.stochasticinf;

import broadwick.montecarlo.MonteCarloResults;
import broadwick.statistics.Samples;
import broadwick.statistics.distributions.MultinomialDistribution;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for holding the results of each scenario run.
 */
@Slf4j
public final class MyMonteCarloScenarioResults implements MonteCarloResults {

    MyMonteCarloScenarioResults(final MySettings settings) {
//        this.settings = settings;
        this.reset();

        final int popnSize = settings.getPopulationSize();
        final double[] probabilities = new double[3];

        final int numUnobserved = settings.getPopulationSize() - settings.getNumObservedExposed()
                                  - settings.getNumObservedInfectious();

        probabilities[0] = (1.0 * numUnobserved) / popnSize;
        probabilities[1] = (1.0 * settings.getNumObservedExposed()) / popnSize;
        probabilities[2] = (1.0 * settings.getNumObservedInfectious()) / popnSize;

        dist = new MultinomialDistribution(popnSize, probabilities);
    }

    @Override
    public double getExpectedValue() {
        return expectedValue.getSize() == 0 ? MIN_VALUE : expectedValue.getMean();
    }

    @Override
    public Samples getSamples() {
        return expectedValue;
    }

    @Override
    public String toCsv() {
        double likelihood = this.expectedValue.getMean();
        if (this.expectedValue.getSize() == 0) {
            likelihood = MIN_VALUE;
        }

        return String.format("%f,%f,%f,%f,%f", likelihood,
                             numberOfSusceptibles.getMean(),
                             numberOfExposed1.getMean(),
                             numberOfExposed2.getMean(),
                             numberOfInfectious.getMean());
    }

    @Override
    public MonteCarloResults join(MonteCarloResults results) {
        final MyMonteCarloScenarioResults mcResults = (MyMonteCarloScenarioResults) results;

        final double likelihood = mcResults.getScore();
        if (likelihood != MIN_VALUE) {
            this.expectedValue.add(likelihood);
        }

        this.getNumberOfSusceptibles().add(mcResults.getNumberOfSusceptibles());
        this.getNumberOfExposed1().add(mcResults.getNumberOfExposed1());
        this.getNumberOfExposed2().add(mcResults.getNumberOfExposed2());
        this.getNumberOfInfectious().add(mcResults.getNumberOfInfectious());

        return this;
    }

    @Override
    public void reset() {

        this.expectedValue = new Samples();
        this.numberOfSusceptibles = new Samples();
        this.numberOfExposed1 = new Samples();
        this.numberOfExposed2 = new Samples();
        this.numberOfInfectious = new Samples();

    }

    /**
     * Calculate the Monte Carlo score (the log-likelihood) for the simulation.
     * @return the log-likelihood.
     */
    public double getScore() {
        if (dist != null) {

            final int[] x = new int[dist.getN()];
            // when we call getScore there will only be one value in the sample so rounding to an integer should be ok.
            x[0] = (int) (Math.round(numberOfSusceptibles.getMean()) + Math.round(numberOfExposed1.getMean()));
            x[1] = (int) (Math.round(numberOfExposed2.getMean()));
            x[2] = (int) (Math.round(numberOfInfectious.getMean()));

            // We want the log-likelihood here so we copy most of the code from MultinomialDistribution but  
            // neglecting the final Math.exp() call.
            double sumXFact = 0.0;
            int sumX = 0;
            double sumPX = 0.0;
            final double[] p = dist.getP();

            for (int i = 0; i < p.length; i++) {
                sumX += x[i];
                sumXFact += broadwick.math.Factorial.lnFactorial(x[i]);
                if (p[i] > 1E-15) {
                    // just in case probabilities[i] == 0.0
                    sumPX += (x[i] * Math.log(p[i]));
                }
            }

            if (sumX != dist.getN()) {
                throw new IllegalArgumentException(String.format("Multinomial distribution error: Sum_x [%d] != number of samples [%d].", sumX, dist.getN()));
            } else {
                final double logLikelihood = broadwick.math.Factorial.lnFactorial(dist.getN()) - sumXFact + sumPX;
                log.debug("logLikelihood : {}", logLikelihood);
                return logLikelihood;
            }
        }
        return MIN_VALUE;
    }
//    private final MySettings settings;
    private Samples expectedValue;
    @Getter
    private Samples numberOfSusceptibles;
    @Getter
    private Samples numberOfExposed1;
    @Getter
    private Samples numberOfExposed2;
    @Getter
    private Samples numberOfInfectious;
    private final MultinomialDistribution dist;
    private final static Double MIN_VALUE = Double.NEGATIVE_INFINITY; //-Double.MAX_VALUE;
}
