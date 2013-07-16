package broadwick.montecarlo;

import broadwick.montecarlo.path.Step;

/**
 * A simple model class thats runs a model and creates named quantities, i.e. a map containing the vlaues and their
 * names.
 */
public interface McModel {

    /**
     * Compute the value of the Monte Carlo model/simulation at the given set of coordinates.
     * @param coordinates the coordinates of the current MC step.
     * @return a Monte Carlo Results object.
     */
    McResults compute(final Step coordinates);
}
