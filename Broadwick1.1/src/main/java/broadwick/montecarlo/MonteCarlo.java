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
package broadwick.montecarlo;

import broadwick.io.FileOutput;
import broadwick.montecarlo.acceptor.Acceptor;
import broadwick.montecarlo.acceptor.MetropolisHastings;
import broadwick.montecarlo.path.PathGenerator;
import broadwick.montecarlo.path.Step;
import broadwick.rng.RNG;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Monte Carlo class that is responsible for constructing Monte Carlo chains and the simulations thereon.
 */
@Slf4j
public class MonteCarlo {

    /**
     * Create a Monte Carlo instance.
     * @param model     The Monte Carlo Model to be run.
     * @param generator the object that will generate the Monte Carlo chain/path.
     */
    public MonteCarlo(final McModel model, final PathGenerator generator) {
        this(model, new McMaxNumStepController(1000), generator);
    }

    /**
     * Create a Monte Carlo instance.
     * @param model      The Monte Carlo Model to be run.
     * @param controller the controller object for this class.
     * @param generator  the object that will generate the Monte Carlo chain/path.
     */
    public MonteCarlo(final McModel model, final McController controller,
                      final PathGenerator generator) {
        this.observers = new HashSet<>(1);
        this.model = model;
        this.mcController = controller;
        this.pathGenerator = generator;
        this.acceptor = new MetropolisHastings(GENERATOR.getInteger(Integer.MIN_VALUE, Integer.MAX_VALUE));
    }

    /**
     * Run the MonteCarlo Simulation.
     */
    public final void run() {
        
        Step currentStep = pathGenerator.getInitialStep();

        writer.write("# Steps taken [1]\n");
        writer.write("# Current step accepted? [2]\n");
        writer.write(String.format("# Current step coordinates [3-%d]\n", (2+currentStep.getCoordinates().size())));
        writer.write(String.format("# Results for current step [%d-n]\n", (3+currentStep.getCoordinates().size())));

        for (McObserver observer : observers) {
            observer.started();
        }

        log.info("Running Monte Carlo simulation with initial step {}", currentStep.toString());
        McResults prevResults = model.compute(currentStep);
        writer.write("%d,%d,%s,%s\n", numStepsTaken, 1, currentStep.toString(), prevResults.toCsv());
            
        while (mcController.goOn(this)) {
            numStepsTaken++;
            int stepsSinceLastMeasurement = 0;
            final Step proposedStep = pathGenerator.generateNextStep(currentStep);
            final McResults currentResults = model.compute(proposedStep);
            
            for (McObserver observer : observers) {
                observer.step();
            }

            final boolean accepted = acceptor.accept(prevResults, currentResults);

            writer.write("%d,%d,%s,%s\n", numStepsTaken, (accepted ? 1 : 0),
                         proposedStep.toString(), currentResults.toCsv());
            if (accepted) {
                log.info("Accepted Monte Carlo step {}", proposedStep.toString());
                numAcceptedSteps++;
                if (numStepsTaken > burnIn
                    && (thinningInterval == 0 || stepsSinceLastMeasurement % thinningInterval == 0)) {
                    stepsSinceLastMeasurement++;
                    for (McObserver observer : observers) {
                        observer.takeMeasurements();
                    }
                }
                currentStep = proposedStep;
                prevResults = currentResults;
            } else {
                log.info("Rejected Monte Carlo step {}", proposedStep.toString());
            }
        }

        for (McObserver observer : observers) {
            observer.finished();
        }
    }

    /**
     * Add an observer to the list of observers.
     * @param observer the observer that is used to monitor/take measurements.
     */
    public final void addObserver(final McObserver observer) {
        observers.add(observer);
    }
    @Getter
    private int numStepsTaken = 0;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private int numAcceptedSteps = 0;
    @Setter
    private int burnIn = 0;
    @Setter
    private int thinningInterval = 0;
    @Setter
    private Acceptor acceptor;
    @Setter
    private McController mcController;
    @Getter
    private Set<McObserver> observers;
    @Setter
    private FileOutput writer = new FileOutput();
    private McModel model;
    private PathGenerator pathGenerator;
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
    // TODO measure [auto]correlation function(s)
}
