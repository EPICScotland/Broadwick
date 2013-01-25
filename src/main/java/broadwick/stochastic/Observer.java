package broadwick.stochastic;

import java.util.Collection;
import lombok.Getter;

/**
 * Implementing classes are used to observe certain aspects of simulations (stochastic processes). It is basically just
 * an interface of methods that get called during a simulation together with some common methods useful for many types
 * of observers.
 * <p/>
 * Some observers take repeats into account. If you repeat the simulations, they will yield average data.
 * <p/>
 * Each observer should override the
 * <code>toString</code> method which should return a string representation of its data.
 * <p/>
 * You can register a {@link PrintWriter} to the
 * <code>Observer</code>. If you have, the return value of
 * <code>toString</code> is printed if a simulation has finished.
 * <p/>
 * @author Florian Erhard (copied from FERN http://www.bio.ifi.lmu.de/FERN/)
 * <p/>
 */
public abstract class Observer implements Comparable {

    /**
     * Creates an observer dedicated to one process. The observer is NOT registered at the process, you have to call
     * {@link Simulator#addObserver(Observer)} in order to do this.
     * <p/>
     * @param sim the process
     */
    public Observer(final StochasticSimulator sim) {
        this.process = sim;
    }

    @Override
    public final int compareTo(final Object o) {
        // We really are only interested in determining if the Observers are equal so that a tree based table 
        // can distinguish between observers. Order does NOT matter so we return 1 if the objects are not equal.
        if (this == o) {
            return 0;
        }
        return 1;
    }

    /**
     * Gets called when the simulation has started after the initialization and before the termination condition is
     * checked the first time.
     */
    public abstract void started();

    /**
     * Gets called after each termination check and before
     * {@link Simulator#performStep(fern.simulation.controller.SimulationController)} is called.
     */
    public abstract void step();

    /**
     * Gets called when a simulation has finished, directly after the termination check.
     */
    public abstract void finished();

    /**
     * Gets called by simulators when a certain moment in time is reached. This moment in time has to be registered with
     * the observer in the StochasticSimulator.
     * @param thetaTime moment in time
     * @param events    a collection of events that occur at thetaTime.
     */
    public abstract void theta(final double thetaTime, final Collection<Object> events);

    /**
     * Gets called before an event is triggered.
     * @param event the event which is supposed to fire
     * @param tau   the time the event occurs (at this time {@link Simulator#getTime()}
     * @param times the number of firings
     */
    public abstract void performEvent(final SimulationEvent event, final double tau, final int times);

    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private StochasticSimulator process;
}
