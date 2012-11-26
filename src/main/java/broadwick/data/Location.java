package broadwick.data;

import java.util.Map;
import lombok.Data;

/**
 * Utility class for location data.
 */
@Data
@SuppressWarnings("PMD.UnusedPrivateField")
public class Location {
// TODO what about cutom tags??????

    private final String id;
    private final Double easting;
    private final Double northing;
    private final Map<String, Integer> populations;
}
