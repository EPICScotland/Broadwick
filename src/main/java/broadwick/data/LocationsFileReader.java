package broadwick.data;

import broadwick.BroadwickException;
import broadwick.config.generated.CustomTags;
import broadwick.config.generated.DataFiles;
import broadwick.io.FileInput;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reader for the files describing the locations for the simulation.
 */
@Slf4j
public class LocationsFileReader {

    /**
     * Create the locations file reader.
     * @param locationsFile the [xml] tag of the config file that is to be read.
     * @param dataReader    the reader object that contains some useful functionality.
     * @param dataDb        the database facade object used to save the data in the locations file section.
     */
    public LocationsFileReader(final DataFiles.LocationsFile locationsFile,
                               final DataReader dataReader, final MovementDatabaseFacade dataDb) {
        this.locationsFile = locationsFile;
        this.dataDb = dataDb;
        final StringBuilder errors = new StringBuilder();

        dataReader.updateSectionDefiniton(ID, locationsFile.getLocationIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
        dataReader.updateSectionDefiniton(EASTING, locationsFile.getEastingColumn(), keyValuePairs, errors, true, SECTION_NAME);
        dataReader.updateSectionDefiniton(NORTHING, locationsFile.getNorthingColumn(), keyValuePairs, errors, true, SECTION_NAME);

        if (locationsFile.getCustomTags() != null) {
            for (CustomTags.CustomTag tag : locationsFile.getCustomTags().getCustomTag()) {
                dataReader.updateSectionDefiniton(tag.getName(), tag.getColumn(), keyValuePairs, errors, true, SECTION_NAME);
            }
        }

        if (errors.length() > 0) {
            throw new BroadwickException(errors.toString());
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
        try (FileInput fle = new FileInput(locationsFile.getName(), locationsFile.getSeparator())) {
                        final Set<String> insertedIds = new HashSet<>();

            //CHECKSTYLE:OFF
            while (!(line = fle.readLine()).isEmpty()) {
                //CHECKSTYLE:ON
                final String nodeId = line.get(locationsFile.getLocationIdColumn() - 1);
                final Map<String, Object> properties = new HashMap<>();
                properties.put("index", nodeId);

                for (Map.Entry<String, Integer> entry : keyValuePairs.entrySet()) {
                    final String value = line.get(entry.getValue() - 1);

                    if (value != null && !value.isEmpty()) {
                        final String property = entry.getKey();
                        properties.put(property, value);
                    }
                }
                
                final Long node = dataDb.createNode(nodeId, properties);
                if (node != null && !insertedIds.contains(nodeId)) {
                    dataDb.getIndex().add(node, properties);
                    insertedIds.add(nodeId);
                    inserted++;
                    if (inserted % 100000 == 0) {
                        dataDb.getIndex().flush();
                    }
                }
            }

        } catch (IndexOutOfBoundsException | NoSuchElementException | NumberFormatException | BroadwickException e) {
            final String errorMsg = "Could not read file %s; last line read %s";
            log.trace(String.format(errorMsg, locationsFile.getName(), line));
            throw new BroadwickException(String.format(errorMsg, locationsFile.getName(), line) + NEWLINE + Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            final String errorMsg = "Could not open file %s";
            log.trace(String.format(errorMsg, locationsFile.getName()));
            throw new BroadwickException(String.format(errorMsg, locationsFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        }
        return inserted;
    }

    private MovementDatabaseFacade dataDb;
    private DataFiles.LocationsFile locationsFile;
    private Map<String, Integer> keyValuePairs = new HashMap<>();
    @Getter
    private static final String ID = "Id";
    @Getter
    private static final String EASTING = "Easting";
    @Getter
    private static final String NORTHING = "Northing";
    private static final String NEWLINE = "\n";
    private static final String SECTION_NAME = "LocationsFile";
}
