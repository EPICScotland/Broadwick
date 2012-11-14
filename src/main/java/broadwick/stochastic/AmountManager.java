package broadwick.stochastic;

/**
 * An amount manager is responsible for keeping track of the populations within the simulation. Each
 * <code>StochasticProcess</code> calls the
 * <code>performEvent</code> method when it fires an event. The amount manager then
 */
public interface AmountManager {

    /**
     * Reflects a (multiple) firing of a reaction by adjusting the populations of the states. If a population becomes
     * negative, a <code> RuntimeException</code> is thrown.
     * @param reaction	the index of the reaction fired
     * @param times    the number of firings
     */
    void performEvent(SimulationEvent reaction, int times);

    /**
     * Get a detailed description of the states and their sizes (potentially for debugging).
     * @return a detailed description of the states in the manager.
     */
    String toVerboseString();

    /**
     * Resets the amount of each species to the initial amount retrieved by the networks {@link AnnotationManager}. This
     * is called whenever a {@link Simulator} is started.
     */
    void resetAmount();

    /**
     * Makes a copy of the amount array.
     */
    void save();

    /**
     * Restore the amount array from the recently saved one.
     */
    void rollback();
}
