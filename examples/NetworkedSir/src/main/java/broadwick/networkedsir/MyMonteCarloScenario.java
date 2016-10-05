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

import broadwick.data.Movement;
import broadwick.montecarlo.MonteCarloResults;
import broadwick.montecarlo.MonteCarloScenario;
import broadwick.statistics.distributions.HypergeometricDistribution;
import broadwick.rng.RNG;
import broadwick.stochastic.SimulationController;
import broadwick.stochastic.SimulationEvent;
import broadwick.stochastic.StochasticSimulator;
import broadwick.stochastic.TransitionKernel;
import broadwick.stochastic.algorithms.TauLeapingFixedStep;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Run a single scenario (simulation).
 */
@Slf4j
public class MyMonteCarloScenario extends MonteCarloScenario {

    MyMonteCarloScenario(final MyProjectSettings settings,
                         final Collection<Movement> movements) {

        super();
        this.settings = settings;
        this.results = new MyMonteCarloScenarioResults(settings.getObservedNumInfected(), settings.getObservedNumRemoved());
        this.movements = movements;
        this.currentMovements = new HashMap<>();
    }

    @Override
    public final MonteCarloResults run(int seed) {
        log.trace("Running scenario");
        generator.seed(seed);

        // we will need an amount manager to keep track of the number of S/I/R in our simulation.
        amountManager = new MyAmountManager(this);

        // infect an individual at location 0.
        initialiseInfections();

        // The transition kernel defines the transition rates between each state.
        kernel = new TransitionKernel();

        simulator = new TauLeapingFixedStep(amountManager, kernel, 1);

        // we will need an observer to 'observe' our simulation and record the simulation states. First we
        // remove any obervers attached to the simulator (we can have more than one observer!).
        simulator.getObservers().clear();
        final MyMonteCarloScenarioObserver observer = new MyMonteCarloScenarioObserver(simulator, this);
        simulator.addObserver(observer);

        // Create a simple controller object to tell the simulator when to stop.
        final SimulationController controller = new MySimulationController(settings.getTMax());
        simulator.setController(controller);

        updateKernel();

        simulator.run();

        return results;
    }

    /**
     * Update the transition kernel. This method actually clears the transition kernel and re-creates it given the
     * number of susceptibles, infectious and removed individuals.
     */
    protected final void updateKernel() {
        kernel.clear();

        // Update the kernel to include the I-R and S-I events
        for (final Map.Entry<String, Collection<Agent>> entry : amountManager.getInfectedLocations().entrySet()) {
            final String locationId = entry.getKey();
            // Assume the mean number of the individuals is 120 with a std dev of 40
            final int numSusAtLocation = ((int) generator.getGaussian(settings.getMeanNumIndividualsAtLocation(),
                                                                      settings.getStdDevNumIndividualsAtLocation()))
                                         - entry.getValue().size();

            for (final Agent agent : entry.getValue()) {
                switch (agent.getState().getStateName()) {

                    case "INFECTIOUS":
                        // Add S->E event, since the newly infected agent does not have an id (we're not tracking 
                        // susceptible animals) we will give it an empty one and let the event handler deal with it.

                        final Agent susceptibleAgent = new Agent("", locationId, amountManager.getSusceptible());
                        final Agent newInfection = new Agent(Integer.toString(lastUsedAgentId++), locationId,
                                                       amountManager.getInfectious());
                        final Agent removedAgent = new Agent("", locationId, amountManager.getRemoved());

                        kernel.addToKernel(new SimulationEvent(susceptibleAgent, newInfection),
                                           numSusAtLocation * step.getCoordinates().get("beta"));
                        kernel.addToKernel(new SimulationEvent(agent, removedAgent), step.getCoordinates().get("sigma"));

                        break;
                }
            }
        }

        currentMovements.clear();

        final Predicate p = (Predicate<Movement>) (Movement m) -> m.getDepartureDate() == Math.round(simulator.getCurrentTime())
                                                            && amountManager.getInfectedLocations().containsKey(m.getDepartureId());
        for (final Object o : Iterables.filter(this.getMovements(), p)) {
            final Movement m = (Movement) o;
            // How many infected agents will be moved

            final int numInfectionsAtLocation = amountManager.getInfectedLocations().get(m.getDepartureId()).size();
            if (numInfectionsAtLocation > 0) {

                int locationSize = 0;
                while (locationSize <= numInfectionsAtLocation) {
                    locationSize = (int) (generator.getGaussian(settings.getMeanNumIndividualsAtLocation(),
                                                                settings.getStdDevNumIndividualsAtLocation()));
                }
                int movementSize = 0;
                while (movementSize <= 0) {
                    movementSize = (int) (generator.getGaussian(settings.getMeanNumIndividualsMoved(),
                                                                settings.getStdDevNumIndividualsMoved()));
                }

                Collection<Agent> agentsToMove = new ArrayList<>();
                log.trace("Moving {} individuals from {}", movementSize, m.getDepartureId());

                if (movementSize <= numInfectionsAtLocation) {
                    final int infectedAgentsToMove = new HypergeometricDistribution(locationSize, numInfectionsAtLocation, movementSize).sample();

                    agentsToMove.addAll(generator.selectManyOf(amountManager.getInfectedLocations().get(m.getDepartureId()),
                                                               infectedAgentsToMove));

                } else {
                    // move all the infecteds
                    agentsToMove = amountManager.getInfectedLocations().get(m.getDepartureId());
                }
                if (!agentsToMove.isEmpty()) {
                    log.debug("{}", String.format("Moving %d infections from %s to %s", agentsToMove.size(),
                                                                                        m.getDepartureId(), m.getDestinationId()));
                    
                    // Update the collection of movements of infected animals              
                    for (final Agent agent : agentsToMove) {
                        currentMovements.put(agent, m.getDestinationId());

                        final Agent newInfection = new Agent(Integer.toString(lastUsedAgentId++), m.getDestinationId(),
                                amountManager.getInfectious());
                        final Agent removedAgent = new Agent("", m.getDestinationId(), amountManager.getRemoved());

                        final int numSusAtLocation = (int) generator.getGaussian(settings.getMeanNumIndividualsAtLocation(),
                                                                                 settings.getStdDevNumIndividualsAtLocation());
                        
                        kernel.addToKernel(new SimulationEvent(agent, newInfection),
                                           numSusAtLocation * step.getCoordinates().get("beta"));
                        kernel.addToKernel(new SimulationEvent(agent, removedAgent), step.getCoordinates().get("sigma"));

                    }
                }
            }
        }

        if (log.isTraceEnabled()) {
            log.trace("Kernel = {}", kernel.toString());
        }
    }

    /**
     * Set the initial infection states of the cattle and badgers.
     * @param generator the random number generator to use.
     */
    private void initialiseInfections() {
        final Agent agent = new Agent(Integer.toString(lastUsedAgentId), "0", amountManager.getInfectious());
        amountManager.getInfectedAgents().add(agent);

        Collection<Agent> agents = amountManager.getInfectedLocations().get(agent.getLocation());
        if (agents == null) {
            agents = new ArrayList<>();
        }
        agents.add(agent);
        amountManager.getInfectedLocations().put("2", agents);
    }

    @Override
    protected final void finalize() throws Throwable {
        currentMovements.clear();

        super.finalize();
    }

    private TransitionKernel kernel;
    private StochasticSimulator simulator;
    private MyAmountManager amountManager;
    private int lastUsedAgentId = 0;
    private final MyProjectSettings settings;
    @Getter
    private final Iterable<Movement> movements;
    @Getter
    private final RNG generator = new RNG(RNG.Generator.Well19937c);
    @Getter
    private final MyMonteCarloScenarioResults results;
    @Getter
    private final Map<Agent, String> currentMovements;

}
