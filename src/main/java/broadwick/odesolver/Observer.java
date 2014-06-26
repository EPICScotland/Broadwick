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
package broadwick.odesolver;

import java.util.Collection;
import lombok.Getter;

/**
 *
 */
public abstract class Observer implements Comparable<Observer> {

    /**
     * Creates an observer dedicated to an ODE solver. The observer is NOT registered to the solver until you call
     * {@link OdeSolver#addObserver(Observer)}.
     * <p/>
     * @param solver the solver
     */
    public Observer(final OdeSolver solver) {
        this.solver = solver;
    }

    /**
     * Gets called when the ODE solver has started after the initialization and before the termination condition is
     * checked the first time.
     */
    public abstract void started();

    /**
     * Gets called after each termination check and before
     * {@link Simulator#performStep(fern.simulation.controller.SimulationController)} is called.
     */
    public abstract void step();

    /**
     * Gets called when the ODE solver has finished, directly after the termination check.
     */
    public abstract void finished();

    /**
     * Gets called by the ODE solver when a certain moment in time is reached. This time has to be registered with the
     * observer in the OdeSolver.
     * @param thetaTime the time the event has to be triggered.
     * @param events    a collection of events that occur at thetaTime.
     */
    public abstract void theta(final double thetaTime, final Collection<Object> events);

    @Override
    public final int compareTo(final Observer o) {
        // We really are only interested in determining if the Observers are equal so that a tree based table 
        // can distinguish between observers. Order does NOT matter so we return 1 if the objects are not equal.
        if (this == o) {
            return 0;
        }
        return 1;
    }

    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private final OdeSolver solver;

}
