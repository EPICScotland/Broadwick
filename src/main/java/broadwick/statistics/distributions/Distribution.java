package broadwick.statistics.distributions;


/**
 * Interface defining all distribution classes.
 */
public interface Distribution {

    /**
     * Obtain a sample from the distribution.
     * @return the sampled value formt he distribution.
     */
    double sample();
}
