package broadwick.montecarlo.acceptor;

import broadwick.montecarlo.McResults;

/**
 * Acceptor for a Monte Carlo step.
 */
public interface Acceptor {

    /**
     * Accept a step in a Monte Carlo path/chain based on the ratio of the results at each step.
     * @param oldResult the results at the previous step.
     * @param newResult the result at the current (proposed) step.
     * @return true if the step that generated newResults is to be accepted, false otherwise.
     */
    boolean accept(McResults oldResult, McResults newResult);
}
