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
 * Reader for the files describing the populations for the simulation.
 */
@Slf4j
public class PopulationsFileReader {

    /**
     * Create the populations file reader.
     * @param populationFile the [xml] tag of the config file that is to be read.
     * @param dataReader     the reader object that contains some useful functionality.
     * @param dataDb         the database facade object used to save the data in the populations file section.
     */
    public PopulationsFileReader(final DataFiles.PopulationFile populationFile,
                                 final DataReader dataReader, final MovementDatabaseFacade dataDb) {
        this.populationFile = populationFile;
        this.dataDb = dataDb;
        this.dataReader = dataReader;

        final StringBuilder errors = new StringBuilder();

        if (populationFile.getLifeHistory() != null) {
            keyValuePairs.putAll(readLifeHistory());
        } else if (populationFile.getPopulation() != null) {
            keyValuePairs.putAll(readPopulation());
        } else {
            throw new BroadwickException("Cannot read Populations section - No lifeHistory or population section found.");
        }

        if (populationFile.getCustomTags() != null) {
            for (CustomTags.CustomTag tag : populationFile.getCustomTags().getCustomTag()) {
                dataReader.updateSectionDefiniton(tag.getName(), tag.getColumn(), keyValuePairs, errors, true, SECTION_NAME);
            }
        }
    }

    /**
     * Read the life history declarations of a population file section in the configuration file, if there are errors,
     * they are noted and and exception is thrown with ALL the errors at the end of the method.
     * @return A com.google.common.collect.Table element with a column name, location (i.e. column) in the data file and
     *         the database type of the element (VARCHAR, DATE, INT or DOUBLE).
     */
    private Map<String, Integer> readLifeHistory() {
        final Map<String, Integer> elements = new HashMap<>();
        final StringBuilder errors = new StringBuilder();

        dataReader.updateSectionDefiniton(ID, populationFile.getLifeHistory().getIdColumn(), keyValuePairs, errors, true, SECTION_NAME);
        dataReader.updateSectionDefiniton(DATE_OF_BIRTH, populationFile.getLifeHistory().getDateOfBirthColumn(), keyValuePairs, errors, true, SECTION_NAME);
        dataReader.updateSectionDefiniton(LOCATION_OF_BIRTH, populationFile.getLifeHistory().getLocationOfBirthColumn(), keyValuePairs, errors, true, SECTION_NAME);
        dataReader.updateSectionDefiniton(DATE_OF_DEATH, populationFile.getLifeHistory().getDateOfDeathColumn(), keyValuePairs, errors, true, SECTION_NAME);

        if (populationFile.getLifeHistory().getSpeciesColumn() != null) {
            dataReader.updateSectionDefiniton(SPECIES, populationFile.getLifeHistory().getSpeciesColumn(), keyValuePairs, errors, true, SECTION_NAME);
        }

        if (populationFile.getLifeHistory().getLocationOfDeathColumn() != null) {
            dataReader.updateSectionDefiniton(LOCATION_OF_DEATH, populationFile.getLifeHistory().getLocationOfDeathColumn(), keyValuePairs, errors, true, SECTION_NAME);
        }

        if (errors.length() > 0) {
            throw new BroadwickException(errors.toString());
        }

        return elements;
    }

    /**
     * Read the populations declarations of a population file section in the configuration file, if there are errors,
     * they are noted and and exception is thrown with ALL the errors at the end of the method.
     * @return A com.google.common.collect.Table element with a column name, location (i.e. column) in the data file and
     *         the database type of the element (VARCHAR, DATE, INT or DOUBLE).
     */
    private Map<String, Integer> readPopulation() {
        final Map<String, Integer> elements = new HashMap<>();
        final StringBuilder errors = new StringBuilder();

        dataReader.updateSectionDefiniton(LOCATION, populationFile.getPopulation().getLocationIdColumn(), elements, errors, true, SECTION_NAME);
        dataReader.updateSectionDefiniton(POPULATION, populationFile.getPopulation().getPopulationSizeColumn(), elements, errors, true, SECTION_NAME);
        dataReader.updateSectionDefiniton(DATE_LC, populationFile.getPopulation().getPopulationDateColumn(), elements, errors, true, SECTION_NAME);

        if (populationFile.getPopulation().getSpeciesColumn() != null) {
            dataReader.updateSectionDefiniton(SPECIES, populationFile.getPopulation().getSpeciesColumn(), keyValuePairs, errors, true, SECTION_NAME);
        }

        if (errors.length() > 0) {
            throw new BroadwickException(errors.toString());
        }

        return elements;
    }

    /**
     * Insert the data from the input file into the database. The data structure has been read and the database set up
     * already so this method simply reads the file and extracts the relevant information, storing it in the database.
     * @return the number of rows read
     */
    public final int insert() {
        int inserted = 0;
        List<String> line = Collections.EMPTY_LIST;
        try (FileInput fle = new FileInput(populationFile.getName(), populationFile.getSeparator())) {
            final Set<String> insertedIds = new HashSet<>();
            //CHECKSTYLE:OFF
            while (!(line = fle.readLine()).isEmpty()) {
                //CHECKSTYLE:ON

                //TODO what about the populations? we only get the id from the LifeHistory.....
                final String nodeId = line.get(populationFile.getLifeHistory().getIdColumn() - 1);
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
            log.trace(String.format(errorMsg, populationFile.getName(), line));
            throw new BroadwickException(String.format(errorMsg, populationFile.getName(), line) + NEWLINE + Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            final String errorMsg = "Could not open file %s";
            log.trace(String.format(errorMsg, populationFile.getName()));
            throw new BroadwickException(String.format(errorMsg, populationFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        }
        return inserted;
    }

    private MovementDatabaseFacade dataDb;
    private DataReader dataReader;
    private DataFiles.PopulationFile populationFile;
    private Map<String, Integer> keyValuePairs = new HashMap<>();
    @Getter
    private static final String ID = "Id";
    private static final String LOCATION = "Location";
    private static final String POPULATION = "Population";
    private static final String DATE_LC = "Date";
    @Getter
    private static final String SPECIES = "Species";
    @Getter
    private static final String DATE_OF_BIRTH = "DateOfBirth";
    @Getter
    private static final String LOCATION_OF_BIRTH = "LocationOfBirth";
    @Getter
    private static final String DATE_OF_DEATH = "DateOfDeath";
    @Getter
    private static final String LOCATION_OF_DEATH = "LocationOfDeath";
    private static final String NEWLINE = "\n";
    private static final String SECTION_NAME = "PopulationsFile";
}
