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
package broadwick.stochastic;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

/**
 * Base class for stochastic simulators. Extending classes just have to implement
 * <code>performStep</code> which gets invoked as long as the simulation is running. They also may override
 * <code>init</code> but should in that case call
 * <code>super.init()</code> to avoid unwanted effects. <p> Simulators also should handle the special time
 * <code>theta</code>: is is a moment in time when
 * <code>doThetaEvent</code> is supposed to be invoked (e.g. to measure species populations at this moment). Consider
 * this, if you want to implement a simulator.
 * <p/>
 * The <TT>doEvent</TT> are supposed to be invoked when a simulator causes a reaction to fire.
 * <p/>
 * If an extending class sticks to these conventions, it can take full benefit of the observers system: One or more
 * {@link Observer} can be registered at a simulator and observe certain aspects of the simulation (see the
 * {@link Observer}s javadoc for more information).
 * <p/>
 * Note that this class is NOT thread-safe. Consequently, if a simulation program uses multiple threads, it should
 * acquire a lock on a simulator (using a <TT>synchronized</TT> block) before accessing its state. Note however, that
 * one can launch many simulations in parallel with as many threads, as long as <SPAN CLASS="textit">each thread has its
 * own</SPAN> <TT>Simulator</TT>.
 * <p/>
 * This class is mostly copied from the FERN projects stochastic classes and modified to fit into this framework.
 * @see http://www.bio.ifi.lmu.de/FERN/
 */
@Slf4j
public abstract class StochasticSimulator {

    /**
     * Creates a new simulator using a given amount manager.
     * @param amountManager    the amount manager used in the simulator.
     * @param transitionKernel the transition kernel to be used with the stochastic solver.
     */
    public StochasticSimulator(final AmountManager amountManager, final TransitionKernel transitionKernel) {
        this(amountManager, transitionKernel, false);
    }

    /**
     * Creates a new simulator.
     * @param amountManager    the amount manager used in the simulator.
     * @param transitionKernel the transition kernel to be used with the stochastic solver.
     * @param reverseTime      true if we wish to go backwards in time.
     */
    public StochasticSimulator(final AmountManager amountManager,
                               final TransitionKernel transitionKernel, final boolean reverseTime) {
        this.amountManager = amountManager;
        this.transitionKernel = transitionKernel;
        this.reverseTime = reverseTime;
        this.thetaQueue = new ThetaQueue();
    }

    /**
     * Starts the simulation up to a given time. It just uses a {@link DefaultController} and calls
     * {@link StochasticSimulator#start(SimulationController)}.
     * @param time	simulation time
     */
    public final void run(final double time) {
        if (controller == null) {
            controller = new DefaultController(time);
        }
        run();
    }

    /**
     * Start the simulation with the given {@link SimulationController}.
     */
    public final void run() {

        try {
            // If we haven't set a controller for the process then set the default one with a max time of 5
            // (unknonw  units).
            if (controller == null) {
                controller = new DefaultController(5);
            }

            init();

            // tell our observers that the simulation has started
            for (Observer observer : observers) {
                observer.started();
            }

            final StopWatch sw = new StopWatch();
            sw.start();
            while (controller.goOn(this)) {

                // The performStep() method is implemented by the specifc stochastic algorithm (e.g. Gillespie's)
                performStep();
                
                //amountManager.doHouseKeeping();

                // tell our observers that the step has been completed.
                for (Observer observer : observers) {
                    observer.step();
                }
                sw.split();
                log.trace("Performed step in {}", sw.toSplitString());
            }

            // tell our observers that the simulation has finished.
            for (Observer observer : observers) {
                observer.finished();
            }
        } catch (Exception e) {
            log.error("Error running stochastic simulation. {}", e.getLocalizedMessage());
            throw (e);
        }
    }

    /**
     * Initializes the algorithm. <ul><li>set currentTime=0</li><li>reset the {@link AmountManager}</li><li>recalculate
     * the propensities</li></ul> Gets called at the very beginning of
     * <code>start</code>
     */
    public final void init() {
        currentTime = startTime;
    }

    /**
     * Fires a reaction. It calls the observers and the {@link AmountManager}.
     * @param mu reaction to be fired.
     * @param t	 time at which the firing occurs.
     */
    protected final void doEvent(final SimulationEvent mu, final double t) {
        doEvent(mu, t, 1);
    }

    /**
     * Perform an event by informing the subscribed observers and the amountManager.
     * @param event event to be performed.
     * @param t	    time at which the firing occurs.
     * @param n     the number of times mu is to be fired.
     */
    protected final void doEvent(final SimulationEvent event, final double t, final int n) {
        log.trace("Performing event {}", event);
        for (Observer observer : observers) {
            observer.observeEvent(event, t, n);
        }
        // change the amount of the reactants
        if (!Double.isInfinite(t)) {
            amountManager.performEvent(event, n);
        }
    }

    /**
     * Gets called when the simulator reaches the predetermined time of a theta event. All the observers for the events
     * that are configured for this time are notified and given a list of events that are triggered.
     */
    protected final void doThetaEvent() {
        final double nextThetaEventTime = thetaQueue.getNextThetaEventTime();
        log.trace("Performing theta events at t = {}", nextThetaEventTime);

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
     * Add an observer to the engines list of observers.
     * @param observer the observer.
     */
    public final void addObserver(final Observer observer) {
        if (observer.getProcess() != this) {
            throw new IllegalArgumentException("Observer doesn't belong to this simulator!");
        }
        this.getObservers().add(observer);
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
     * Register a new theta event that is triggered at a given time.
     * @param obs       the observers which is registering.
     * @param thetaTime the time the theta event occurs.
     * @param event     the theta event.
     */
    public final void registerNewTheta(final Observer obs, final double thetaTime, final Object event) {
        thetaQueue.pushTheta(thetaTime, obs, event);
    }

    /**
     * Get the default observer for the stochastic process. The default observer is first one defined.
     * @return the default observer.
     */
    public final Observer getDefaultObserver() {
        return observers.toArray(new Observer[observers.size()])[0];
    }

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
            if (reverseTime) {
                nextThetaEventTime = Double.NEGATIVE_INFINITY;
            } else {
                nextThetaEventTime = Double.POSITIVE_INFINITY;
            }
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

            if (reverseTime) {
                nextThetaEventTime = Math.max(nextThetaEventTime, time);
            } else {
                nextThetaEventTime = Math.min(nextThetaEventTime, time);
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
            if (reverseTime) {
                if (thetas.rowKeySet().isEmpty()) {
                    nextThetaEventTime = Double.NEGATIVE_INFINITY;
                } else {
                    nextThetaEventTime = Collections.max(thetas.rowKeySet());
                }
            } else {
                if (thetas.rowKeySet().isEmpty()) {
                    nextThetaEventTime = Double.POSITIVE_INFINITY;
                } else {
                    nextThetaEventTime = Collections.min(thetas.rowKeySet());
                }
            }

            return eventData;
        }
        @Getter
        private Table<Double, Observer, Collection<Object>> thetas;
        @Getter
        private double nextThetaEventTime;
    }

    /**
     * Reset propensities when a event has been executed.
     */
    public abstract void reinitialize();

    /**
     * Performs one simulation step. Between steps the terminating condition is checked. The simulators controller is
     * passed, if an extending class wants to check it within one step. Implementing algorithms cannot be sure that the
     * propensities are correct at the beginning of a step (e.g. if the volume changed). They should override
     * {@link Simulator#setAmount(int, long)} and {@link Simulator#setVolume(double)} if they need correct values!
     */
    public abstract void performStep();

    /**
     * Gets the name of the algorithm.
     * @return name of the algorithm
     */
    public abstract String getName();

    /**
     * Seed the RNG if required.
     * @param seed the RNG seed.
     */
    public abstract void setRngSeed(final int seed);
    @Setter
    private int startTime = 0;
    @Setter
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private TransitionKernel transitionKernel;
    @Getter
    private AmountManager amountManager;
    @Setter
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private double currentTime = 0;
    @Getter
    @Setter
    private SimulationController controller = null;
    private ThetaQueue thetaQueue = new ThetaQueue();
    @Getter
    private Set<Observer> observers = new HashSet<>(1);
    protected boolean reverseTime = false;
}
