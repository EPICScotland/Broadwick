package broadwick.abc;

/**
 * This interface determines how the Prior distribution(s) should be sampled.
 */
public interface AbcPriorsSampler {
       
    /** 
     * Sample from the models Priors to obtain a set of model parameters.
     * @return the new sampled parameters.
     */
    AbcNamedQuantity sample();
    
}
