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

import broadwick.BroadwickException;
import broadwick.rng.RNG;
import broadwick.statistics.Samples;
import broadwick.utils.CloneUtils;
import com.google.common.base.Throwables;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

/**
 * An implementation of the Monte Carlo method, it runs many MonteCarloSimulations and gathers the statistics of their
 * outputs. Note, each simulation object is cloned and each of these clones are run independently
 * <code>numSimulations</code> times.
 */
@Slf4j
public class MonteCarlo {

    /**
     * Create a Monte Carlo object that is capable of running <code>numSimulation</code> MonteCarloScenarios.
     * @param simulation     the simulation or scenario to be run.
     * @param numSimulations the number of times the simulation should be run.
     */
    public MonteCarlo(final MonteCarloScenario simulation, final int numSimulations) {
        this.simulation = simulation;
        this.numSimulations = numSimulations;
        this.resultsConsumer = new MonteCarloDefaultResults();
    }

    /**
     * Run the Monte Carlo simulations. Two threads (a producer and consumer) are created to asynchronously run the
     * simulations (the producer) and to handle the results from each simulation as they are calculated (the consumer).
     * The producer thread uses an execution pool to manage running each simulation and places the results on a queue
     * which is monitored by a consumer thread to calculate the posterior distributions for the Monte Carlo run.
     */
    public final void run() {
        final ArrayBlockingQueue<MonteCarloResults> queue = new ArrayBlockingQueue<>(numSimulations + 1);
        try {

            //Creating Producer and Consumer Thread
            final Thread producer = new Thread(new Producer(queue, simulation, numSimulations));
            final Thread consumer = new Thread(new Consumer(queue, resultsConsumer));
            producer.start();
            consumer.start();

            producer.join();
            consumer.join();
        } catch (Exception e) {
            log.error("Error joining Monte Carlo results {}", Throwables.getStackTraceAsString(e));
            throw new BroadwickException("Failed to run Monte Carlo simulation. " + Throwables.getStackTraceAsString(e));
        }

        queue.clear();
    }

    /**
     * Get a copy of the results from the simulation. This method MUST return a copy of the results NOT a reference.
     * @return the MonteCarloResults object that contains the results of all the simulations.
     */
    public final MonteCarloResults getResults() {
        return CloneUtils.deepClone(resultsConsumer);
    }

    /**
     * Set the consumer object for this Monte Carlo simulation. A consumer is simply a MonteCarloResults class that
     * 'joins' each result that is consumes so that statistics can be calculated at the end of the simulation.
     * @param consumer the results object to use as a consumer.
     */
    public final void setResultsConsumer(final MonteCarloResults consumer) {
        resultsConsumer = consumer;
        resultsConsumer.reset();
    }

    private final MonteCarloScenario simulation;
    private MonteCarloResults resultsConsumer;
    private final int numSimulations;
}

/**
 * This class takes (or consumes) the results of a single simulation of the model and saves the results in a manner to
 * allow statistics to be generated from the Monte Carlo simulation.
 */
@Slf4j
class Consumer implements Runnable {

    /**
     * Create the consumer object.
     * @param queue         the queue that the consumer should check for values to consume.
     * @param joinedResults the MonteCarloResults object to which the consumed results will be added.
     */
    public Consumer(final ArrayBlockingQueue<MonteCarloResults> queue, final MonteCarloResults joinedResults) {
        this.queue = queue;
        this.joinedResults = joinedResults;
        joinedResults.reset();
    }

    @Override
    public void run() {
        log.trace("Starting Monte Carlo results consumer thread");

        // it might seem that we should reset (or initialise) the consumer results object but
        // the consumer object is created for each Monte Carlo step so is really not strictly necessary.
        joinedResults.reset();
        final StopWatch sw = new StopWatch();
        sw.start();

        try {
            MonteCarloResults results = null;
            while (!(results instanceof Poison)) {
                results = queue.take();

                if (!(results instanceof Poison)) {
                    numSimulationsFound++;

                    // get the MonteCarloResults from the q and calculate the statistics on it.
                    joinedResults.join(results);

                    if (log.isTraceEnabled()) {
                        log.trace("Monte Carlo consumer: consumed {}", results.getSamples().getSummary());
                        log.trace("Monte Carlo consumer: consumed {} results, expected value={}",
                                  numSimulationsFound, joinedResults.getExpectedValue());
                    }

                    // we no longer require the results so allow the memory to be freed.
                    results = null;
                }
            }
        } catch (java.lang.InterruptedException e) {
            log.error("Error consuming Monte Carlo Results {}", Throwables.getStackTraceAsString(e));
        }
        sw.stop();
        log.debug("Analysed {} simulation results in {}.", numSimulationsFound, sw);
    }

    private final ArrayBlockingQueue<MonteCarloResults> queue;
    private final MonteCarloResults joinedResults;
    private int numSimulationsFound = 0;
}

/**
 * This class runs a single simulation of the model to 'produce' results (hence the name). This runnable class is run
 * many times to create a Monte Carlo simulation.
 */
@Slf4j
class Producer implements Runnable {

    /**
     * Create the producer object.
     * @param queue          the queue on which the producers results are added.
     * @param simulation     the model that will be run to produce results.
     * @param numSimulations the number of simulations that are to be run and placed in the quque.
     */
    public Producer(final ArrayBlockingQueue<MonteCarloResults> queue, final MonteCarloScenario simulation,
                    final int numSimulations) {
        this.queue = queue;
        this.simulation = simulation.copyOf();
        this.numSimulations = numSimulations;
    }

    @Override
    public void run() {
        log.trace("Starting Monte Carlo results producer thread");
        try {
            final int poolSize = Runtime.getRuntime().availableProcessors();
            final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setNameFormat("MCScenarioProducer-%d")
                    .setDaemon(true)
                    .build();
            final ExecutorService es = Executors.newFixedThreadPool(poolSize, threadFactory);
            final RNG generator = new RNG(RNG.Generator.Well44497b);

            final StopWatch sw = new StopWatch();
            sw.start();
            for (int i = 0; i < numSimulations; i++) {
                final int scenarioId = i;
                es.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            log.trace("Monte Carlo producer: creating scenario object");
                            final MonteCarloScenario scenario = simulation.copyOf();
                            scenario.setId(scenarioId);
                            final MonteCarloResults results = scenario.run(generator.getInteger(0, Integer.MAX_VALUE - 1));
                            log.trace("Monte Carlo producer: generated results {}", results.getExpectedValue());
                            queue.put(results);
                        } catch (Exception e) {
                            log.error("Error running Monte Carlo simulation {}", Throwables.getStackTraceAsString(e));
                        }
                    }
                });
            }
            es.shutdown();
            while (!es.isTerminated()) {
                es.awaitTermination(1, TimeUnit.SECONDS);
            }
            queue.put(new Poison());

            sw.stop();
            log.info("Finished {} simulations in {}.", numSimulations, sw);
        } catch (Exception ex) {
            log.error("Monte Carlo simulation error: {}", Throwables.getStackTraceAsString(ex));
        }
    }

    private final ArrayBlockingQueue<MonteCarloResults> queue;
    private final MonteCarloScenario simulation;
    private final int numSimulations;
}

/**
 * Poison object that causes the consumer of the MonteCarlo results to stop looking for objects on the queue.
 */
class Poison implements MonteCarloResults {

    @Override
    public double getExpectedValue() {
        return Double.NEGATIVE_INFINITY;
    }

    @Override
    public Samples getSamples() {
        return new Samples();
    }

    @Override
    public String toCsv() {
        return "POISON";
    }

    @Override
    public final MonteCarloResults join(final MonteCarloResults results) {
        return results;
    }

    @Override
    public void reset() {
        // do nothing - this is a poison pill
    }
}
