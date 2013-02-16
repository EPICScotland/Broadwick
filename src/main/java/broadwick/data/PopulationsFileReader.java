package broadwick.data;

import broadwick.BroadwickException;
import broadwick.config.generated.CustomTags;
import broadwick.config.generated.DataFiles;
import com.google.common.base.Throwables;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Read a file containing the population data.
 */
@Slf4j
public class PopulationsFileReader extends DataFileReader {

    /**
     * Create the populations file reader.
     * @param populationFile the [xml] tag of the config file that is to be read.
     * @param dbImpl         the implementation of the database object.
     */
    public PopulationsFileReader(final DataFiles.PopulationFile populationFile,
                                 final DatabaseImpl dbImpl) {

        super();
        this.database = dbImpl;
        this.dataFile = populationFile.getName();
        this.dateFields = new HashSet<>();
        this.dateFormat = populationFile.getDateFormat();
        this.insertedColInfo = new TreeMap<>();
        this.populationFile = populationFile;

        if (populationFile.getLifeHistory() != null) {
            readLifeHistory();
        } else if (populationFile.getPopulation() != null) {
            readPopulation();
        } else {
            throw new BroadwickException("Cannot read Populations section - No lifeHistory or population section found.");
        }
    }

    /**
     * Read the life history declarations of a population file section in the configuration file, if there are errors,
     * they are noted and and exception is thrown with ALL the errors at the end of the method.
     */
    private void readLifeHistory() {
        tableName = LIFE_HISTORIES_TABLE_NAME;
        final StringBuilder errors = new StringBuilder();
        this.createTableCommand = new StringBuilder();
        updateCreateTableCommand(ID, populationFile.getLifeHistory().getIdColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, LIFE_HISTORIES_TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(DATE_OF_BIRTH, populationFile.getLifeHistory().getDateOfBirthColumn(),
                                 " VARCHAR(128), ", insertedColInfo, createTableCommand, LIFE_HISTORIES_TABLE_NAME,
                                 SECTION_NAME, errors);
        updateCreateTableCommand(LOCATION_OF_BIRTH, populationFile.getLifeHistory().getLocationOfBirthColumn(),
                                 " VARCHAR(128), ", insertedColInfo, createTableCommand, LIFE_HISTORIES_TABLE_NAME,
                                 SECTION_NAME, errors);
        updateCreateTableCommand(DATE_OF_DEATH, populationFile.getLifeHistory().getDateOfDeathColumn(), " INT, ",
                                 insertedColInfo, createTableCommand, LIFE_HISTORIES_TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(LOCATION_OF_DEATH, populationFile.getLifeHistory().getLocationOfDeathColumn(),
                                 " VARCHAR(128), ", insertedColInfo, createTableCommand, LIFE_HISTORIES_TABLE_NAME,
                                 SECTION_NAME, errors);
        updateCreateTableCommand(SPECIES, populationFile.getLifeHistory().getSpeciesColumn(),
                                 " VARCHAR(32), ", insertedColInfo, createTableCommand, LIFE_HISTORIES_TABLE_NAME,
                                 SECTION_NAME, errors);

        if (populationFile.getCustomTags() != null) {
            for (CustomTags.CustomTag tag : populationFile.getCustomTags().getCustomTag()) {
                updateCreateTableCommand(tag.getName(), tag.getColumn(), " VARCHAR(128), ",
                                         insertedColInfo, createTableCommand, LIFE_HISTORIES_TABLE_NAME, SECTION_NAME, errors);
                if ("date".equals(tag.getType())) {
                    dateFields.add(tag.getColumn());
                }
            }
        }

        if (populationFile.getLifeHistory().getDateOfBirthColumn() > 0) {
            dateFields.add(populationFile.getLifeHistory().getDateOfBirthColumn());
        }

        if (populationFile.getLifeHistory().getDateOfDeathColumn() > 0) {
            dateFields.add(populationFile.getLifeHistory().getDateOfDeathColumn());
        }

        final StringBuilder createIndexCommand = new StringBuilder();
        createTableCommand.deleteCharAt(createTableCommand.length() - 1);
        createTableCommand.append(");");

        createIndexCommand.append(String.format(" CREATE INDEX IF NOT EXISTS IDX_POP_ID ON %s (%s);",
                                                LIFE_HISTORIES_TABLE_NAME, ID));
        createIndexCommand.append(String.format(" CREATE INDEX IF NOT EXISTS IDX_POP_DATE ON %s (%s,%s,%s);",
                                                LIFE_HISTORIES_TABLE_NAME, ID, DATE_OF_BIRTH, DATE_OF_DEATH));

        createTableCommand.append(createIndexCommand.toString());

        insertString = String.format("INSERT INTO %s (%s) VALUES (%s)",
                                     LIFE_HISTORIES_TABLE_NAME, asCsv(insertedColInfo.keySet()), asQuestionCsv(insertedColInfo.keySet()));

        if (errors.length() > 0) {
            log.error(errors.toString());
            throw new BroadwickException(errors.toString());
        }
    }

    /**
     * Read the populations declarations of a population file section in the configuration file, if there are errors,
     * they are noted and and exception is thrown with ALL the errors at the end of the method.
     */
    private void readPopulation() {
        tableName = POPULATIONS_TABLE_NAME;
        final StringBuilder errors = new StringBuilder();
        this.createTableCommand = new StringBuilder();
        updateCreateTableCommand(LOCATION, populationFile.getPopulation().getLocationIdColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, POPULATIONS_TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(POPULATION, populationFile.getPopulation().getPopulationSizeColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, POPULATIONS_TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(DATE_LC, populationFile.getPopulation().getPopulationDateColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, POPULATIONS_TABLE_NAME, SECTION_NAME, errors);


        dateFields.add(populationFile.getPopulation().getPopulationDateColumn());

        if (populationFile.getPopulation().getSpeciesColumn() != null) {
            if (populationFile.getPopulation().getSpeciesColumn() != 0) {
                updateCreateTableCommand(SPECIES, populationFile.getPopulation().getSpeciesColumn(),
                                         " VARCHAR(32), ", insertedColInfo, createTableCommand, POPULATIONS_TABLE_NAME,
                                         SECTION_NAME, errors);
            } else {
                errors.append("No ").append(SPECIES).append(" column set in ").append(SECTION_NAME).append(" section\n");
            }
        }

        if (populationFile.getCustomTags() != null) {
            for (CustomTags.CustomTag tag : populationFile.getCustomTags().getCustomTag()) {
                if (populationFile.getPopulation().getSpeciesColumn() != 0) {
                    updateCreateTableCommand(tag.getName(), tag.getColumn(), " VARCHAR(128), ",
                                             insertedColInfo, createTableCommand, POPULATIONS_TABLE_NAME, SECTION_NAME, errors);
                } else {
                    errors.append("No ").append(tag.getName()).append(" column set in ").append(SECTION_NAME).append(" section\n");
                }
            }
        }

        if (populationFile.getPopulation().getPopulationDateColumn() > 0) {
            dateFields.add(populationFile.getPopulation().getPopulationDateColumn());
        }

        final StringBuilder createIndexCommand = new StringBuilder();
        createTableCommand.deleteCharAt(createTableCommand.length() - 1);
        createTableCommand.append(");");

        createIndexCommand.append(String.format(" CREATE INDEX IF NOT EXISTS IDX_POP_LOCATION ON %s (%s);",
                                                POPULATIONS_TABLE_NAME, LOCATION));

        createTableCommand.append(createIndexCommand.toString());

        insertString = String.format("INSERT INTO %s (%s) VALUES (%s)",
                                     POPULATIONS_TABLE_NAME, asCsv(insertedColInfo.keySet()), asQuestionCsv(insertedColInfo.keySet()));

        if (errors.length() > 0) {
            log.error(errors.toString());
            throw new BroadwickException(errors.toString());
        }
    }

    @Override
    public final int insert() {
        log.trace("PopulationFileReader insert");

        int inserted = 0;
        try (Connection connection = database.getConnection()) {
            createTable(tableName, createTableCommand.toString(), connection);

            inserted = insert(connection, tableName, insertString, dataFile, dateFormat, insertedColInfo, dateFields);
        } catch (Exception ex) {
            log.error("Error reading population data {}. {}", ex.getLocalizedMessage(), Throwables.getStackTraceAsString(ex));
            throw new BroadwickException(ex);
        }
        return inserted;
    }

    private DatabaseImpl database;
    private String dataFile;
    private String dateFormat;
    private StringBuilder createTableCommand;
    private String insertString;
    private Map<String, Integer> insertedColInfo;
    private Collection<Integer> dateFields;
    private DataFiles.PopulationFile populationFile;
    @Getter
    private static String tableName;
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
    private static final String SECTION_NAME = "PopulationsFile";
    @Getter
    private static final String POPULATIONS_TABLE_NAME = "Populations";
    @Getter
    private static final String LIFE_HISTORIES_TABLE_NAME = "LifeHistories";
}
