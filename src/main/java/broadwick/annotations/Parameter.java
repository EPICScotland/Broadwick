package broadwick.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that marks a field of a mode as a parameter for the model. Each field marked as
 * a parameter MUST be declared in the <parameters> section of the configuration file.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Parameter {

    /**
     * Provide a description for the parameter.
     */
     String hint() default "";
}