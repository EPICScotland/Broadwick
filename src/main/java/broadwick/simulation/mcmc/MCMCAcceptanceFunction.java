package broadwick.simulation.mcmc;

/**
 * Acceptance function for MCMC that determines whether or not a proposed state should be accepted or not.
 */
public interface MCMCAcceptanceFunction {

    /**
     * Determine whether or not to accept the proposed state in the Markov Chain.
     * @param state the current (MCMC) state of the system.
     * @param proposedState the proposed state in the chain.
     * @return true if the proposed step is to be accepted.
     */
    boolean isAccepted(final MCMCState state, final MCMCState proposedState);
}
