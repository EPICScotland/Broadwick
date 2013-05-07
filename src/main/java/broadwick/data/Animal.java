package broadwick.data;

import java.io.Serializable;
import lombok.Data;

/**
 * Utility class for location data.
 */
@Data
@SuppressWarnings({"PMD.UnusedPrivateField","serial"})
public class Animal implements Serializable {
// TODO what about cutom tags??????

    private final String id;
    private final String species;
    private final Integer dateOfBirth;
    private final String locationOfBirth;
    private final Integer dateOfDeath;
    private final String locationOfDeath;
}
