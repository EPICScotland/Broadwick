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
package broadwick.example.stochasticsir;

import broadwick.stochastic.AmountManager;
import broadwick.stochastic.SimulationEvent;
import broadwick.stochastic.SimulationState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Create the amount manager that manages the populations of Susceptble/Infected/Removed.
 */
@Slf4j
class MyAmountManager implements AmountManager {

    /**
     * Create the amount manager. Normally we would define simulation states and event in separate classes but since, in
     * this case, they are quite simple we will define them here and allow accessors to get access to them.
     * @param s the number of susceptibles in the simulation.
     * @param i the number of infectious in the simulation.
     * @param r the number of removed in the simulation.
     * @param model the model is told to update the transition probabilities when the numbers of S/I/R are changed.
     */
    MyAmountManager(final int s, final int i, final int r, final StochasticSIR model) {
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
        numberOfSusceptibles = s;
        numberOfInfectious = i;
        numberOfRemoved = r;
        this.model = model;
    }

    @Override
    public void performEvent(SimulationEvent event, int times) {
        log.trace("{}", String.format("Performing event %s->%s %d times.", 
                                     event.getInitialState().getStateName(), event.getFinalState().getStateName(), times));
        
        if (event.getInitialState().equals(susceptible) && event.getFinalState().equals(infectious)) {
            // the event was a S->I so decrease numberOfSusceptibles and increase numberOfInfectious
            final int numToBeRemoved = numberOfSusceptibles - Math.max(0, numberOfSusceptibles - times);
            numberOfSusceptibles = numberOfSusceptibles - numToBeRemoved;
            numberOfInfectious = numberOfInfectious + numToBeRemoved;
        } else if (event.getInitialState().equals(infectious) && event.getFinalState().equals(removed)) {
            // the event was a I->R so decrease numberOfInfectious and increase numberOfRemoved
            final int numToBeRemoved = numberOfInfectious - Math.max(0, numberOfInfectious - times);
            numberOfInfectious = numberOfInfectious - numToBeRemoved;
            numberOfRemoved = numberOfRemoved + numToBeRemoved;
        } else {
            log.error("Event {} undefined.", event.toString());
        }
        
        // Now that the populations of S/I/R have changed we need to update our kernel
        model.updateKernel();
    }

    @Override
    public String toVerboseString() {
        return String.format("%d\t%d\t%d\n",
                             numberOfSusceptibles, numberOfInfectious, numberOfRemoved);
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
    

    @Getter
    @Setter
    private int numberOfSusceptibles;
    @Getter
    private int numberOfInfectious;
    @Getter
    private int numberOfRemoved;

    @Getter
    private final SimulationState susceptible;
    @Getter
    private final SimulationState infectious;
    @Getter
    private final SimulationState removed;
    final private StochasticSIR model;

}
