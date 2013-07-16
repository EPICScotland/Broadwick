package broadwick.montecarlo.path;

import lombok.Getter;
import lombok.Setter;

/**
 * An implementation of a Markov Chain. Markov Chains have a current step and generates a new step based solely on the
 * current one.
 */
public class MarkovChain implements PathGenerator {

    /**
     * Create a new Markov chain with an initial step.
     * @param initialStep the initial step.
     */
    public MarkovChain(final Step initialStep) {
        this.currentStep = initialStep;
        this.initialStep = initialStep;
        this.generator = new MarkovNormalProposal();
        this.chainLength = 1;
    }

    /**
     * Create a new Markov Chain with an initial step and proposal function.
     * @param initialStep the initial step.
     * @param generator   the generator object for proposing new steps.
     */
    public MarkovChain(final Step initialStep, final MarkovProposalFunction generator) {
        this.currentStep = initialStep;
        this.generator = generator;
        this.chainLength = 1;
    }

    @Override
    public final Step generateNextStep(final Step step) {
        chainLength++;
        return generator.generate(currentStep);
    }

    @Override
    public final Step getInitialStep() {
        return initialStep;
    }
    @Getter
    @Setter
    private Step currentStep;
    private Step initialStep;
    private MarkovProposalFunction generator;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private int chainLength;
}
