package broadwick.abc;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Getter;

/**
 * An ABC named quantity is an order maintaining store of key-value pairs that can be used to store parameters
 * for a model or the summary statistic calculated by a model.
 */
public class AbcNamedQuantity {

    /**
     * Create a single set of named quantities.
     * @param parameters the collection of named values.
     */
    public AbcNamedQuantity(final Map<String, Double> parameters) {
        if (parameters instanceof LinkedHashMap) {
            this.parameters = new LinkedHashMap<>();
            this.parameters.putAll(parameters);
        } else {
            throw new IllegalArgumentException("The parameters of an ABC Model must be a LinkedHashMap");
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
