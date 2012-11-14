package broadwick.simulation.mcmc;

/**
 * Proposal function for MCMC sequences to obtain the next MCMCState in the chain.
 */
public interface MCMCProposalFunction {

    /**
     * Propose a new MCMCState from a probability distribution. This method is responsible for sampling from the
     * required probability distribution of states to obtain the next state in the chain of states that make up the
     * Markov Chain.
     * @param state the current (MCMC) state of the system.
     * @return the new proposed state.
     */
    MCMCState propose(MCMCState state);
}
