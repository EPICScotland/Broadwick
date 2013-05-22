package broadwick.markovchain;

/**
 * A Markov proposal distribution generates a new step based only on information from the current step.
 */
public interface MarkovProposalFunction {

    /** 
     * Generate a new step in the Markov Chain based only on the supplied step.
     * @param step the current step in the Markov Chain
     * @return the proposed new step.
     */
    Step generate(final Step step);
}
