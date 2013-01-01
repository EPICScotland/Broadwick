package broadwick.data;

import broadwick.BroadwickException;
import broadwick.config.generated.CustomTags;
import broadwick.config.generated.DataFiles;
import broadwick.io.FileInput;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 *
 * @author anthony
 */
@Slf4j
public class TestsFileReader {

    /**
     * Create the tests file reader.
     * @param testsFile  the [xml] tag of the config file that is to be read.
     * @param dataReader the reader object that contains some useful functionality.
     * @param dataDb     the database facade object used to save the data in the locations file section.
     */
    public TestsFileReader(final DataFiles.TestsFile testsFile,
                           final DataReader dataReader, final MovementDatabaseFacade dataDb) {
        this.testsFile = testsFile;
        this.dataDb = dataDb;
        final StringBuilder errors = new StringBuilder();
        dateFormat = testsFile.getDateFormat();

        if (testsFile.getIdColumn() != null) {
            dataReader.updateSectionDefiniton(ID, testsFile.getIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
        }
        if (testsFile.getGroupIdColumn() != null) {
            dataReader.updateSectionDefiniton(GROUP_ID, testsFile.getGroupIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
        }
        if (testsFile.getLocationIdColumn() != null) {
            dataReader.updateSectionDefiniton(LOCATION_ID, testsFile.getLocationIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
        }
        if (testsFile.getTestDateColumn() != null) {
            dataReader.updateSectionDefiniton(TEST_DATE, testsFile.getTestDateColumn(), keyValuePairs, errors, true, SECTION_NAME);
        }
        if (testsFile.getPostiveResultColumn() != null) {
            dataReader.updateSectionDefiniton(POSITIVE_RESULT, testsFile.getPostiveResultColumn(), keyValuePairs, errors, true, SECTION_NAME);
        }
        if (testsFile.getNegativeResultColumn() != null) {
            dataReader.updateSectionDefiniton(NEGATIVE_RESULT, testsFile.getNegativeResultColumn(), keyValuePairs, errors, true, SECTION_NAME);
        }

        if (testsFile.getCustomTags() != null) {
            for (CustomTags.CustomTag tag : testsFile.getCustomTags().getCustomTag()) {
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
        //CHECKSTYLE:OFF
        try (FileInput fle = new FileInput(testsFile.getName(), testsFile.getSeparator())) {
                //CHECKSTYLE:ON
            final Set<String> insertedIds = new HashSet<>();

            final List<String> booleanKeys = Arrays.asList(POSITIVE_RESULT, NEGATIVE_RESULT);
            final List<String> dateKeys = Arrays.asList(TEST_DATE);
            DateTimeFormatter pattern = null;
            if (dateFormat != null) {
                pattern = DateTimeFormat.forPattern(dateFormat);
            }

            //CHECKSTYLE:OFF
            while (!(line = fle.readLine()).isEmpty()) {
                //CHECKSTYLE:ON
                final String nodeId = getId(line);

                final Map<String, Object> properties = new HashMap<>();
                properties.put("index", nodeId);

                for (Map.Entry<String, Integer> entry : keyValuePairs.entrySet()) {
                    final String value = line.get(entry.getValue() - 1);

                    if (value != null && !value.isEmpty()) {
                        final String property = entry.getKey();
                        if (dateKeys.contains(property)) {
                            if (pattern == null) {
                                throw new BroadwickException(String.format("Date column set but no date format in configuration for file %s ", testsFile.getName()));
                            } else {
                                final DateTime date = pattern.parseDateTime(value);
                                properties.put(property, Days.daysBetween(this.dataDb.getZeroDate(), date).getDays());
                            }
                        } else if (booleanKeys.contains(property)) {
                            final Boolean result = Integer.parseInt(value) == 0 ? Boolean.TRUE : Boolean.FALSE;
                            properties.put(property, result);
                        } else {
                            properties.put(property, value);
                        }
                    }

                }
                properties.put(MovementDatabaseFacade.TYPE, MovementDatabaseFacade.TEST);

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
            log.trace(String.format(errorMsg, testsFile.getName(), line));
            throw new BroadwickException(String.format(errorMsg, testsFile.getName(), line) + NEWLINE + Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            final String errorMsg = "Could not open file %s";
            log.trace(String.format(errorMsg, testsFile.getName()));
            throw new BroadwickException(String.format(errorMsg, testsFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        }
        return inserted;

    }

    /**
     * Get the id of the test which will be used as the index in the database. The test id can be either the individual 
     * id, the group/flock/herd id or the location id. Use the ids in this order else throw an exception if none are found.
     * Note, there may be a problem here in the future - what we want is to consistently use the individual, group or l
     * location id but in this algorithm we are checking this on a line by line basis.
     * @param line a collection of tokens read from a line in the database.
     * @return the correct id to use as the index in the database.
     */
    private String getId(final List<String> line) {

        // The test id can be either the individual id, the group/flock/herd id or the location id. Use the
        // ids in this order else throw an exception if none are found.
        String nodeId = StringUtils.EMPTY;
        if (testsFile.getIdColumn() != null) {
            nodeId = line.get(testsFile.getIdColumn() - 1);
        } else if (testsFile.getGroupIdColumn() != null) {
            nodeId = line.get(testsFile.getGroupIdColumn() - 1);
        } else if (testsFile.getLocationIdColumn() != null) {
            nodeId = line.get(testsFile.getLocationIdColumn() - 1);
        } else {
            throw new BroadwickException("No valid test id set in configuration file");
        }
        return nodeId;
    }

    private MovementDatabaseFacade dataDb;
    private DataFiles.TestsFile testsFile;
    private String dateFormat;
    private Map<String, Integer> keyValuePairs = new HashMap<>();
    @Getter
    private static final String ID = "Id";
    @Getter
    private static final String GROUP_ID = "GroupId";
    @Getter
    private static final String LOCATION_ID = "LocationId";
    @Getter
    private static final String TEST_DATE = "TestDate";
    @Getter
    private static final String POSITIVE_RESULT = "PositiveResult";
    @Getter
    private static final String NEGATIVE_RESULT = "NegativeResult";
    private static final String NEWLINE = "\n";
    private static final String SECTION_NAME = "LocationsFile";
}
