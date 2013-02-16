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
 * Reader for the files describing the batched movements (i.e. those that do not specify an individual but rather the
 * number of individuals moved)for the simulation. The movements are read and stored in internal databases for later
 * use.
 */
@Slf4j
public class BatchedMovementsFileReader extends DataFileReader {

    /**
     * Create the movement file reader.
     * @param movementFile the [xml] tag of the config file that is to be read.
     * @param dbImpl    the implementation of the database object.
     */
    public BatchedMovementsFileReader(final DataFiles.BatchMovementFile movementFile,
                                      final DatabaseImpl dbImpl) {
        super();
        this.database = dbImpl;
        this.dataFile = movementFile.getName();
        this.dateFields = new HashSet<>();
        this.dateFormat = movementFile.getDateFormat();
        this.insertedColInfo = new TreeMap<>();
        final StringBuilder errors = new StringBuilder();

        this.createTableCommand = new StringBuilder();
        updateCreateTableCommand(BATCH_SIZE, movementFile.getBatchSizeColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(DEPARTURE_DATE, movementFile.getDepartureDateColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(DEPARTURE_ID, movementFile.getDepartureLocationIdColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(DESTINATION_DATE, movementFile.getDestinationDateColumn(), " INT, ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(DESTINATION_ID, movementFile.getDestinationLocationIdColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(MARKET_ID, movementFile.getMarketIdColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(MARKET_DATE, movementFile.getMarketDateColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(SPECIES, movementFile.getSpeciesColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);

        if (movementFile.getCustomTags() != null) {
            for (CustomTags.CustomTag tag : movementFile.getCustomTags().getCustomTag()) {
                updateCreateTableCommand(tag.getName(), tag.getColumn(), " VARCHAR(128), ",
                                         insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
                if ("date".equals(tag.getType())) {
                    dateFields.add(tag.getColumn());
                }
            }
        }

        createTableCommand.deleteCharAt(createTableCommand.length() - 1);
        createTableCommand.append(");");

        if (movementFile.getDepartureDateColumn() > 0) {
            dateFields.add(movementFile.getDepartureDateColumn());
        }
        if (movementFile.getDestinationDateColumn() > 0) {
            dateFields.add(movementFile.getDestinationDateColumn());
        }
        if (movementFile.getMarketDateColumn() > 0) {
            dateFields.add(movementFile.getMarketDateColumn());
        }

        final StringBuilder createIndexCommand = new StringBuilder();
        createIndexCommand.append(String.format(" CREATE INDEX IF NOT EXISTS IDX_BATCH_ID ON %s (%s,%s,%s,%s,%s);",
                                                TABLE_NAME, DEPARTURE_ID, DEPARTURE_DATE, DESTINATION_ID, DESTINATION_DATE, BATCH_SIZE));

        createTableCommand.append(createIndexCommand.toString());

        insertString = String.format("INSERT INTO %s (%s) VALUES (%s)",
                                     TABLE_NAME,
                                     asCsv(insertedColInfo.keySet()), asQuestionCsv(insertedColInfo.keySet()));

        if (errors.length() > 0) {
            log.error(errors.toString());
            throw new BroadwickException(errors.toString());
        }
    }

    @Override
    public final int insert() {
        log.trace("BatchedMovementsFileReader insert");

        int inserted = 0;
         try (Connection connection = database.getConnection()) {  
         createTable(TABLE_NAME, createTableCommand.toString(), connection);

         inserted = insert(connection, TABLE_NAME, insertString, dataFile, dateFormat,  insertedColInfo, dateFields);
         } catch (Exception ex) {
            log.error("{}", ex.getLocalizedMessage());
            log.error("Error reading movement data. {}", Throwables.getStackTraceAsString(ex));
            throw new BroadwickException(ex);
        }
        return inserted;
    }

    private DatabaseImpl database;
    private String dataFile;
    private String dateFormat;
    @Getter
    private static final String TABLE_NAME = "BatchedMovements";
    private StringBuilder createTableCommand;
    private String insertString;
    private Map<String, Integer> insertedColInfo;
    private Collection<Integer> dateFields;
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
    private static final String SPECIES = "Species";
    private static final String SECTION_NAME = "BatchedMovementFile";
}
