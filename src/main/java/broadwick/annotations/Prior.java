package broadwick.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a parameter of a mode as a prior. Each parameter marked
 * as a prior MUST be declared in the <priors> section of the configuration
 * file.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Prior {

    /**
     * The minimum value for the prior.
     */
    double min();

    /**
     * The maximum value of the prior.
     */
    double max();

    /**
     * The name of the distribution describing this prior.
     */
    String distribution();
}
