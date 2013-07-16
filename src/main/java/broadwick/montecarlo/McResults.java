package broadwick.montecarlo;

/**
 * This interface encapsulates the results from a Monte Carlo run. All McModels are expected to create a class implementing
 * this interface.
 */
public interface McResults {
    
    /**
     * Get the score for the Monte Carlo simulation. The Acceptor interface and it's implementing classes require 
     * a known value in this class for their analysis, e.g. this method may return the likelihood of the simulation and
     * the Acceptor class (e.g. MetropolisHastingsAcceptor) will use the McResults.getScore() method to determine whether
     * or not to accept this proposed step.
     * @return the value of the score.
     */
    double getScore();
    
    /**
     * Get a CSV string of the Monte Carlo results stored in this class. This will be used for logging and for output to
     * file.
     * @return a CSV representation of the results. 
     */
    String toCsv();
}
