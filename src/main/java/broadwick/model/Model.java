/*
 * Copyright 2013 University of Glasgow.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package broadwick.model;

import broadwick.BroadwickException;
import broadwick.config.generated.Parameter;
import broadwick.config.generated.Prior;
import broadwick.config.generated.UniformPrior;
import broadwick.data.Lookup;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This interface declares a model for the Broadwick framework. To create a model for this framework (by model we mean
 * both the model and the methodology for running/solving it) the user creates an implementation of this class and
 * refers to it in the framework configuration file.  <code>
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
@Slf4j
public abstract class Model {

    /**
     * Set the xml string for the <model> element in the configuration file.
     * @param model a string representation of the xml section defining the configuration of the model.
     */
    public final void setModelConfiguration(final String model) {
        this.model = model;
        this.priors = new ArrayList<>();
    }

    /**
     * Set the lookup object that allows access to the data files specified in the configuration file.
     * @param lookup the XML element corresponding to the Model element in the config.
     */
    public final void setModelDataLookup(final Lookup lookup) {
        this.lookup = lookup;
    }

    /**
     * Set the list of parameters for the model.
     * @param parameters a collection of parameters for the model.
     */
    public final void setModelParameters(final List<Parameter> parameters) {
        this.parameters = parameters;
    }

    /**
     * Set the list of priors for the model.
     * @param priors a collection of priors for the model.
     */
    public final void setModelPriors(final List<Prior> priors) {
        this.priors.addAll(priors);
    }

    /**
     * Get the prior of a paramter for the model given the parameter name (as defined in the config file).
     * @param name the name of the parameter.
     * @return the prior defeind in the configuration file.
     */
    public final UniformPrior getUniformPrior(final String name) {
        return ((UniformPrior) Iterables.find(priors, new Predicate<Prior>() {
            @Override
            public boolean apply(final Prior prior) {
                return (name.equals(prior.getId()));
            }
        }));
    }

    /**
     * Get the value of a parameter for the model given the parameter name (as defined in the config file).
     * @param name the name of the parameter.
     * @return a string value of the value for the parameter.
     */
    public final String getParameterValue(final String name) {
        try {
            return Iterables.find(parameters, new Predicate<Parameter>() {
                @Override
                public boolean apply(final Parameter parameter) {
                    return (name.equals(parameter.getId()));
                }
            }).getValue();
        } catch (java.util.NoSuchElementException e) {
            log.error("{} in is not configured for the model.", name);
            throw new BroadwickException(String.format("Could not find parameter %s in configuration file.", name));
        }
    }

    /**
     * Get the value (as a double) of a parameter for the model given the parameter name (as defined in the config
     * file).
     * @param name the name of the parameter.
     * @return a string value of the value for the parameter.
     */
    public final Double getParameterValueAsDouble(final String name) {
        return Double.parseDouble(getParameterValue(name));
    }

    /**
     * Get the value (as an integer) of a parameter for the model given the parameter name (as defined in the config
     * file).
     * @param name the name of the parameter.
     * @return a string value of the value for the parameter.
     */
    public final Integer getParameterValueAsInteger(final String name) {
        return Integer.parseInt(getParameterValue(name));
    }

    /**
     * Get the value (as a boolean) of a parameter for the model given the parameter name (as defined in the config
     * file).
     * @param name the name of the parameter.
     * @return a string value of the value for the parameter.
     */
    public final Boolean getParameterValueAsBoolean(final String name) {
        return Boolean.parseBoolean(getParameterValue(name));
    }

    /**
     * Initialise the model. This method is called by the framework before calling the models run method to allow the
     * implementation of the model to perform any initialisation required.
     */
    public abstract void init();

    /**
     * Run the model. This is the entry point to the model from the framework, up til now the framework has read any
     * configuration files and processed data mentioned therein.
     */
    public abstract void run();

    /**
     * End the model. This method is called by the framework after the model has finished running.
     */
    public abstract void finalise();
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private String model;
    @SuppressWarnings("PMD.UnusedPrivateField")
    @Getter
    private Lookup lookup;
    @SuppressWarnings("PMD.UnusedPrivateField")
    @Getter
    private List<Parameter> parameters;
    @SuppressWarnings("PMD.UnusedPrivateField")
    @Getter
    private List<Prior> priors;
}
