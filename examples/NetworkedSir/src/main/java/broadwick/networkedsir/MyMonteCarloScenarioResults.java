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

import broadwick.BroadwickException;
import broadwick.montecarlo.MonteCarloResults;
import broadwick.statistics.Samples;
import broadwick.statistics.distributions.MultinomialDistribution;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for holding the results of each scenario run.
 */
@Slf4j
public class MyMonteCarloScenarioResults implements MonteCarloResults {

    /**
     * Create an object to store the results from the Monte Carlo run.
     * @param numObservedInfected
     * @param numObservedRemoved
     */
    public MyMonteCarloScenarioResults(final int numObservedInfected, final int numObservedRemoved) {
        log.trace("Starting scenario results");

        this.reset();

        final int total = numObservedInfected + numObservedRemoved;
        final double[] probabilities = new double[2];
        probabilities[0] = (1.0 * numObservedInfected) / total;
        probabilities[1] = (1.0 * numObservedRemoved) / total;
        
        if (probabilities[0] == 0.0 || probabilities[1] == 0.0) {
            throw new BroadwickException("Cannot create multinomial distribution for likelihood calculation: number of observeables must be > 0");
        }

        dist = new MultinomialDistribution(total, probabilities);
    }

    @Override
    public final double getExpectedValue() {
        return expectedValue.getSize() == 0 ? MIN_VALUE : expectedValue.getMean();
    }

    @Override
    public final Samples getSamples() {
        return expectedValue;
    }

    @Override
    public final MonteCarloResults join(MonteCarloResults results) {
        // this is used to join all the 'expectedValues' of each simulation so we add it to the Samples object we've 
        // created
        final MyMonteCarloScenarioResults mcResults = (MyMonteCarloScenarioResults) results;

        final double likelihood = mcResults.getScore();
        if (likelihood != MIN_VALUE) {
            this.expectedValue.add(likelihood);
        }

        this.outbreakSize.add(mcResults.getOutbreakSize().getMax());
        this.numRemoved.add(mcResults.getNumRemoved().getSum());
        this.numInfected.add(mcResults.getNumInfected().getSum());

        this.infectedLocationsTimeSeries.append(mcResults.infectedLocationsTimeSeries).append("\n");
        this.infectedIndividualsTimeSeries.append(mcResults.infectedIndividualsTimeSeries).append("\n");
        this.removedIndividualsTimeSeries.append(mcResults.removedIndividualsTimeSeries).append("\n");

        this.scenarioCount++;
        return this;
    }

    @Override
    public final void reset() {
        this.expectedValue = new Samples();
        this.outbreakSize = new Samples();
        this.numRemoved = new Samples();
        this.numInfected = new Samples();
        this.rejectedScenarioCount = 0;
        this.scenarioCount = 0;

        this.infectedLocationsTimeSeries = new StringBuilder();
        this.infectedIndividualsTimeSeries = new StringBuilder();
        this.removedIndividualsTimeSeries = new StringBuilder();
    }

    @Override
    public final String toCsv() {

        double likelihood = this.expectedValue.getMean();
        if (this.expectedValue.getSize() == 0) {
            likelihood = MIN_VALUE;
        }

        return String.format("%f,%f,%f,%f,%f,%f", likelihood, outbreakSize.getMean(),
                             numInfected.getMean(), numInfected.getStdDev(),
                             numRemoved.getMean(), numRemoved.getStdDev());
    }

    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Scenarios counted = ").append(scenarioCount);

        //sb.append("LogLikelihood = ").append(getScore(false)).append("\n");
        sb.append("\nLogLikelihood = ").append(expectedValue.getMean());
        sb.append("\nOutbreak size = ").append(outbreakSize.getMean());
        sb.append(" \u00B1 ").append(outbreakSize.getStdDev()).append("\n");

        return sb.toString();
    }

    /**
     * Calculate the Monte Carlo score (the log-likelihood) for the simulation.
     * @return the log-likelihood.
     */
    public final double getScore() {
        if (dist != null) {

            // We want the log-likelihood here so we copy most of the code from MultinomialDistribution but  
            // neglecting the final Math.exp() call.
            final double[] p = dist.getP();
            final int numI = (int) Math.round(numInfected.getSum() * dist.getN() / (numInfected.getSum() + numRemoved.getSum()));
            final int numR = (int) Math.round(numRemoved.getSum() * dist.getN() / (numInfected.getSum() + numRemoved.getSum()));
            final int sumX = numI + numR;
            
            if (sumX == 0) {
                // epidemic died out, ignore it.
                log.warn("Could not calculate likelihood : {},{}", numI, numR);
                return MIN_VALUE;
            }
            final double sumXFact = broadwick.math.Factorial.lnFactorial(numI)
                                    + broadwick.math.Factorial.lnFactorial(numR);
            final double sumPX = (numI * Math.log(p[0])) + (numR * Math.log(p[1]));
            
            if (sumX != dist.getN()) {
                log.error("{}",
                          String.format("Cannot calculate the Likelihood for simulated (%d,%d); compared to observations (%d)",
                                        numI, numR, dist.getN()));
                return MIN_VALUE;
            } else {
                
                final double logLikelihood = broadwick.math.Factorial.lnFactorial(dist.getN()) - sumXFact + sumPX;
                log.debug("{}", String.format("logLikelihood : %f", logLikelihood));
                return logLikelihood;
            }
        }
        return Double.NEGATIVE_INFINITY;
    }
    
    
    
    @Override
    protected final void finalize() throws Throwable {
        expectedValue.clear();
        outbreakSize.clear();
        numRemoved.clear();
        numInfected.clear();

        super.finalize();
    }
    
    private Samples expectedValue;
    @Getter
    private Samples outbreakSize;
    @Getter
    private Samples numRemoved;
    @Getter
    private Samples numInfected;

    // Some time series plots of various measureables that are updated in the observer.step() method.
    @Getter
    private StringBuilder infectedLocationsTimeSeries;
    @Getter
    private StringBuilder infectedIndividualsTimeSeries;
    @Getter
    private StringBuilder removedIndividualsTimeSeries;

    // these variables are only used in this class, the join method keeps these statistics.
    @Getter
    private int rejectedScenarioCount;
    @Getter
    private int scenarioCount;

    private final MultinomialDistribution dist;
    private final static Double MIN_VALUE = Double.NEGATIVE_INFINITY; //-Double.MAX_VALUE;
}
