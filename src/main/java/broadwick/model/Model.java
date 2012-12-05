package broadwick.model;

import broadwick.data.Lookup;
import lombok.Getter;

/**
 * This interface declares a model for the Broadwick framework. To create a model for this framework (by model we mean
 * both the model and the methodology for running/solving it) the user creates an implementation of this class and
 * refers to it in the framework configuration file.
 * <code>
 * public class SIRModel implements Model {
 *     public void run() {
 *         // perform model specific step here
 *     }
 *
 * @Parameter(hint="transmission term")
 * public double beta;
 * }
 * </code>
 */
public abstract class Model {

    /**
     * Set the xml string for the <model> element in the configuration file.
     * @param model a string representation of the xml section defining the configuration of the model.
     */
    public final void setModelConfiguration(final String model) {
        this.model = model;
    }

    /**
     * Set the lookup object that allows access to the data files specified in the configuration file.
     * @param lookup the XML element corresponding to the Model element in the config.
     */
    public final void setModelDataLookup(final Lookup lookup) {
        this.lookup = lookup;
    }

    /**
     * Initialise the model. This method is called by the framework before calling the models run method to allow
     * the implementation of the model to perform any initialisation required.
     */
    public abstract void init();

    /**
     * Run the model. This is the entry point to the model from the framework, up til now the framework has read any
     * configuration files and processed data mentioned therein.
     */
    public abstract void run();

    @Getter
    private String model;
    @SuppressWarnings("PMD.UnusedPrivateField")
    @Getter
    private Lookup lookup;
}
