package broadwick.model;

/**
 * This interface declares a model for the Broadwick framework. To create a model 
 * for this framework (by model we mean both the model and the methodology for
 * running/solving it) the user creates an implementation of this class and refers
 * to it in the framework configuration file.
 * <code>
 * public class SIRModel implements Model {
 *
 *     public void run() {
 *         // perform model specific step here
 *     }
 *
 * @Parameter(hint="transmission term")
 * public double beta;
 * }
 * </code>
 */
public interface Model {

    /**
     * Run the model. This is the entry point to the model from the framework, up til now
     * the framework has read any configuration files and processed data mentioned therein.
     */
    void run();
}
