package broadwick.montecarlo.path;

import broadwick.rng.RNG;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A proposal class that samples each new parameter in the steps coordinate from a normal distribution.
 */
public class MarkovNormalProposal implements MarkovProposalFunction {

    @Override
    public final Step generate(final Step step) {

        final Map<String, Double> proposedStep = new LinkedHashMap<>(step.getCoordinates().size());
        for (Map.Entry<String, Double> entry : step.getCoordinates().entrySet()) {
            proposedStep.put(entry.getKey(), GENERATOR.getGaussian(entry.getValue(), 1.0));
        }
        
        return new Step(proposedStep);
    }
    
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
}
