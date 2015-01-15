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

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * A base class for all ODE solvers.
 */
@Slf4j
public abstract class OdeSolver {

    /**
     * Create the solver object with initial consitions.
     * @param ode the ode object containing the specification of the ode.
     * @param tStart the start time (the independent variable is assumed to be time)
     * @param tEnd the end time (the independent variable is assumed to be time)
     * @param stepSize the size of the step to be used in the solver.
     */
    public OdeSolver(final Ode ode, final double tStart, final double tEnd, final double stepSize) {
        this.ode = ode;
        this.independentVariableStart = tStart;
        this.independentVariableEnd = tEnd;
        this.stepSize = stepSize;

        dependentVariables = new ArrayList<>(ode.getInitialValues());
        log.debug("Created ODE solver");
    }

    /**
     * Add an observer to the engines list of observers.
     * @param observer the observer.
     */
    public final void addObserver(final Observer observer) {
        if (observer.getSolver() != this) {
            log.error("Cannot add Observer to OdeSolver, type = {}", observer.getSolver());
            throw new IllegalArgumentException("Observer doesn't belong to this solver!");
        }
        log.trace("Adding Observer to OdeSolver");
        this.getObservers().add(observer);
    }

    /**
     * Register a new theta event that is triggered at a given time.
     * @param obs       the observers which is registering.
     * @param thetaTime the time the theta event occurs.
     * @param event     the theta event.
     */
    public final void registerNewTheta(final Observer obs, final double thetaTime, final Object event) {
        thetaQueue.pushTheta(thetaTime, obs, event);
    }

        /**
     * Gets called when the simulator reaches the predetermined time of a theta event. All the observers for the events
     * that are configured for this time are notified and given a list of events that are triggered.
     */
    protected final void doThetaEvent() {
        final double nextThetaEventTime = thetaQueue.getNextThetaEventTime();

        final Map<Observer, Collection<Object>> nextEvents = thetaQueue.getNextEventDataAndRemove();
        for (Map.Entry<Observer, Collection<Object>> entry : nextEvents.entrySet()) {

            final Observer observer = entry.getKey();
            final Collection<Object> events = entry.getValue();

            if (events != null) {
                log.trace("Informing observer of {} theta events", events.size());
                observer.theta(nextThetaEventTime, events);
            }
        }
        log.trace("Finished theta events next =  {}", thetaQueue.getNextThetaEventTime());
    }
    
    /**
     * Theta defines a moment, where the simulator has to invoke <TT>theta</TT> of a observers. It is used e.g. to
     * determine the amounts of species at one moments. Extending class just have to call
     * {@link Simulator#doThetaEvent()} which basically calls the observers.
     * @return the theta
     */
    public final double getNextThetaEventTime() {
        return thetaQueue.getNextThetaEventTime();
    }

    /**
     * Get a csv representation of the current state of the dependent variables. 
     * @return a csv string.
     */
    public final String getDependetVariablesAsCsv() {
        return StringUtils.join(dependentVariables, ",");
    }

    /**
     * Get the dependent variables.
     * @return an array of the dependent variables.
     */
    public final List<Double> getDependentVariables() {
        return dependentVariables;
    }

    /**
     * Solve the system of ODEs.
     */
    public abstract void run();

    /**
     * Manages the registered theta events. Each registered theta event is stored in a table containing the time of the
     * event the list of observers for that event and the list of events for that time.
     */
    private class ThetaQueue {

        /**
         * Construct an empty theta queue.
         */
        public ThetaQueue() {
            thetas = TreeBasedTable.create();
        }

        /**
         * Add a new theta event to the registry, including the time, collection of observers and collection of events.
         * Each event is stored as an object where it is assumed that the observer
         * @param time  the time the theta event occurs.
         * @param obs   the observers.
         * @param theta the theta event.
         */
        @Synchronized
        public void pushTheta(final double time, final Observer obs, final Object theta) {
            log.trace("Adding new {} theta event at t={}", theta.getClass(), time);

            Collection<Object> events = thetas.get(time, obs);
            if (events == null) {
                try {
                    log.trace("No theta events registered at t={}; Creating new list", time);
                    events = new HashSet<>();
                    events.add(theta);
                    thetas.put(time, obs, events);
                } catch (UnsupportedOperationException | ClassCastException |
                         IllegalArgumentException | IllegalStateException e) {
                    log.error("Could not register theta. {}", e.getLocalizedMessage());
                }
            } else {
                log.trace("Found {} theta events for t={}; Adding new event", events.size(), time);
                events.add(theta);
            }
            
            // Now we need to update the nextThetaEventTime
            if (thetas.rowKeySet().isEmpty()) {
                nextThetaEventTime = Double.POSITIVE_INFINITY;
            } else {
                nextThetaEventTime = Collections.min(thetas.rowKeySet());
            }
        }

        /**
         * Get the next observer and the collection of events they are subscribed to and remove it from the theta queue.
         * @return a map of the observers and their subscribed data.
         */
        public Map<Observer, Collection<Object>> getNextEventDataAndRemove() {
            final Map<Observer, Collection<Object>> nextEventData = thetas.row(nextThetaEventTime);

            // we have a view of the underlying data that we want to return, copy it first then delete the
            // underlying data.
            final Map<Observer, Collection<Object>> eventData = new HashMap<>(nextEventData);
            log.trace("Found {} configured events and observers at t={}", eventData.size(), nextThetaEventTime);

            for (Observer obs : eventData.keySet()) {
                final Collection<Object> removed = thetas.remove(nextThetaEventTime, obs);
                log.trace("Removed {} items from registered theta list", removed.size());
            }

            // Now we need to update the nextThetaEventTime
            if (thetas.rowKeySet().isEmpty()) {
                nextThetaEventTime = Double.POSITIVE_INFINITY;
            } else {
                nextThetaEventTime = Collections.min(thetas.rowKeySet());
            }

            return eventData;
        }
        @Getter
        private final Table<Double, Observer, Collection<Object>> thetas;
        @Getter
        private double nextThetaEventTime;
    }

    protected final List<Double> dependentVariables;
    protected final double stepSize;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private final Double independentVariableStart;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private final Double independentVariableEnd;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private final Ode ode;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    protected double currentTime = 0;
    @Getter
    @Setter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private OdeController controller = new DefaultOdeController(100.0);
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private final Set<Observer> observers = new HashSet<>(1);
    private final ThetaQueue thetaQueue = new ThetaQueue();
}
