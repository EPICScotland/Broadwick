package broadwick.data;

import broadwick.BroadwickException;
import broadwick.config.generated.CustomTags;
import broadwick.config.generated.DataFiles;
import broadwick.io.FileInput;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reader for the files describing the batched movements (i.e. those that do not specify an individual but rather the
 * number of individuals moved)for the simulation. The movements are read and stored in internal databases for later
 * use.
 */
@Slf4j
public class BatchedMovementsFileReader {

    /**
     * Create the movement file reader.
     * @param movementFile the [xml] tag of the config file that is to be read.
     * @param dataReader   the reader object that contains some useful functionality.
     * @param dataDb       the database facade object used to save the data in the movements file section.
     */
    public BatchedMovementsFileReader(final DataFiles.BatchMovementFile movementFile,
                                      final DataReader dataReader, final MovementDatabaseFacade dataDb) {
        this.movementFile = movementFile;
        this.dataDb = dataDb;

        try {
            final StringBuilder errors = new StringBuilder();
            dataReader.updateSectionDefiniton(BATCH_SIZE, movementFile.getBatchSizeColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(DEPARTURE_DATE, movementFile.getDepartureDateColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(DEPARTURE_ID, movementFile.getDepartureLocationIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(DESTINATION_DATE, movementFile.getDestinationDateColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(DESTINATION_ID, movementFile.getDestinationLocationIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(MARKET_ID, movementFile.getMarketIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(MARKET_DATE, movementFile.getMarketDateColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(SPECIES, movementFile.getSpeciesColumn(), keyValuePairs, errors, true, SECTION_NAME);

            if (movementFile.getCustomTags() != null) {
                for (CustomTags.CustomTag tag : movementFile.getCustomTags().getCustomTag()) {
                    dataReader.updateSectionDefiniton(tag.getName(), tag.getColumn(), keyValuePairs, errors, true, SECTION_NAME);
                }
            }
        } catch (BroadwickException e) {
            log.error("Cannot read Movements section correctly.\n{}", e.getLocalizedMessage());
        }
    }

    /**
     * Insert the data from the input file into the database. The data structure has been read and the database set up
     * already so this method simply reads the file and extracts the relevant information, storing it in the database.
     * @return the number of rows read
     */
    public final int insert() {
        int inserted = 0;

        try (FileInput fle = new FileInput(movementFile.getName(), movementFile.getSeparator())) {
            List<String> line;
            //CHECKSTYLE:OFF
            while (!(line = fle.readLine()).isEmpty()) {
                //CHECKSTYLE:ON

                final long departureNode = dataDb.getNodeById(line.get(movementFile.getDepartureLocationIdColumn()));
                final long destinationNode = dataDb.getNodeById(line.get(movementFile.getDestinationLocationIdColumn()));

                final Map<String, Object> properties = new HashMap<>();
                for (Map.Entry<String, Integer> entry : keyValuePairs.entrySet()) {
                    final String value = line.get(entry.getValue() - 1);

                    if (value != null && !value.isEmpty()) {
                        final String property = entry.getKey();
                        properties.put(property, value);
                    }
                }

                dataDb.getInternalDb().createRelationship(departureNode, destinationNode, MovementDatabaseFacade.MovementRelationship.MOVES, properties);
                inserted++;
            }

        } catch (IndexOutOfBoundsException | NoSuchElementException | NumberFormatException | BroadwickException e) {
            final String errorMsg = "Adding to reading list for %s";
            log.trace(String.format(errorMsg, movementFile.getName()));
            throw new BroadwickException(String.format(errorMsg, movementFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            final String errorMsg = "Could not open file %s";
            log.trace(String.format(errorMsg, movementFile.getName()));
            throw new BroadwickException(String.format(errorMsg, movementFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        }
        return inserted;
    }

    private MovementDatabaseFacade dataDb;
    private DataFiles.BatchMovementFile movementFile;
    private Map<String, Integer> keyValuePairs = new HashMap<>();
    @Getter
    private static final String BATCH_SIZE = "batchSize";
    @Getter
    private static final String DEPARTURE_DATE = "DepartureDate";
    @Getter
    private static final String DEPARTURE_ID = "DepartureId";
    @Getter
    private static final String DESTINATION_DATE = "DestinationDate";
    @Getter
    private static final String DESTINATION_ID = "DestinationId";
    @Getter
    private static final String MARKET_ID = "MarketId";
    @Getter
    private static final String MARKET_DATE = "MarketDate";
    @Getter
    private static final String SPECIES = "Sprcies";
    private static final String NEWLINE = "\n";
    private static final String SECTION_NAME = "BatchedMovementFile";
}
