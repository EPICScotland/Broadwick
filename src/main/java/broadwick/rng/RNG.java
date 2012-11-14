package broadwick.rng;

import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.apache.commons.math.random.MersenneTwister;
import org.apache.commons.math.random.RandomDataImpl;
import org.apache.commons.math.random.Well19937c;
import org.apache.commons.math.random.Well44497b;

/**
 * Random number generator singleton class This class is not thread safe.
 */
@EqualsAndHashCode
@ToString
public class RNG {

    /**
     * Create a random number generator using the default JDK-provied PRNG.
     */
    public RNG() {
        // By default, the implementation provided in RandomDataImpl uses
        // the JDK-provided PRNG. Like most other PRNGs, the JDK generator
        // generates sequences of random numbers based on an initial
        // "seed value".
        generator = new RandomDataImpl();
    }

    /**
     * Create a random number generator from a given generator using the current
     * time as a seed.
     * @param gen the random number generator to use.
     */
    public RNG(final Generator gen) {
        if (gen.equals(Generator.MERSENNE)) {
            generator = new RandomDataImpl(new MersenneTwister(System.currentTimeMillis() * Thread.currentThread().getId()));
            name = "Mersenne Twister";
        } else if (gen.equals(Generator.Well19937c)) {
            generator = new RandomDataImpl(new Well19937c(System.currentTimeMillis() * Thread.currentThread().getId()));
            name = "Well19937c";
        } else if (gen.equals(Generator.Well44497b)) {
            generator = new RandomDataImpl(new Well44497b(System.currentTimeMillis() * Thread.currentThread().getId()));
            name = "Well44497b";
        } else {
            // By default, the implementation provided in RandomDataImpl uses
            // the JDK-provided PRNG. Like most other PRNGs, the JDK generator
            // generates sequences of random numbers based on an initial
            // "seed value".
            generator = new RandomDataImpl();
        }
    }

    /**
     * Get a list of valid geometries for the lattice.
     * @return Vector<String> a list of valid lattice geometries (in uppercase)
     */
    public static List<String> validGenerators() {
        final ArrayList<String> generators = new ArrayList<>(3);
        for (Generator value : Generator.values()) {
            generators.add(value.name());
        }
        return generators;
    }

    /**
     * Seed the random number generator.
     * @param seed the seed to use in the Rng
     */
    public final void seed(final int seed) {
        generator.reSeed(seed);
    }

    /**
     * Generates a uniformly distributed random value from the open interval (
     * <code>0.0</code>,
     * <code>1.0</code>) (i.e., endpoints excluded). <p>
     * <strong>Definition</strong>: <a
     * href="http://www.itl.nist.gov/div898/handbook/eda/section3/eda3662.htm">
     * Uniform Distribution</a>
     * <code>0.0</code> and
     * <code>1.0 - 0.0</code> are the <a href =
     * "http://www.itl.nist.gov/div898/handbook/eda/section3/eda364.htm">
     * location and scale parameters</a>, respectively.</p>
     * @return uniformly distributed random value between lower and upper
     * (exclusive)
     */
    public final double getDouble() {
        return generator.nextUniform(0.0, 1.0);
    }

    /**
     * Generates a uniformly distributed random value from the open interval (
     * <code>lower</code>,
     * <code>upper</code>) (i.e., endpoints excluded). <p>
     * <strong>Definition</strong>: <a
     * href="http://www.itl.nist.gov/div898/handbook/eda/section3/eda3662.htm">
     * Uniform Distribution</a>
     * <code>lower</code> and
     * <code>upper - lower</code> are the <a href =
     * "http://www.itl.nist.gov/div898/handbook/eda/section3/eda364.htm">
     * location and scale parameters</a>, respectively.</p> <p>
     * <strong>Preconditions</strong>:<ul> <li><code>lower < upper</code>
     * (otherwise an IllegalArgumentException is thrown.)</li> </ul></p>
     * @param lower lower endpoint of the interval of support
     * @param upper upper endpoint of the interval of support
     * @return uniformly distributed random value between lower and upper
     * (exclusive)
     */
    public final double getDouble(final double lower, final double upper) {
        double lo = lower;
        double hi = upper;
        if (lower >= upper) {
            hi = lower;
            lo = upper;
        }
        if (lower == upper) {
            return lower;
        }
        return generator.nextUniform(lo, hi);
    }

    /**
     * Generates a uniformly distributed random integer between
     * <code>lower</code> and
     * <code>upper</code> (endpoints included). <p> The generated integer will
     * be random, but not cryptographically secure. To generate
     * cryptographically secure integer sequences, use
     * <code>nextSecureInt</code>.</p> <p> <strong>Preconditions</strong>:<ul>
     * <li><code>lower < upper</code> (otherwise an IllegalArgumentException is
     * thrown.)</li> </ul></p>
     * @param lower lower bound for generated integer
     * @param upper upper bound for generated integer
     * @return a random integer greater than or equal to <code>lower</code> and
     * less than or equal to <code>upper</code>. If lower == upper then lower is
     * returned.
     */
    public final int getInteger(final int lower, final int upper) {
        int lo = lower;
        int hi = upper;
        if (lower >= upper) {
            hi = lower;
            lo = upper;
        }
        if (lower == upper) {
            return lower;
        }
        return generator.nextInt(lo, hi);
    }

    /**
     * Generates a random value from the Poisson distribution with the given
     * mean.
     * <p> <strong>Definition</strong>: <a
     * href="http://www.itl.nist.gov/div898/handbook/eda/section3/eda366j.htm">
     * Poisson Distribution</a>
     * </p>
     * <p> <strong>Preconditions</strong>: <ul>
     * <li>The specified mean <i>must</i> be positive (otherwise an
     * IllegalArgumentException is thrown.)</li> </ul></p>
     * @param mean Mean of the distribution
     * @return poisson deviate with the specified mean
     */
    public final long getPoisson(final double mean) {
        return generator.nextPoisson(mean);
    }

    /**
     * Generates a random value from the Normal (or Gaussian) distribution with
     * the given mean and standard deviation. <p> <strong>Definition</strong>:
     * <a
     * href="http://www.itl.nist.gov/div898/handbook/eda/section3/eda3661.htm">
     * Normal Distribution</a></p> <p> <strong>Preconditions</strong>: <ul>
     * <li><code>sigma > 0</code> (otherwise an IllegalArgumentException is
     * thrown.)</li> </ul></p>
     * @param mu Mean of the distribution
     * @param sigma Standard deviation given as a percentage of the mean.
     * @return random value from Gaussian distribution with mean = mu, standard
     * deviation = sigma
     */
    public final double getGaussian(final double mu, final double sigma) {
        return generator.nextGaussian(mu, sigma);
    }

    /**
     * Get a boolean (true/false) value.
     * @return boolean random true/false value
     */
    public final boolean getBoolean() {
        return getDouble(0.0, 1.0) < 0.5 ? true : false;
    }

    /**
     * Generates a random value from the binomial distribution with the given N
     * and p. The returned value is a random integer drawn from <DIV
     * ALIGN="CENTER" CLASS="mathdisplay"> <I>P</I>(<I>x</I>) =
     * (<SUP>n</SUP><SUB>x</SUB>)
     * <I>p</I><SUP>x</SUP>(1-<I>p</I>)<SUP>(<I>n-x</I>)</SUP> </DIV><P></P>
     * Successive draws from this distribution can be combined, i.e. if X ~
     * getBinomial(n, p) and Y ~ getBinomial(m, p) are independent binomial
     * variables , then X + Y is again a binomial variable; its distribution is
     * getBinomial(n+m, p)
     * @param n the number of trials.
     * @param p the probability of success of each trial.
     * @return the number of successes on n trials.
     */
    public final int getBinomial(final int n, final double p) {
        int x = 0;
        for (int i = 0; i < n; i++) {
            if (generator.nextUniform(0.0, 1.0) < p) {
                x++;
            }
        }
        return x;
    }

    /**
     * Randomly pick an object from an array of objects.
     * @param objects an array of objects, one of whom is to be picked.
     * @return a random element from objects.
     */
    public final Object selectOneOf(final Object[] objects) {
        return objects[getInteger(0, objects.length - 1)];
    }
    /**
     * Random number generator type.
     */
    private RandomDataImpl generator;
    @SuppressWarnings("PMD.UnusedPrivateField")
    @Getter
    private String name;

    /**
     * Random number generator type.
     */
    public static enum Generator {

        /*
         * This generator features an extremely long period (219937-1) and 623-dimensional equidistribution up to 32 bits accuracy.
         * The home page for this generator is located at http://www.math.sci.hiroshima-u.ac.jp/~m-mat/MT/emt.html.
         * This generator is described in a paper by Makoto Matsumoto and Takuji Nishimura in 1998:
         * "Mersenne Twister: A 623-Dimensionally Equidistributed Uniform Pseudo-Random Number Generator",
         * ACM Transactions on Modeling and Computer Simulation, Vol. 8, No. 1, January 1998, pp 3--30
         */
        MERSENNE,
        /*
         * WELL19937c pseudo-random number generator from François Panneton, Pierre L'Ecuyer and Makoto Matsumoto.
         * This generator is described in a paper by François Panneton, Pierre L'Ecuyer and Makoto Matsumoto
         * "Improved Long-Period Generators Based on Linear Recurrences Modulo 2" ACM Transactions on Mathematical Software, 32, 1 (2006)
         */
        Well19937c,
        /*
         * WELL44497b pseudo-random number generator from François Panneton, Pierre L'Ecuyer and Makoto Matsumoto.
         * This generator is described in a paper by François Panneton, Pierre L'Ecuyer and Makoto Matsumoto
         * "Improved Long-Period Generators Based on Linear Recurrences Modulo 2" ACM Transactions on Mathematical Software, 32, 1 (2006)
         */
        Well44497b
    };
}
