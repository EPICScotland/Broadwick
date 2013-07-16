package broadwick.montecarlo.path;

/**
 * A Path consists of several steps generated at random.
 */
public interface PathGenerator {
    
    /**
     * Generate the next step in the path.
     * @param step the current step.
     * @return the proposed next step.
     */
     Step generateNextStep(final Step step);
     
     /**
      * Get the initial Step of the path.
      * @return the initial step.
      */
     Step getInitialStep();
}
