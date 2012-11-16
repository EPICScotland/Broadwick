package broadwick.config;

import broadwick.config.generated.Models;
import broadwick.config.generated.Parameters;
import broadwick.config.generated.Priors;
import broadwick.config.generated.Project;
import broadwick.model.Model;
import java.lang.reflect.Field;
import lombok.extern.slf4j.Slf4j;

/**
 * Validator class for configuration files supplied to Broadwick, the framework
 * for epidemiological modelling.
 */
@Slf4j
public final class ConfigValidator {

    /**
     * Create an validator instance that is capable of checking the content of a
     * configuration file.
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
     * Validate the Models section of the configuration file. Any errors will be
     * saved in the local ValidationErrors.
     */
    private void validateModels() {
        for (Models.Model model : project.getModels().getModel()) {

            // we create (using reflection) the model(s) in the configuration file and check that the parameters
            // and priors in the configuration file are valid for the model (by checking that the model has fields
            // marked with the @Parameter and @Prior annotations).
            final String clazz = model.getClassname();
            final Model modelObj = this.<Model>createObject(Model.class, clazz);

            if (modelObj != null) {
                // Check that the parameters are in the model.
                validateModelParameters(model);

                // Check that the priors are in the model.
                validateModelPriors(model);
            } else {
                errors.addError(String.format("Could not create class [%s]. Does it exist?",clazz));
            }
        }
    }

    /**
     * Validate the parameters of the model.
     * @param model the model object whose parameters we are to validate.
     */
    private void validateModelParameters(final Models.Model model) {
        final String modelName = String.format("The Model %s", model.getClassname());
        final Model modelObj = this.<Model>createObject(Model.class, model.getClassname());
        final Parameters parameters = model.getParameters();

        if (parameters != null) {
            for (Parameters.Parameter parameter : parameters.getParameter()) {
                if (!hasDeclaredField(modelObj, parameter.getName())) {
                    final StringBuilder validationFailures = new StringBuilder();
                    validationFailures.append(modelName).append(" does not contain parameter ").append(parameter.getName());
                    errors.addError(validationFailures.toString());
                }
            }
        }
    }

    /**
     * Validate the priors of the model.
     * @param model the model object whose priors we are to validate.
     */
    private void validateModelPriors(final Models.Model model) {
        final String modelName = String.format("The Model %s", model.getClassname());
        final Model modelObj = this.<Model>createObject(Model.class, model.getClassname());
        final Priors priors = model.getPriors();

        if (priors != null) {
            for (Priors.Prior prior : priors.getPrior()) {
                if (!hasDeclaredField(modelObj, prior.getName())) {
                    final StringBuilder validationFailures = new StringBuilder();
                    validationFailures.append(modelName).append(" does not contain the prior ");
                    validationFailures.append(prior.getName()).append(" declared as a prior.");
                    errors.addError(validationFailures.toString());
                }
            }
        }
    }

    /**
     * Checks that a model contains a given parameter as a field.
     * @param model the model object to be checked.
     * @param parameter the name of the parameter (class field).
     * @return true if the model class contains the field, false otherwise.
     */
    private boolean hasDeclaredField(final Model model, final String parameter) {
        try {
            final Field declaredField = model.getClass().getDeclaredField(parameter);
            return declaredField.getName().equalsIgnoreCase(parameter);
        } catch (NoSuchFieldException | SecurityException ex) {
            return false;
        }
    }

    /**
     * If the project has been validated, i.e. it is structurally ok, then this method will simply return the project
     * supplied in this objects constructor, else it will return null.
     * @return null if the project is invalid (i.e. contains major errors) else
     * returns the project object supplied to the constructor.
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
     * @param <T> the type of object to create.
     * @param clazz the class type of the object we will create.
     * @param name the name of the class that is to be instantiated.
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
