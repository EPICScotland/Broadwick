package broadwick.data;

import java.io.Serializable;
import lombok.Data;

/**
 * Utility class for test data.
 */
@Data
@SuppressWarnings({"PMD.UnusedPrivateField","serial"})
public class Test implements Serializable {
// TODO what about custom tags??????

    private final String id;
    private final String group;
    private final String location;
    private final Integer testDate;
    private final Boolean positiveResult;
    private final Boolean negativeResult;
}
