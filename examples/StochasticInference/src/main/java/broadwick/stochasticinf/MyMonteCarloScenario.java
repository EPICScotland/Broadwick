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

import broadwick.montecarlo.MonteCarloResults;
import broadwick.montecarlo.MonteCarloScenario;
import broadwick.montecarlo.MonteCarloStep;
import broadwick.rng.RNG;
import broadwick.stochastic.AmountManager;
import broadwick.stochastic.SimulationController;
import broadwick.stochastic.SimulationEvent;
import broadwick.stochastic.SimulationState;
import broadwick.stochastic.StochasticSimulator;
import broadwick.stochastic.TransitionKernel;
import broadwick.stochastic.algorithms.TauLeaping;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Run a single simulation/scenario on a static network.
 */
@Slf4j
public class MyMonteCarloScenario extends MonteCarloScenario {

    MyMonteCarloScenario(final MonteCarloStep step, final MySettings settings) {

        super();
        this.step = step;
        this.settings = settings;
    }

    /**
     * Initialise the scenario by creating the stochastic simulator, transition kernel etc.
     */
    private void init() {

        // we will need an amount manager to keep track of the number of S/I/R in our simulation.
        this.amountManager = new MyAmountManager(settings.getInitialSusceptibles(),
                                                 settings.getInitialExposed1(),
                                                 settings.getInitialExposed2(),
                                                 settings.getInitialInfectious(),
                                                 this);

        // The transition kernel defines the transition rates between each state.
        this.kernel = new TransitionKernel();
        updateKernel();

        this.simulator = new TauLeaping(amountManager, kernel);

        // we will need an observer to 'observe' our simulation and record the simulation states. First we
        // remove any obervers attached to the simulator (we can have more than one observer!).
        // simulator.getObservers().clear();
        //final MyObserver observer = new MyObserver(simulator, outputFileName);
        //simulator.addObserver(observer);
        
        // Create a simple controller object to tell the simulator when to stop.
        final SimulationController controller = new SimulationController() {
            @Override
            public boolean goOn(StochasticSimulator process) {
                return ((MyAmountManager) process.getAmountManager()).getNumberOfSusceptibles() > 0
                       && process.getCurrentTime() < settings.getMaxT();
            }
        };
        this.simulator.setController(controller);
        
        results = new MyMonteCarloScenarioResults(settings);
    }

    @Override
    public final MonteCarloResults run(int seed) {
        this.generator.seed(seed);
        
        init();

        this.simulator.run();
        
         // Calculate measureables and update results object
        results.getNumberOfSusceptibles().add(amountManager.getNumberOfSusceptibles());
        results.getNumberOfExposed1().add(amountManager.getNumberOfExposed1());
        results.getNumberOfExposed2().add(amountManager.getNumberOfExposed2());
        results.getNumberOfInfectious().add(amountManager.getNumberOfInfectious());
        return results;
    }

    @Override
    public final MonteCarloScenario copyOf() {
        return new MyMonteCarloScenario(step, settings);
    }

    /**
     * Update the transition kernel. This method actually clears the transition kernel and re-creates it given the
     * number of susceptibles, infectious and removed individuals.
     */
    protected final void updateKernel() {
        kernel.clear();

        final Map<String, Double> parameters = step.getCoordinates();
        
        
        // there are amountManager.getNumberOfSusceptibles() of these events so we multiply the rate by
        // amountManager.getNumberOfSusceptibles()
        kernel.addToKernel(new SimulationEvent(amountManager.getSusceptible(), amountManager.getExposed1()),
                           amountManager.getNumberOfSusceptibles() * amountManager.getNumberOfInfectious() * parameters.get("beta"));
        // there are amountManager.getNumberOfExposed1() of these events so we multiply the rate by
        // amountManager.getNumberOfExposed1()
        kernel.addToKernel(new SimulationEvent(amountManager.getExposed1(), amountManager.getExposed2()),
                           amountManager.getNumberOfExposed1() * parameters.get("sigma"));
        // there are amountManager.getNumberOfExposed2() of these events so we multiply the rate by
        // amountManager.getNumberOfExposed2()
        kernel.addToKernel(new SimulationEvent(amountManager.getExposed2(), amountManager.getInfectious()),
                           amountManager.getNumberOfExposed2() * parameters.get("gamma"));
    }

    @Getter
    private final RNG generator = new RNG(RNG.Generator.Well19937c);
    private final MySettings settings;
    private MyMonteCarloScenarioResults results;
    private TransitionKernel kernel;
    private MyAmountManager amountManager;
    private StochasticSimulator simulator;
}

/**
 * Create the amount manager that manages the populations of Susceptble/Infected/Removed.
 */
@Slf4j
class MyAmountManager implements AmountManager {

    /**
     * Create the amount manager. Normally we would define simulation states and event in separate classes but since, in
     * this case, they are quite simple we will define them here and allow accessors to get access to them.
     * @param s        the number of susceptibles in the simulation.
     * @param e1       the number of exposed (class 1) in the simulation.
     * @param e2       the number of exposed (class 2) in the simulation.
     * @param i        the number of infectious in the simulation.
     * @param scenario the Monte Carlo simulation which is using this manager.
     */
    MyAmountManager(final int s, final int e1, final int e2, final int i, final MyMonteCarloScenario scenario) {
        susceptible = new SimulationState() {
            @Override
            public String getStateName() {
                return "SUSCEPTIBLE";
            }
        };

        exposed1 = new SimulationState() {
            @Override
            public String getStateName() {
                return "EXPOSED_1";
            }
        };

        exposed2 = new SimulationState() {
            @Override
            public String getStateName() {
                return "EXPOSED_2";
            }
        };

        infectious = new SimulationState() {
            @Override
            public String getStateName() {
                return "INFECTIOUS";
            }
        };

        numberOfSusceptibles = s;
        numberOfExposed1 = e1;
        numberOfExposed2 = e2;
        numberOfInfectious = i;
        this.model = scenario;
    }

    @Override
    public void performEvent(final SimulationEvent event, final int times) {
        log.trace("{}", String.format("Performing event %s->%s %d times.",
                                      event.getInitialState().getStateName(), event.getFinalState().getStateName(), times));

        if (event.getInitialState().equals(susceptible) && event.getFinalState().equals(exposed1)) {
            // the event was a S->E1 so decrease numberOfSusceptibles and increase numberOfExposed1
            final int numToBeRemoved = Math.max(0, numberOfSusceptibles - times);
            numberOfSusceptibles = numberOfSusceptibles - numToBeRemoved;
            numberOfExposed1 = numberOfExposed1 + numToBeRemoved;
        } else if (event.getInitialState().equals(exposed1) && event.getFinalState().equals(exposed2)) {
            // the event was a E1->E2 so decrease numberOfExposed1 and increase numberOfExposed2
            final int numToBeRemoved = Math.max(0, numberOfExposed1 - times);
            numberOfExposed1 = numberOfExposed1 - numToBeRemoved;
            numberOfExposed2 = numberOfExposed2 + numToBeRemoved;
        } else if (event.getInitialState().equals(exposed2) && event.getFinalState().equals(infectious)) {
            // the event was a E2->I so decrease numberOfExposed2 and increase numberOfInfectious
            final int numToBeRemoved = Math.max(0, numberOfExposed2 - times);
            numberOfExposed2 = numberOfExposed2 - numToBeRemoved;
            numberOfInfectious = numberOfInfectious + numToBeRemoved;
        } else {
            log.error("Event {} undefined.", event.toString());
        }

        // Now that the populations of S/I/R have changed we need to update our kernel
        model.updateKernel();
    }

    @Override
    public String toVerboseString() {
        return String.format("%d\t%d\t%d\t%d%n",
                             numberOfSusceptibles, numberOfExposed1, numberOfExposed2, numberOfInfectious);
    }

    @Override
    public void resetAmount() {
        // We could save the state of the simulation here. We won't do it in this simulation but in general one would
        // save the internal states of all internal variables e.g.
        //        numberOfSusceptiblesSaved = initial value of susceptibles
        //        numberOfExposed1Saved = initial value of exposed1;
        //        numberOfExposed2Saved = initial value of exposed2;
        //        numberOfInfectiousSaved = initial value of infectious
    }

    @Override
    public void save() {

        // We could save the state of the simulation here. We won't do it in this simulation but in general one would
        // save the internal states of all internal variables e.g.
        //        numberOfSusceptiblesSaved = numberOfSusceptibles;
        //        numberOfExposed1Saved = numberOfExposed1;
        //        numberOfExposed2Saved = numberOfExposed2;
        //        numberOfInfectiousSaved = numberOfInfectious;
    }

    @Override
    public void rollback() {
        // Here we would recover the last saved values. We won't do it in this simulation but in general one would
        // use the savedinternal states of all internal variables e.g.
        //        numberOfSusceptibles = numberOfSusceptiblesSaved;
        //        numberOfExposed1 = numberOfExposed1Saved;
        //        numberOfExposed2 = numberOfExposed2Saved;
        //        numberOfInfectious = numberOfInfectiousSaved;
    }

    @Getter
    @Setter
    private int numberOfSusceptibles;
    @Getter
    private int numberOfExposed1;
    @Getter
    private int numberOfExposed2;
    @Getter
    private int numberOfInfectious;

    @Getter
    private final SimulationState susceptible;
    @Getter
    private final SimulationState exposed1;
    @Getter
    private final SimulationState exposed2;
    @Getter
    private final SimulationState infectious;
    private final MyMonteCarloScenario model;

}
