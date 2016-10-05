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

import broadwick.BroadwickConstants;
import broadwick.data.Movement;
import broadwick.model.Model;
import broadwick.montecarlo.MonteCarloResults;
import broadwick.montecarlo.MonteCarloScenario;
import broadwick.montecarlo.acceptor.MonteCarloAcceptor;
import broadwick.montecarlo.markovchain.MarkovStepGenerator;
import broadwick.montecarlo.markovchain.SequentialMonteCarlo;
import broadwick.montecarlo.markovchain.controller.MarkovChainMaxNumStepController;
import broadwick.rng.RNG;
import com.google.common.base.Throwables;
import java.util.ArrayList;
import java.util.Collection;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.format.DateTimeFormat;

/**
 * A simple example on how to use a the stochastic solver of Broadwick to solve the SIR model.
 * <p>
 * We add some aded functionality that stops the simulation when the number of susceptibles reached zero or a maximum
 * set time is reached. If, after 40 time units, the simulation is still running we add a further 50 susceptibles to the
 * simulation.
 */
@Slf4j
public class App extends Model {

    @Override
    public final void init() {
        log.info("Initialising project");

        settings = new MyProjectSettings();
        settings.setTMax(getParameterValueAsDouble("tMax"))
                .setObservedNumInfected(getParameterValueAsInteger("numObservedInfected"))
                .setObservedNumRemoved(getParameterValueAsInteger("numObservedRemoved"));

        //log.info("beta = {}, rho = {}", betaVal, rhoVal);
        log.info("Max t = {}", settings.getTMax());

    }

    @Override
    public final void run() {
        log.info("Running project");

        try {

            // The model runs a Markov Chain Monte Carlo simulation where each step consists of several simulations of 
            // a Btb epidemic on a network of farms in NI. The movements between farms are drawn from movement 
            // distributions obtained by analysing the actual movements and similarly for the whole herd tests.
            final RNG generator = new RNG(RNG.Generator.Well19937c);

            // Generate the initial step of the Markov chain
            final MarkovStepGenerator pathGenerator = new MyMonteCarloPathGenerator(this.getPriors());

            // All the movements have their dates stored as the number of days from 1/1/1900, we will take
            // the movements from the database and transform them back. (Using Iterables.transform or Collections2.transform
            // crashes the JVM when we clone them later - this is a problem up to and including jdk8-102 and jre8-102)
            final int zeroDate = BroadwickConstants.getDate(DateTimeFormat.forPattern("D").parseDateTime("1"));

            final Collection<Movement> movements = new ArrayList<>();
            for (final Movement movement : this.getLookup().getMovements()) {
                movements.add(new Movement(movement.getId(),
                                           movement.getBatchSize(),
                                           movement.getDepartureDate() - zeroDate,
                                           movement.getDepartureId(),
                                           movement.getDestinationDate() - zeroDate,
                                           movement.getDestinationId(),
                                           movement.getMarketDate(),
                                           movement.getMarketId(),
                                           movement.getSpecies()));
            }

            final MonteCarloScenario scenario = new MyMonteCarloScenario(settings, movements);
            SequentialMonteCarlo smc;
            smc = new SequentialMonteCarlo(
                    this.getPriors(),
                    this.getParameterValueAsInteger("numParticles"),
                    scenario,
                    this.getParameterValueAsInteger("numScenarios"),
                    new MyMonteCarloScenarioResults(this.getParameterValueAsInteger("numObservedInfected"),
                                                    this.getParameterValueAsInteger("numObservedRemoved")),
                    new MarkovChainMaxNumStepController(this.getParameterValueAsInteger("numMcSteps")),
                    pathGenerator,
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
                        log.trace("{}", String.format("Math.log(r) < (%g-%g) [=%g]?",
                                                      newResult.getExpectedValue(), oldResult.getExpectedValue(),
                                                      ratio));

                        return Math.log(generator.getDouble()) < ratio;
                    }
                    return false;
                }
            });

            final MyMarkovChainObserver myMcObserver = new MyMarkovChainObserver();
            smc.addParticleObserver(myMcObserver);
            smc.run();

        } catch (NumberFormatException e) {
            log.error("Found error running scenarios. {}", Throwables.getStackTraceAsString(e));
        }

    }

    @Override
    public final void finalise() {
        log.info("Closing project");
    }

    private MyProjectSettings settings;
}
