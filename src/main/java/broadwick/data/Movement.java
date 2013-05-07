package broadwick.data;

import java.io.Serializable;
import lombok.Data;

/**
 * Utility class for movement data.
 */
@Data
@SuppressWarnings({"PMD.UnusedPrivateField","serial"})
public class Movement implements Serializable {
// TODO what about cutom tags??????

    private final String id;
    private final Integer batchSize;
    private final Integer departureDate;
    private final String departureId;
    private final Integer destinationDate;
    private final String destinationId;
    private final Integer marketDate;
    private final String marketId;
    private final String species;
}
