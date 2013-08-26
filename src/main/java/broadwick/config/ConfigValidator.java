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
package broadwick.config;

import broadwick.config.generated.Models;
import broadwick.config.generated.Project;
import broadwick.model.Model;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator class for configuration files supplied to Broadwick, the framework for epidemiological modelling.
 */
@Slf4j
public final class ConfigValidator {

    /**
     * Create an validator instance that is capable of checking the content of a configuration file.
     * @param project the contents of the [read] configuration file. project.
     */
    public ConfigValidator(final Project project) {
        this.project = project;
    }

    /**
     * Validate the project.
     * @return a ConfigValidationErrors object that contains all the errors (if any).
     */
    public ConfigValidationErrors validate() {
        try {
            validateModels();
        } catch (Exception e) {
            errors.addError("Could not validate the configuration file. " + e.getLocalizedMessage());
        }

        return errors;
    }

    /**
     * Validate the Models section of the configuration file. Any errors will be saved in the local ValidationErrors.
     */
    private void validateModels() {
        for (Models.Model model : project.getModels().getModel()) {

            // we create (using reflection) the model(s) in the configuration file and check that the parameters
            // and priors in the configuration file are valid for the model (by checking that the model has fields
            // marked with the @Parameter and @Prior annotations).
            final String clazz = model.getClassname();
            final Model modelObj = this.<Model>createObject(Model.class, clazz);

            if (modelObj == null) {
                errors.addError(String.format("Could not create class [%s]. Does it exist?", clazz));
            }
            // TODO else perform any validation
        }
    }

    /**
     * If the project has been validated, i.e. it is structurally ok, then this method will simply return the project
     * supplied in this objects constructor, else it will return null.
     * @return null if the project is invalid (i.e. contains major errors) else returns the project object supplied to
     *         the constructor.
     */
    public Project getValidatedProject() {
        if (errors.isValid()) {
            return project;
        } else {
            return null;
        }
    }

    /**
     * Create an object through reflection, to use this method to create a solver object
     * <code>
     * Solver solverObj = this.<Solver>createObject(Solver.class, "RungeKutta4");
     * </code>
     * @param <T>   the type of object to create.
     * @param clazz the class type of the object we will create.
     * @param name  the name of the class that is to be instantiated.
     * @return the created object.
     */
    private <T> T createObject(final Class<T> clazz, final String name) {
        T object = null;
        try {
            // Check if the class exists
            object = clazz.cast(Class.forName(name).newInstance());

        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
            errors.addError("Could not find " + clazz.getCanonicalName() + " class <" + name + ">");
        }
        return object;
    }

    private Project project;
    private ConfigValidationErrors errors = new ConfigValidationErrors();
}
