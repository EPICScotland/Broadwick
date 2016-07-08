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
package broadwick.stochasticinf;

import broadwick.model.Model;
import broadwick.montecarlo.MonteCarloResults;
import lombok.extern.slf4j.Slf4j;
import com.google.common.base.Throwables;
import broadwick.rng.RNG;
import broadwick.montecarlo.MonteCarloScenario;
import broadwick.montecarlo.acceptor.MonteCarloAcceptor;
import broadwick.montecarlo.markovchain.SequentialMonteCarlo;
import broadwick.montecarlo.markovchain.controller.MarkovChainMaxNumStepController;

/**
 * This class demonstrates how to perform Bayesian inference for a stochastic model using an Adaptive step generation algorithm.
 */
@Slf4j
public class App extends Model {

    @Override
    public final void init() {
        log.info("Initialising project");

        outputFileName = getParameterValue("outputFile");
        log.info("Saving data to {}", outputFileName);

        mySettings = new MySettings(getParameterValueAsInteger("initialSusceptibles"),
                                    getParameterValueAsInteger("initialExposed1"),
                                    getParameterValueAsInteger("initialExposed2"),
                                    getParameterValueAsInteger("initialInfectious"),
                                    getParameterValueAsInteger("observedExposed"),
                                    getParameterValueAsInteger("observedInfectious"),
                                    getParameterValueAsDouble("tMax"));

        log.info("{}", mySettings.toString());
    }

    @Override
    public final void run() {
        log.info("Running project");

        try {

            // The model runs a Markov Chain Monte Carlo simulation where each step consists of several simulations of 
            // a Btb epidemic on a network of farms in NI. The movements between farms are drawn from movement 
            // distributions obtained by analysing the actual movements and similarly for the whole herd tests.
            final RNG generator = new RNG(RNG.Generator.Well19937c);

            log.info("{}", this.getPriors());

            final MonteCarloScenario scenario = new MyMonteCarloScenario(null, mySettings);

            final SequentialMonteCarlo smc = new SequentialMonteCarlo(
                    this.getPriors(),
                    this.getParameterValueAsInteger("numParticles"),
                    scenario,
                    this.getParameterValueAsInteger("numScenarios"),
                    new MyMonteCarloScenarioResults(mySettings),
                    new MarkovChainMaxNumStepController(this.getParameterValueAsInteger("numMcSteps")),
                    new MonteCarloAMPathGenerator(this.getPriors(),
                                                  this.getParameterValueAsDouble("percentageDeviation")),
                    new MonteCarloAcceptor() {
                @Override
                public boolean accept(final MonteCarloResults oldResult, final MonteCarloResults newResult) {

                    // In some situations, the likelihood for the first step may be negative infinity, in this case 
                    // accept the first finite value.
                    if (Double.isInfinite(oldResult.getExpectedValue()) && Double.isFinite(newResult.getExpectedValue())) {
                        return true;
                    }

                    if (Double.isFinite(newResult.getExpectedValue())) {
                        // we are using a log likelihood, so...                    
                        final double ratio = newResult.getExpectedValue() - oldResult.getExpectedValue();
                        log.trace("{}", String.format("Math.log(r) < (%g-%g)?",
                                                      newResult.getExpectedValue(), oldResult.getExpectedValue()));

                        return Math.log(generator.getDouble()) < ratio;
                    }
                    return false;
                }
            },
                    generator.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE));

            smc.run();

        } catch (NumberFormatException e) {
            log.error("Found error running scenarios. {}", Throwables.getStackTraceAsString(e));
        }
    }

    @Override
    public final void finalise() {
        log.info("Closing project");
    }

    private MySettings mySettings;
    private String outputFileName;
//    private double tMax;
//    private int initialSusceptibles;
//    private int initialExposed1;
//    private int initialExposed2;
//    private int initialInfectious;

}
