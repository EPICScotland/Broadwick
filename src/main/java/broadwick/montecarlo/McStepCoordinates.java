package broadwick.montecarlo;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/**
 * The coordinates of a Monte Carlo step. These are an order maintaining store of key-value pairs that can be used to 
 * store parameters for a model and where the toString() method returns a CSV list of the values in a consistent order.
 */
class McStepCoordinates {
    
    /**
     * Create an object to store the coordinates of a Monte Carlo step.
     * @param parameters the collection of named values.
     */
    public McStepCoordinates(final Map<String, Double> parameters) {
        if (parameters instanceof LinkedHashMap) {
            this.parameters = new LinkedHashMap<>();
            this.parameters.putAll(parameters);
        } else {
            throw new IllegalArgumentException("The parameters of McStepCoordinates must be a LinkedHashMap");
        }
    }
    
    
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : parameters.entrySet()) {
            sb.append(entry.getValue()).append(',');
        }

        // remove the last value separator.
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }
    
    @Getter
    private Map<String, Double> parameters;
}