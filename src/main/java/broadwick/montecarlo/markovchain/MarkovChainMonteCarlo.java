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
package broadwick.montecarlo.markovchain;

import broadwick.BroadwickException;
import broadwick.io.FileOutput;
import broadwick.montecarlo.MonteCarlo;
import broadwick.montecarlo.MonteCarloResults;
import broadwick.montecarlo.MonteCarloScenario;
import broadwick.montecarlo.MonteCarloStep;
import broadwick.montecarlo.acceptor.MetropolisHastings;
import broadwick.montecarlo.acceptor.MonteCarloAcceptor;
import broadwick.montecarlo.markovchain.controller.MarkovChainController;
import broadwick.montecarlo.markovchain.controller.MarkovChainMaxNumStepController;
import broadwick.montecarlo.markovchain.observer.MarkovChainObserver;
import broadwick.rng.RNG;
import com.google.common.base.Throwables;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Monte Carlo class that is responsible for constructing Monte Carlo chains and the simulations thereon.
 */
@Slf4j
public class MarkovChainMonteCarlo {

    /**
     * Create a Monte Carlo instance.
     * @param model          The Monte Carlo Model to be run.
     * @param numSimulations The number of time the Monte Carlo model should be run at each step.
     * @param consumer       the object that will consume and aggregate the MC results.
     * @param generator      the object that will generate the Monte Carlo chain/path.
     */
    public MarkovChainMonteCarlo(final MonteCarloScenario model, final int numSimulations,
                                 final MonteCarloResults consumer,
                                 final MarkovStepGenerator generator) {
        this(model, numSimulations, consumer, new MarkovChainMaxNumStepController(1000), generator);
    }

    /**
     * Create a Monte Carlo instance.
     * @param model          The Monte Carlo Model to be run.
     * @param numSimulations The number of time the Monte Carlo model should be run at each step.
     * @param consumer       the object that will consume and aggregate the MC results.
     * @param controller     the controller object for this class.
     * @param generator      the object that will generate the Monte Carlo chain/path.
     */
    public MarkovChainMonteCarlo(final MonteCarloScenario model, final int numSimulations,
                                 final MonteCarloResults consumer, final MarkovChainController controller,
                                 final MarkovStepGenerator generator) {
        this.observers = new HashSet<>(1);
        this.model = model;
        this.numSimulations = numSimulations;
        this.consumer = consumer;
        this.mcController = controller;
        this.pathGenerator = generator;
        this.acceptor = new MetropolisHastings(GENERATOR.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    /**
     * Run the MonteCarlo Simulation.
     */
    public final void run() {

        try {
            for (MarkovChainObserver observer : observers) {
                observer.started();
            }

            log.info("Running Monte Carlo simulation with initial step {}", currentStep.toString());
            model.setStep(currentStep);

            MonteCarlo mc = new MonteCarlo(model, numSimulations);
            mc.setResultsConsumer(consumer);
            mc.run();
            MonteCarloResults prevResults = mc.getResults();
            for (MarkovChainObserver observer : observers) {
                observer.step();
                observer.takeMeasurements();
            }
            numStepsTaken++;

            while (mcController.goOn(this)) {
                int stepsSinceLastMeasurement = 0;
                final MonteCarloStep proposedStep = pathGenerator.generateNextStep(currentStep);
                model.setStep(proposedStep);

                mc = new MonteCarlo(model, numSimulations);
                mc.setResultsConsumer(consumer);
                mc.run();
                final MonteCarloResults currentResults = mc.getResults();

                lastStepAccepted = acceptor.accept(prevResults, currentResults);
                for (MarkovChainObserver observer : observers) {
                    observer.step();
                }
                
                if (lastStepAccepted) {
                    log.info("Accepted Monte Carlo step ({})", proposedStep.toString());
                    numAcceptedSteps++;
                    if (numStepsTaken > burnIn
                        && (thinningInterval == 0 || stepsSinceLastMeasurement % thinningInterval == 0)) {
                        stepsSinceLastMeasurement++;
                        for (MarkovChainObserver observer : observers) {
                            observer.takeMeasurements();
                        }
                    }
                    currentStep = proposedStep;
                    prevResults = currentResults;
                } else {
                    log.info("Rejected Monte Carlo step ({})", proposedStep.toString());
                }
                numStepsTaken++;
            }

            for (MarkovChainObserver observer : observers) {
                observer.finished();
            }
        } catch (Exception e) {
            log.error("Error running Monte Carlo simulation. {}", Throwables.getStackTraceAsString(e));
            throw new BroadwickException("Error running Monte Carlo simulation." + Throwables.getStackTraceAsString(e));
        }
    }
    
    public String getHeader() {
            return String.format("# Steps taken [1]\n# Current step accepted? [2]\n# Current step coordinates [3-%d]%n# Results for current step [%d-n]%n",
                                 2 + currentStep.getCoordinates().size(), 3 + currentStep.getCoordinates().size() );
    }

    /**
     * Add an observer to the list of observers.
     * @param observer the observer that is used to monitor/take measurements.
     * @return <tt>true</tt> if this object didn't already contain the observer.
     */
    public final boolean addObserver(final MarkovChainObserver observer) {
        observer.setMonteCarlo(this);
        return observers.add(observer);
    }
    
    @Getter
    private int numStepsTaken = 0;
    private int numSimulations;
    @Getter
    private boolean lastStepAccepted = true; // accept the first step!
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private int numAcceptedSteps = 0;
    @Setter
    private int burnIn = 0;
    @Setter
    private int thinningInterval = 0;
    @Setter
    private MonteCarloAcceptor acceptor;
    @Setter
    private MarkovChainController mcController;
    @Getter
    private Set<MarkovChainObserver> observers;
    @Getter
    private MonteCarloScenario model;
    private MarkovStepGenerator pathGenerator;
    private MonteCarloStep currentStep;
    @Getter
    private MonteCarloResults consumer;
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
    // TODO measure [auto]correlation function(s)
}
