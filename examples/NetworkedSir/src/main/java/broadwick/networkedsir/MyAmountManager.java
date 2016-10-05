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
import broadwick.stochastic.AmountManager;
import broadwick.stochastic.SimulationEvent;
import broadwick.stochastic.SimulationState;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Create the amount manager that manages the populations of Susceptble/Infected/Removed.
 */
@Slf4j
class MyAmountManager implements AmountManager {

    /**
     * Create the amount manager. Normally we would define simulation states and event in separate classes but since, in
     * this case, they are quite simple we will define them here and allow accessors to get access to them.
     * @param model the model is told to update the transition probabilities when the numbers of S/I/R are changed.
     */
    MyAmountManager(final MyMonteCarloScenario scenario) {
        susceptible = new SimulationState() {
            @Override
            public String getStateName() {
                return "SUSCEPTIBLE";
            }
        };

        infectious = new SimulationState() {
            @Override
            public String getStateName() {
                return "INFECTIOUS";
            }
        };

        removed = new SimulationState() {
            @Override
            public String getStateName() {
                return "REMOVED";
            }
        };

        this.scenario = scenario;
        infectedAgents = new ArrayList<>();
        removedAgents = new ArrayList<>();
        infectedLocations = new HashMap<>();
    }

    @Override
    public void performEvent(final SimulationEvent event, final int times) {

        final Agent initialstateAgent = (Agent) event.getInitialState();

        if (initialstateAgent.getState().getStateName().equalsIgnoreCase(susceptible.getStateName())) {
            // we have a new infection.

            final Agent infectedAgent = (Agent) event.getFinalState();
            final String infectedLocation = infectedAgent.getLocation();
            Collection<Agent> infectionsAtLocation = infectedLocations.get(infectedLocation);
            if (infectionsAtLocation == null) {
                infectionsAtLocation = new ArrayList<>();
            }
            infectionsAtLocation.add(infectedAgent);
            infectedAgents.add(infectedAgent);
            infectedLocations.put(infectedLocation, infectionsAtLocation);
            this.scenario.getResults().getNumInfected().add(1);

        } else if (initialstateAgent.getState().getStateName().equalsIgnoreCase(infectious.getStateName())) {
            // we have a removal event.

            final Agent infectedAgent = (Agent) event.getInitialState();
            final String infectedLocation = infectedAgent.getLocation();
            final Collection<Agent> infectionsAtLocation = infectedLocations.get(infectedLocation);
            if (infectionsAtLocation != null) {
                // the infected agent may have moved so will be removed later from the other location.

                infectedAgents.remove(infectedAgent);
                removedAgents.add(infectedAgent);
                infectionsAtLocation.remove(infectedAgent);
                if (infectionsAtLocation.isEmpty()) {
                    infectedLocations.remove(infectedLocation);
                } else {
                    infectedLocations.put(infectedLocation, infectionsAtLocation);
                }

                this.scenario.getResults().getNumRemoved().add(1);
            }
        } else {
            throw new BroadwickException("Cannot recognise event " + event.toString());
        }

    }

    @Override
    public String toVerboseString() {
        return "";
    }

    @Override
    public void resetAmount() {
        // We could save the state of the simulation here. We won't do it in this simulation but in general one would
        // save the internal states of all internal variables e.g.
        //        numberOfSusceptiblesSaved = initial value of susceptibles
        //        numberOfInfectiousSaved = initial value of infectious
        //        numberOfRemovedSaved = initial value of removed
    }

    @Override
    public void save() {

        // We could save the state of the simulation here. We won't do it in this simulation but in general one would
        // save the internal states of all internal variables e.g.
        //        numberOfSusceptiblesSaved = numberOfSusceptibles;
        //        numberOfInfectiousSaved = numberOfInfectious;
        //        numberOfRemovedSaved = numberOfRemoved;
    }

    @Override
    public void rollback() {
        // Here we would recover the last saved values. We won't do it in this simulation but in general one would
        // use the savedinternal states of all internal variables e.g.
        //        numberOfSusceptibles = numberOfSusceptiblesSaved;
        //        numberOfInfectious = numberOfInfectiousSaved;
        //        numberOfRemoved = numberOfRemovedSaved;
    }

    // the amount manager holds the list of agents, farms with infections, etc
    @Getter
    private final List<Agent> infectedAgents;
    @Getter
    private final List<Agent> removedAgents;
    @Getter
    private final Map<String, Collection<Agent>> infectedLocations;

    @Getter
    private final SimulationState susceptible;
    @Getter
    private final SimulationState infectious;
    @Getter
    private final SimulationState removed;
    @Getter
    private final MyMonteCarloScenario scenario;

}
