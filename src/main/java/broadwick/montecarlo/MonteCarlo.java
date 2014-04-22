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
import broadwick.utils.CloneUtils;
import com.google.common.base.Throwables;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
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
     * @param simulation the simulation or scenario to be run.
     * @param numSimulations the number of times the simulation should be run.
     */
    public MonteCarlo(final MonteCarloScenario simulation,
                      final int numSimulations) {
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

        // Create the queue for the producer-consumer to use. It will take the producers longer to create results than it takes
        // consumers to process them so having a relavively small queue should be ok.
        final ArrayBlockingQueue<MonteCarloResults> q = new ArrayBlockingQueue<>(1000);
        final MonteCarloResults poison = new Poison();

        // Create the producer (the epdemics and generator of data) and the consumer (the gatherer of the data).
        final Thread producer = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    final int poolSize = Runtime.getRuntime().availableProcessors();
                    final ExecutorService es = Executors.newFixedThreadPool(poolSize);

                    final StopWatch sw = new StopWatch();
                    sw.start();
                    for (int i = 0; i < numSimulations; i++) {
                        es.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    //final StochasticEpidemic epidemic = new StochasticEpidemic((ConfigProperties) CloneUtils.deepClone(parameters), herdSizeDistrib, generator.getInteger(0, Integer.MAX_VALUE), maxRecordedBreakdownSize);
                                    MonteCarloScenario scenario = (MonteCarloScenario) CloneUtils.deepClone(simulation);
                                    MonteCarloResults results = scenario.run();
                                    q.put(results);

                                } catch (java.lang.InterruptedException e) {
                                    log.error(Throwables.getStackTraceAsString(e));
                                }
                            }
                        });
                    }
                    es.shutdown();
                    while (!es.isTerminated()) {
                        es.awaitTermination(1, TimeUnit.SECONDS);
                    }
                    q.put(poison);
                    sw.stop();
                    log.trace("Finished {} simulations in {}.", numSimulations, sw);
                } catch (InterruptedException ex) {
                    log.error(Throwables.getStackTraceAsString(ex));
                }
            }
        });

        final Thread consumer;
        consumer = new Thread(new Runnable() {

            @Override
            public void run() {
                MonteCarloResults results = null;
                final StopWatch sw = new StopWatch();
                sw.start();

                while (!(results instanceof Poison)) {
                    try {
                        results = q.take();

                        if (!(results instanceof Poison)) {
                            numSimulationsFound++;

                            // get the MonteCarloResults from the q and calculate the statistics on it.
                            resultsConsumer.join(results);
                        }

                    } catch (java.lang.InterruptedException e) {
                        log.error(Throwables.getStackTraceAsString(e));
                    }
                }
                sw.stop();
                log.trace("Analysed {} simulation results in {}.", numSimulationsFound, sw);

            }
            private int numSimulationsFound = 0;
        });

        producer.start();
        consumer.start();

        try {
            producer.join();
            consumer.join();
        } catch (java.lang.InterruptedException e) {
            log.error(Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * Get a copy of the results from the simulation.
     * @return the MonteCarloResults object that contains the results of all the simulations.
     */
    public final MonteCarloResults getResults() {
        return resultsConsumer;
    }

    private final MonteCarloScenario simulation;
    @Setter
    private MonteCarloResults resultsConsumer;
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
}
