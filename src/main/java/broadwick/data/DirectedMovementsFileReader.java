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
 * Reader for the files describing the directed movements (i.e. those that only have a location and a movement_ON or
 * movement_OFF flag)for the simulation. The movements are read and stored in internal databases for later use.
 */
@Slf4j
public class DirectedMovementsFileReader {

    /**
     * Create the movement file reader.
     * @param movementFile the [XML] tag of the config file that is to be read.
     * @param dataReader   the reader object that contains some useful functionality.
     * @param dataDb       the database facade object used to save the data in the movements file section.
     */
    public DirectedMovementsFileReader(final DataFiles.DirectedMovementFile movementFile,
                                       final DataReader dataReader, final MovementDatabaseFacade dataDb) {
        this.movementFile = movementFile;
        this.dataDb = dataDb;

        try {
            final StringBuilder errors = new StringBuilder();
            dataReader.updateSectionDefiniton(ID, movementFile.getIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(MOVEMENT_DATE, movementFile.getMovementDateColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(MOVEMENT_DIRECTION, movementFile.getMovementDirectionColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(LOCATION_ID, movementFile.getLocationColumn(), keyValuePairs, errors, true, SECTION_NAME);
            dataReader.updateSectionDefiniton(SPECIES, movementFile.getSpeciesColumn(), keyValuePairs, errors, true, SECTION_NAME);

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

        try (FileInput fle = new FileInput(movementFile.getName(), movementFile.getSeparator())) {
            List<String> line;
            //CHECKSTYLE:OFF
            while (!(line = fle.readLine()).isEmpty()) {
                //CHECKSTYLE:ON

                final long animalNode = dataDb.getNodeById(line.get(movementFile.getIdColumn()));
                final long locationNode = dataDb.getNodeById(line.get(movementFile.getLocationColumn()));

                final Map<String, Object> properties = new HashMap<>();
                for (Map.Entry<String, Integer> entry : keyValuePairs.entrySet()) {
                    final String value = line.get(entry.getValue() - 1);

                    if (value != null && !value.isEmpty()) {
                        final String property = entry.getKey();
                        properties.put(property, value);
                    }
                }

                dataDb.getInternalDb().createRelationship(animalNode, locationNode, MovementDatabaseFacade.MovementRelationship.MOVES, properties);
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
    private DataFiles.DirectedMovementFile movementFile;
    private Map<String, Integer> keyValuePairs = new HashMap<>();
    @Getter
    private static final String ID = "Id";
    @Getter
    private static final String SPECIES = "Species";
    @Getter
    private static final String LOCATION_ID = "LocationId";
    @Getter
    private static final String MOVEMENT_DATE = "MovementDate";
    @Getter
    private static final String MOVEMENT_DIRECTION = "MovementDirection";
    private static final String NEWLINE = "\n";
    private static final String SECTION_NAME = "DirectedMovementFile";
}
