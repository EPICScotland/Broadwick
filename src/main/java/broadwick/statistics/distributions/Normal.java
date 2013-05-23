package broadwick.statistics.distributions;

import broadwick.rng.RNG;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Sample fro a Normal (Gaussian) Distribution. This is a simple wrapper class for the RNG.getGaussian
 * method using a Well19937c random number generator.
 * <p>
 * References:</p><p>
 * <ul>
 * <li><a href="http://mathworld.wolfram.com/NormalDistribution.html"> Normal Distribution</a></li>
 * </ul>
 * </p>
 */
@Slf4j
public class Normal implements Distribution {

    /**
     * Create a normal distribution of zero mean and unit standard deviation.
     */
    public Normal() {
    }

    /**
     * Create a normal distribution with a given mean and standard deviation.
     * @param mean   the mean vlaue of the distribution.
     * @param stdDev the stand deviation of the distribution.
     */
    public Normal(final double mean, final double stdDev) {
        this.mean = mean;
        this.stdDev = stdDev;
    }

    @Override
    public final double sample() {
        return GENERATOR.getGaussian(mean, stdDev);
    }
    @Getter
    @Setter
    private double mean = 0.0;
    @Getter
    @Setter
    private double stdDev = 1.0;
    private static final RNG GENERATOR = new RNG(RNG.Generator.Well19937c);
}
