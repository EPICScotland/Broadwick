package broadwick.data;

import broadwick.BroadwickException;
import broadwick.config.generated.CustomTags;
import broadwick.config.generated.DataFiles;
import broadwick.io.FileInput;
import com.google.common.base.Throwables;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.io.IOException;
import java.text.ParseException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 * Reader for the files describing the populations for the simulation.
 */
@Slf4j
public class PopulationsFileReader implements Callable<Integer> {

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
        dateFormat = DateTimeFormat.forPattern(populationFile.getDateFormat());

        if (populationFile.getLifeHistory() != null) {
            populationDescr.putAll(readLifeHistory());
        } else if (populationFile.getPopulation() != null) {
            populationDescr.putAll(readPopulation());
        } else {
            throw new BroadwickException("Cannot read Populations section - No lifeHistory or population section found.");
        }

        if (populationFile.getCustomTags() != null) {
            for (CustomTags.CustomTag tag : populationFile.getCustomTags().getCustomTag()) {
                populationDescr.put(tag.getName(), tag.getColumn(), tag.getType());
            }
        }
    }

    /**
     * Read the life history declarations of a population file section in the configuration file, if there are errors,
     * they are noted and and exception is thrown with ALL the errors at the end of the method.
     * @return A com.google.common.collect.Table element with a column name, location (i.e. column) in the data file and
     *         the database type of the element (VARCHAR, DATE, INT or DOUBLE).
     */
    private Table<String, Integer, String> readLifeHistory() {
        final Table<String, Integer, String> elements = HashBasedTable.create();
        final StringBuilder errors = new StringBuilder();

        dataReader.addElement(ID, populationFile.getLifeHistory().getIdColumn(), VARCHAR, populationDescr, errors, true, SECTION_NAME);
        dataReader.addElement(SPECIES, populationFile.getLifeHistory().getSpeciesColumn(), VARCHAR, populationDescr, errors, true, SECTION_NAME);
        dataReader.addElement(DATE_OF_BIRTH, populationFile.getLifeHistory().getDateOfBirthColumn(), DATE, populationDescr, errors, true, SECTION_NAME);
        dataReader.addElement(LOCATION_OF_BIRTH, populationFile.getLifeHistory().getLocationOfBirthColumn(), VARCHAR, populationDescr, errors, true, SECTION_NAME);
        dataReader.addElement(DATE_OF_DEATH, populationFile.getLifeHistory().getDateOfDeathColumn(), DATE, populationDescr, errors, true, SECTION_NAME);
        dataReader.addElement(LOCATION_OF_DEATH, populationFile.getLifeHistory().getLocationOfDeathColumn(), VARCHAR, populationDescr, errors, true, SECTION_NAME);

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
    private Table<String, Integer, String> readPopulation() {
        final Table<String, Integer, String> elements = HashBasedTable.create();
        final StringBuilder errors = new StringBuilder();

        dataReader.addElement(LOCATION, populationFile.getPopulation().getLocationIdColumn(), VARCHAR, elements, errors, true, SECTION_NAME);
        dataReader.addElement(POPULATION, populationFile.getPopulation().getPopulationSizeColumn(), INT, elements, errors, true, SECTION_NAME);
        dataReader.addElement(DATE_LC, populationFile.getPopulation().getPopulationDateColumn(), DATE, elements, errors, true, SECTION_NAME);
        dataReader.addElement(SPECIES, populationFile.getPopulation().getSpeciesColumn(), VARCHAR, elements, errors, true, SECTION_NAME);

        if (errors.length() > 0) {
            throw new BroadwickException(errors.toString());
        }

        return elements;
    }

    @Override
    public final Integer call() {
        int readSoFar = 0;

        try (FileInput fle = new FileInput(populationFile.getName(), populationFile.getSeparator())) {
            List<String> line;
            //CHECKSTYLE:OFF
            while (!(line = fle.readLine()).isEmpty()) {
                //CHECKSTYLE:ON
                createDatabaseEntry(line);
                readSoFar++;
            }
        } catch (NumberFormatException | ParseException | IndexOutOfBoundsException | NoSuchElementException | BroadwickException e) {
            final String errorMsg = "Adding to reading list for %s";
            log.trace(String.format(errorMsg, populationFile.getName()));
            throw new BroadwickException(String.format(errorMsg, populationFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            final String errorMsg = "Could not open file %s";
            log.trace(String.format(errorMsg, populationFile.getName()));
            throw new BroadwickException(String.format(errorMsg, populationFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        }
        return readSoFar;
    }

    /**
     * Create a database entry for a line read from a data file.
     * @param line a list of tokens read from a populations data file.
     * @throws ParseException if a date element cannot be parsed.
     */
    private void createDatabaseEntry(final List<String> line) throws ParseException {
        // This is the code that actually add the data to the database, if the underlying database changes then
        // this is the only method that need to be changed.

        final String nodeId = line.get(populationFile.getLifeHistory().getIdColumn() - 1);
        final Node node;
        final Transaction tx = dataDb.getInternalDb().beginTx();
        try {
            node = dataDb.getNodeById(nodeId);
            node.setProperty(MovementDatabaseFacade.TYPE, MovementDatabaseFacade.ANIMAL);

            for (Table.Cell<String, Integer, String> cell : populationDescr.cellSet()) {
                final String property = cell.getRowKey();
                final String dataType = cell.getValue();
                final String value = line.get(cell.getColumnKey() - 1);
                if (value != null && !value.isEmpty()) {
                    switch (dataType) {
                        case DOUBLE:
                            node.setProperty(property, Double.parseDouble(value));
                            break;
                        case INT:
                            node.setProperty(property, Integer.parseInt(value));
                            break;
                        case DATE:
                            final DateTime date = DateTime.parse(value, this.dateFormat);
                            node.setProperty(property, Days.daysBetween(dataDb.getZeroDate(), date).getDays());
                            break;
                        default:
                            // add a string so we can catch VARCHAR here by default
                            node.setProperty(property, value);
                    }
                }
            }
            tx.success();
        } catch (org.neo4j.graphdb.NotFoundException e) {
            final String errorMsg = "Could not find node %s";
            log.trace(String.format(errorMsg, nodeId));
            throw new BroadwickException(String.format(errorMsg, nodeId) + NEWLINE + Throwables.getStackTraceAsString(e));
        } finally {
            tx.finish();
        }
    }

    private MovementDatabaseFacade dataDb;
    private DataReader dataReader;
    private DataFiles.PopulationFile populationFile;
    private Table<String, Integer, String> populationDescr = HashBasedTable.create();
    private final DateTimeFormatter dateFormat;
    private static final String VARCHAR = "VARCHAR";
    private static final String DOUBLE = "DOUBLE";
    private static final String DATE = "DATE";
    private static final String INT = "INT";
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
