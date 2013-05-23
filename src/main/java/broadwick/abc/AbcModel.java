package broadwick.abc;

/**
 * A simple model class thats runs a model and creates named quantities, i.e. a map containing the vlaues and their
 * names.
 */
public interface AbcModel {

    /**
     * Run the model to generate data.
     * @param parameters the model parameters.
     * @return generated data in the form of named values.
     */
    AbcNamedQuantity run(final AbcNamedQuantity parameters);
}
