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

import broadwick.stochastic.Observer;
import broadwick.stochastic.SimulationEvent;
import broadwick.stochastic.StochasticSimulator;
import com.google.common.base.Throwables;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * A simple observer class that records data for each scenario.
 */
@Slf4j
class MyMonteCarloScenarioObserver extends Observer {

    /**
     * Create an observer for the simulation that will just save the data at each step.
     * @param simulator      the simulator that will be observed.
     * @param outputFileName the name of the file to which the data will be saved.
     */
    MyMonteCarloScenarioObserver(final StochasticSimulator simulator, final MyMonteCarloScenario scenario) {
        super(simulator);
        this.scenario = scenario;
    }

    @Override
    public void started() {
        //log.info("Started Observing using MyObserver.");
    }

    @Override
    public void step() {
        final MyAmountManager amountManager = (MyAmountManager) this.getProcess().getAmountManager();

        // Update some measureables in the results
        this.scenario.getResults().getInfectedLocationsTimeSeries().append(amountManager.getInfectedLocations().size()).append(",");
        this.scenario.getResults().getInfectedIndividualsTimeSeries().append(amountManager.getInfectedAgents().size()).append(",");
        this.scenario.getResults().getRemovedIndividualsTimeSeries().append(amountManager.getRemovedAgents().size()).append(",");
        this.scenario.getResults().getOutbreakSize().add(amountManager.getInfectedAgents().size());

        // Now we have to deal with the movements, we have a Map in the scenarion of the agents and where we need to move
        // them.
        for (final Map.Entry<Agent, String> movement : this.scenario.getCurrentMovements().entrySet()) {
            final Agent agent = movement.getKey();
            final String source = agent.getLocation();
            final String destination = movement.getValue();

            // it may be the case that an individual that is marked for moving has been removed by the
            // amountManager.performEvent();
            try {
                final Collection<Agent> infectionsAtSource = amountManager.getInfectedLocations().get(source);
                // check that the individual hasn't been removed while moving.
                if (infectionsAtSource != null) {
                    infectionsAtSource.remove(agent);
                    if (infectionsAtSource.isEmpty()) {
                        amountManager.getInfectedLocations().remove(source);
                    }

                    Collection<Agent> infectionsAtDestination = amountManager.getInfectedLocations().get(destination);
                    if (infectionsAtDestination == null) {
                        infectionsAtDestination = new ArrayList<>();
                    }
                    infectionsAtDestination.add(agent);
                    amountManager.getInfectedLocations().put(destination, infectionsAtDestination);
                    agent.setLocation(destination);
                    infectionsAtSource.clear();
                }
            } catch (Exception e) {
                log.debug("agent = {}", agent.toString());
                log.debug("source = {}", source);
                log.debug("destination = {}", destination);
                log.debug("infecteds at source = {}", amountManager.getInfectedLocations().get(source));
                log.debug("infecteds at destination = {}", amountManager.getInfectedLocations().get(destination));
                log.debug("{}", Throwables.getStackTraceAsString(e));
            }
        }

        scenario.updateKernel();
    }

    @Override
    public void finished() {
        //log.info("Finished Observing using MyObserver.");
    }

    @Override
    public void theta(final double thetaTime, final Collection<Object> events) {

    }

    @Override
    public void observeEvent(final SimulationEvent event, final double tau, final int times) {

    }

    private final MyMonteCarloScenario scenario;

}
