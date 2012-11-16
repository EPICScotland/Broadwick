package broadwick.model;

import broadwick.config.generated.Models;
import broadwick.config.generated.Parameters;
import broadwick.config.generated.Priors;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;

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
public abstract class Model {

    /**
     * Set the Model element from the configuration file.
     * @param model the xml element corresponding to the Model element in the config.
     */
    public final void setModelConfiguration(final Models.Model model) {
        this.model = model;
        setParameters();
        setPriors();
    }

    /**
     * Create a list of the model's parameters from the model xml definition.
     */
    private void setParameters() {
        final Parameters params = this.getModel().getParameters();
        if (params == null) {
            parameters = Collections.EMPTY_MAP;
        } else {
            parameters = new TreeMap<>();
            for (Parameters.Parameter par : params.getParameter()) {
                parameters.put(par.getName(), par);
            }
        }
    }

    /**
     * Create a list of the model's priors from the model xml definition.
     */
    private void setPriors() {
        final Priors prs = this.getModel().getPriors();
        if (prs == null) {
            priors = Collections.EMPTY_MAP;
        } else {
            priors = new TreeMap<>();
            for (Priors.Prior prior : prs.getPrior()) {
                priors.put(prior.getName(), prior);
            }
        }
    }

    /**
     * Run the model. This is the entry point to the model from the framework, up til now
     * the framework has read any configuration files and processed data mentioned therein.
     */
    public abstract void run();
    @Getter
    private Models.Model model;
    @Getter
    private Map<String, Parameters.Parameter> parameters;
    @Getter
    private Map<String, Priors.Prior> priors;
}
