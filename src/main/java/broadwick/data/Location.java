package broadwick.data;

import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;

/**
 * Utility class for location data.
 */
@EqualsAndHashCode
@SuppressWarnings("PMD.UnusedPrivateField")
public class Location {
// TODO what about cutom tags??????

    /**
     * Create a new location from a location id and coordinates. A map of species and number of animals at the location
     * can also be given where a deep copy of the data will be made.
     * @param id the location id.
     * @param easting the easting coordinate.
     * @param northing the northing coordinate.
     * @param populations a map of species type and number of animals.
     */
    public Location(final String id, final Double easting, final Double northing, 
                                                            final Map<String, Integer> populations) {
        this.id = id;
        this.easting = easting;
        this.northing = northing;
        this.populations = new HashMap<>();
        for (Map.Entry<String, Integer> population : populations.entrySet()) {
            this.populations.put(population.getKey(), population.getValue());
        }
    }

    /**
     * Get the NULL location, defined as the one with no id, coordinates or occupants.
     * @return an instance of a null location.
     */
    public static Location getNullLocation() {
        return nullLocation;
    }
    
    @Getter
    private final String id;
    @Getter
    private final Double easting;
    @Getter
    private final Double northing;
    @Getter
    private final Map<String, Integer> populations;
    private static Location nullLocation = new Location("", null, null, new HashMap<String, Integer>());
}
