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
 */package broadwick.montecarlo.markovchain;

import broadwick.BroadwickVersion;
import broadwick.config.generated.Prior;
import broadwick.config.generated.UniformPrior;
import broadwick.io.FileOutput;
import broadwick.montecarlo.MonteCarloResults;
import broadwick.montecarlo.MonteCarloScenario;
import broadwick.montecarlo.MonteCarloStep;
import broadwick.montecarlo.acceptor.MonteCarloAcceptor;
import broadwick.montecarlo.markovchain.controller.MarkovChainController;
import broadwick.montecarlo.markovchain.observer.MarkovChainObserver;
import broadwick.rng.RNG;
import broadwick.statistics.distributions.Uniform;
import broadwick.utils.CloneUtils;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

/**
 * Implementation of the sequential Monte Carlo method. Several particles (collection of parameters) are sampled from a
 * distribution as a starting point for a Markov Chain, each point in the Markov Chain will run a Monte Carlo
 * simulation.
 */
@Slf4j
public class SequentialMonteCarlo {

    /**
     * Create a SMC object that will create several MCMC objects each with a different starting point.
     * @param priors        the priors from which the initial starting points will be drawn.
     * @param numParticles  the number of particles (walkers) to create.
     * @param scenario      the Monte Carlo scenario to be run at each step.
     * @param numScenarios  the number of scenarios to be run at each step.
     * @param results       the Monte Carlo Results object that will gather the results of the scenarios.
     * @param controller    the controller object that will tell the MCMC to stop.
     * @param pathGenerator the generator function for creating proposal steps.
     * @param acceptor      the object that specifies whether or not a proposed step is accepted.
     */
    public SequentialMonteCarlo(final Collection<Prior> priors,
                                final int numParticles,
                                final MonteCarloScenario scenario,
                                final int numScenarios,
                                final MonteCarloResults results,
                                final MarkovChainController controller,
                                final MarkovStepGenerator pathGenerator,
                                final MonteCarloAcceptor acceptor) {

        this.priors = priors;
        this.numParticles = numParticles;
        this.scenario = scenario;
        this.numScenarios = numScenarios;
        this.results = results;
        this.controller = controller;
        this.pathGenerator = pathGenerator;
        this.acceptor = acceptor;
        this.observers = new HashSet<>(1);

        generator = new RNG(RNG.Generator.Well19937c);
    }

    /**
     * Create a SMC object that will create several MCMC objects each with a different starting point.
     * @param priors        the priors from which the initial starting points will be drawn.
     * @param numParticles  the number of particles (walkers) to create.
     * @param scenario      the Monte Carlo scenario to be run at each step.
     * @param numScenarios  the number of scenarios to be run at each step.
     * @param results       the Monte Carlo Results object that will gather the results of the scenarios.
     * @param controller    the controller object that will tell the MCMC to stop.
     * @param pathGenerator the generator function for creating proposal steps.
     * @param acceptor      the object that specifies whether or not a proposed step is accepted.
     * @param seed          the seed for the RNG.
     */
    public SequentialMonteCarlo(final Collection<Prior> priors,
                                final int numParticles,
                                final MonteCarloScenario scenario,
                                final int numScenarios,
                                final MonteCarloResults results,
                                final MarkovChainController controller,
                                final MarkovStepGenerator pathGenerator,
                                final MonteCarloAcceptor acceptor,
                                final int seed) {

        this.priors = priors;
        this.numParticles = numParticles;
        this.scenario = scenario;
        this.numScenarios = numScenarios;
        this.results = results;
        this.controller = controller;
        this.pathGenerator = pathGenerator;
        this.acceptor = acceptor;
        this.observers = new HashSet<>(1);

        generator = new RNG(RNG.Generator.Well19937c);
        generator.seed(seed);
    }

    /**
     * Run the MonteCarlo Simulation.
     */
    public final void run() {

        try {
            final int poolSize = Runtime.getRuntime().availableProcessors();
            final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("SMC_particle-%d")
                    .setDaemon(true)
                    .build();
            final ExecutorService es = Executors.newFixedThreadPool(poolSize, threadFactory);
            
            final StopWatch sw = new StopWatch();
            sw.start();
            for (int i = 0; i < numParticles; i++) {
                final int id = i;
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            // sample the initial particles, ignore the initialStep of the prior.
                            final Map<String, Double> initialVals = new LinkedHashMap<>();
                            for (final Prior prior : priors) {
                                //TODO - this only works for uniform priors - need to find a neat way of making this 
                                // list of priors a list of actual distribution objects e.g. Uniform, TruncatedNormalDistribution
                                final UniformPrior uniformPrior = (UniformPrior) prior;
                                initialVals.put(uniformPrior.getId(), (new Uniform(uniformPrior.getMin(), uniformPrior.getMax(),
                                                                                   generator.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE))).sample());
                            }
                            final MonteCarloStep step = new MonteCarloStep(initialVals);

                            final MonteCarloScenario mc = scenario.copyOf();
                            mc.setStep(step);

                            // construct and run the MCMC object for this particle
                            final MarkovChainMonteCarlo mcmc = new MarkovChainMonteCarlo(
                                    mc,
                                    numScenarios,
                                    (MonteCarloResults) CloneUtils.deepClone(results),
                                    (MarkovChainController) CloneUtils.deepClone(controller),
                                    (MarkovStepGenerator) CloneUtils.deepClone(pathGenerator), step, id);
                            mcmc.setAcceptor(acceptor);
                            mcmc.addObserver(new SmcObserver(String.format("particle_%d.dat", id)));
                            for (final MarkovChainObserver observer : observers) {
                                mcmc.addObserver((MarkovChainObserver) CloneUtils.deepClone(observer));
                            }
                            mcmc.run();
                        } catch (Exception e) {
                            log.error("Error running MarkovChainMonteCarlo {}", Throwables.getStackTraceAsString(e));
                        }
                    }
                });
            }
            es.shutdown();
            while (!es.isTerminated()) {
                es.awaitTermination(1, TimeUnit.SECONDS);
            }

            sw.stop();
            log.info("Finished SMC in {}.", sw);
        } catch (Exception ex) {
            log.error("Monte Carlo simulation error: {}", Throwables.getStackTraceAsString(ex));
        }

        //TODO - add method to reject the particle......
    }

    /**
     * Add an observer to the list of observers.
     * @param observer the observer that is used to monitor/take measurements.
     * @return <tt>true</tt> if this object didn't already contain the observer.
     */
    public final boolean addParticleObserver(final MarkovChainObserver observer) {
        return observers.add(observer);
    }

    private final Set<MarkovChainObserver> observers;
    private final int numParticles;
    private final MonteCarloScenario scenario;
    private final int numScenarios;
    private final MonteCarloResults results;
    private final MarkovChainController controller;
    private final MarkovStepGenerator pathGenerator;
    private final MonteCarloAcceptor acceptor;
    private final Collection<Prior> priors;
    private final RNG generator;
}

@Slf4j
@EqualsAndHashCode(callSuper = false)
class SmcObserver extends MarkovChainObserver {

    SmcObserver(final String filename) {
        super();
        smcOutputFile = new FileOutput(filename, false, false);
    }

    @Override
    public void started() {
        smcOutputFile.write("# Broadwick version %s\n", BroadwickVersion.getVersionAndTimeStamp());
        smcOutputFile.write(this.monteCarlo.getHeader());
        smcOutputFile.flush();
    }

    @Override
    public void step() {
        final boolean accepted = this.monteCarlo.isLastStepAccepted();
        smcOutputFile.write("%d,%d,%s,%s\n", this.monteCarlo.getNumStepsTaken(), accepted ? 1 : 0,
                            this.monteCarlo.getModel().getStep().toString(),
                            this.monteCarlo.getConsumer().toCsv());

        smcOutputFile.flush();
    }

    @Override
    public void takeMeasurements() {
    }

    @Override
    public void finished() {
        smcOutputFile.close();
    }

    private final FileOutput smcOutputFile;
}
