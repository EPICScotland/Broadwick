package broadwick.montecarlo.path;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/**
 * A representation of the current step in a Markov Chain. The step is represented by a set of coordinates in the
 * parameter space spanning the possible set of steps.
 */
public class Step {

    /**
     * Create a single step in a Markov Chain.
     * @param coordinates the coordinates of the step in the parameter space of the chain.
     */
    public Step(final Map<String, Double> coordinates) {
        if (coordinates instanceof LinkedHashMap) {
            this.coordinates = new LinkedHashMap<>();
            this.coordinates.putAll(coordinates);
        } else {
            throw new IllegalArgumentException("The coordinates of the Monte Carlo Path step must be a LinkedHashMap");
        }
    }
    
    @Override
    public final String toString() {
        final StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Double> entry : coordinates.entrySet()) {
            sb.append(entry.getValue()).append(',');
        }
        
        // remove the last value separator.
        sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }
    @Getter
    Map<String, Double> coordinates;
}
