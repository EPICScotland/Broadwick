package broadwick.markovchain;

import broadwick.markovchain.proposals.NormalProposal;
import lombok.Getter;
import lombok.Setter;

/**
 * An implementation of a Markov Chain. Markov Chains have a current step and generates a new step based solely on the
 * current one.
 */
public class MarkovChain {

    /**
     * Create a new Markov chain with an initial step.
     * @param initialStep the initial step.
     */
    public MarkovChain(final Step initialStep) {
        this.currentStep = initialStep;
        this.generator = new NormalProposal();

    }

    /**
     * Create a new Markov Chain with an initial step and proposal function.
     * @param initialStep the initial step.
     * @param generator   the generator object for proposing new steps.
     */
    public MarkovChain(final Step initialStep, final MarkovProposalFunction generator) {
        this.currentStep = initialStep;
        this.generator = generator;

    }

    /**
     * Generate the next state from the current state.
     * @return the proposed next step.
     */
    public final Step generateNextStep() {
        return generator.generate(currentStep);
    }
    @Getter
    @Setter
    private Step currentStep;
    private MarkovProposalFunction generator;
}
