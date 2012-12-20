package broadwick.data;

import broadwick.BroadwickException;
import broadwick.config.generated.CustomTags;
import broadwick.config.generated.DataFiles;
import broadwick.io.FileInput;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reader for the files describing the full movements (i.e. those that specify both the departure location and the
 * destination location of a movement)for the simulation. The movements are read and stored in internal databases for
 * later use.
 */
@Slf4j
public class FullMovementsFileReader {

    /**
     * Create the movement file reader.
     * @param movementFile the [XML] tag of the config file that is to be read.
     * @param dataReader   the reader object that contains some useful functionality.
     * @param dataDb       the database facade object used to save the data in the movements file section.
     */
    public FullMovementsFileReader(final DataFiles.FullMovementFile movementFile,
                                   final DataReader dataReader, final MovementDatabaseFacade dataDb) {
        this.movementFile = movementFile;
        this.dataDb = dataDb;

        try {
            final StringBuilder errors = new StringBuilder();

            dataReader.updateSectionDefiniton(ID, movementFile.getIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(DEPARTURE_DATE, movementFile.getDepartureDateColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(DEPARTURE_ID, movementFile.getDepartureLocationIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(DESTINATION_DATE, movementFile.getDestinationDateColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(DESTINATION_ID, movementFile.getDestinationLocationIdColumn(), keyValuePairs, errors, true, SECTION_NAME);

            if (movementFile.getSpeciesColumn() != null) {
                dataReader.updateSectionDefiniton(SPECIES, movementFile.getSpeciesColumn(), keyValuePairs, errors, true, SECTION_NAME);
            }

            if (movementFile.getMarketIdColumn() != null) {
                dataReader.updateSectionDefiniton(MARKET_ID, movementFile.getMarketIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
            }
            if (movementFile.getMarketDateColumn() != null) {
                dataReader.updateSectionDefiniton(MARKET_DATE, movementFile.getMarketDateColumn(), keyValuePairs, errors, true, SECTION_NAME);
            }

            if (movementFile.getCustomTags() != null) {
                for (CustomTags.CustomTag tag : movementFile.getCustomTags().getCustomTag()) {
                    dataReader.updateSectionDefiniton(tag.getName(), tag.getColumn(), keyValuePairs, errors, true, SECTION_NAME);
                }
            }

            if (errors.length() > 0) {
                throw new BroadwickException(errors.toString());
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

        List<String> line = Collections.EMPTY_LIST;
        try (FileInput fle = new FileInput(movementFile.getName(), movementFile.getSeparator())) {

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
            final String errorMsg = "Could not read file %s; last line read %s";
            log.trace(String.format(errorMsg, movementFile.getName(), line));
            throw new BroadwickException(String.format(errorMsg, movementFile.getName(), line) + NEWLINE + Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            final String errorMsg = "Could not open file %s";
            log.trace(String.format(errorMsg, movementFile.getName()));
            throw new BroadwickException(String.format(errorMsg, movementFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        }
        return inserted;
    }

    private MovementDatabaseFacade dataDb;
    private DataFiles.FullMovementFile movementFile;
    private Map<String, Integer> keyValuePairs = new HashMap<>();
    @Getter
    private static final String ID = "Id";
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
    private static final String SPECIES = "Species";
    private static final String NEWLINE = "\n";
    private static final String SECTION_NAME = "FullMovementsFile";
}
