package broadwick.montecarlo.acceptor;

import broadwick.montecarlo.McResults;
import broadwick.rng.RNG;

/**
 *
 * @author anthony
 */
public class MetropolisHastings implements Acceptor {
    
    /**
     * Create a MetropolisHastings acceptor with a given random seed.
     * @param seed the random seed to use in the acceptor.
     */
    public MetropolisHastings(final int seed) {
        GENERATOR.seed(seed);
    }

    @Override
    public final boolean accept(final McResults oldResult, final McResults newResult) {

        final double ratio = newResult.getScore() / oldResult.getScore();
        return GENERATOR.getDouble() < Math.min(ratio, 1);
    }
    
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
}
