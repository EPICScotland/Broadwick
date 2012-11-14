package broadwick.simulation.mcmc;

import lombok.Getter;

/**
 * Markov Chain Monte Carlo implementation.
 */
public class MarkovChainMonteCarlo {

    // TODO need to implement some measurements and their statistics here, incorporating burn-in periods, thinning interval etc.
    /**
     * Create the Markov Chain Monte Carlo object.
     *
     * @param proposer The function that proposes the next step in the Markov Chain.
     * @param accepter The function that decides whether or not the proposed step is accepted.
     */
    public MarkovChainMonteCarlo(final MCMCProposalFunction proposer, final MCMCAcceptanceFunction accepter) {
        this.proposalFunction = proposer;
        this.acceptanceFunction = accepter;
        numStepsRun = 0;
        numStepsAccepted = 0;
    }

    /**
     * Run the Markov Chain for as long as is required.
     */
    public final void run() {

        //todo the while true should be while (chain.goOn()) or something similar.
        while (true) {
            final MCMCState proposedState = proposalFunction.propose(mcmcState);
            //doStep(proposedState);
            if (acceptanceFunction.isAccepted(mcmcState, proposedState)) {
                // todo tell listeners.
                mcmcState = proposedState;
                numStepsAccepted++;
            }
            numStepsRun++;
        }

        //todo get results
    }

    /**
     * Get the percentage of proposed trials have been accepted.
     *
     * @return the percentage of trials accepted.
     */
    public final double getPercentageofAcceptedSteps() {
        return 100.0 * numStepsAccepted / numStepsRun;
    }
    private int numStepsAccepted;
    private int numStepsRun;
    @SuppressWarnings("PMD.UnusedPrivateField")
    @Getter
    private MCMCState mcmcState;
    private MCMCProposalFunction proposalFunction;
    private MCMCAcceptanceFunction acceptanceFunction;
}
