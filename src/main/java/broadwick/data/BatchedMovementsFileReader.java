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
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

/**
 * Reader for the files describing the batched movements (i.e. those that do not specify an individual but rather the
 * number of individuals moved)for the simulation. The movements are read and stored in internal databases for later
 * use.
 */
@Slf4j
public class BatchedMovementsFileReader implements Callable<Integer> {

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
        dateFormat = DateTimeFormat.forPattern(movementFile.getDateFormat());

        try {
            final StringBuilder errors = new StringBuilder();
            dataReader.addElement(BATCH_SIZE, movementFile.getBatchSizeColumn(), VARCHAR, batchMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(DEPARTURE_DATE, movementFile.getDepartureDateColumn(), DATE, batchMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(DEPARTURE_ID, movementFile.getDepartureLocationIdColumn(), VARCHAR, batchMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(DESTINATION_DATE, movementFile.getDestinationDateColumn(), DATE, batchMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(DESTINATION_ID, movementFile.getDestinationLocationIdColumn(), VARCHAR, batchMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(MARKET_ID, movementFile.getMarketIdColumn(), VARCHAR, batchMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(MARKET_DATE, movementFile.getMarketDateColumn(), DATE, batchMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(SPECIES, movementFile.getSpeciesColumn(), VARCHAR, batchMvmtDescr, errors, true, SECTION_NAME);

            if (movementFile.getCustomTags() != null) {
                for (CustomTags.CustomTag tag : movementFile.getCustomTags().getCustomTag()) {
                    dataReader.addElement(tag.getName(), tag.getColumn(), tag.getType(), batchMvmtDescr, errors, true, SECTION_NAME);
                }
            }
        } catch (BroadwickException e) {
            log.error("Cannot read Movements section correctly.\n{}", e.getLocalizedMessage());
        }
    }

    @Override
    public final Integer call() {
        int readSoFar = 0;

        // for each line of the batched movement section add the movement to the database.
        try (FileInput fle = new FileInput(movementFile.getName(), movementFile.getSeparator())) {
            List<String> line;
            //CHECKSTYLE:OFF
            while (!(line = fle.readLine()).isEmpty()) {
                //CHECKSTYLE:ON

                createDatabaseEntry(line);
                readSoFar++;
            }
        } catch (IndexOutOfBoundsException | NumberFormatException | NoSuchElementException | ParseException | BroadwickException e) {
            final String errorMsg = "Adding to reading list for %s";
            log.trace(String.format(errorMsg, movementFile.getName()));
            throw new BroadwickException(String.format(errorMsg, movementFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            final String errorMsg = "Could not open file %s";
            log.trace(String.format(errorMsg, movementFile.getName()));
            throw new BroadwickException(String.format(errorMsg, movementFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        }
        return readSoFar;
    }

    /**
     * Create a database entry for a line read from a data file.
     * @param line a list of tokens read from a movements data file.
     * @throws ParseException if a date element cannot be parsed.
     */
    private void createDatabaseEntry(final List<String> line) throws ParseException {
        // This is the code that actually add the data to the database, if the underlying database changes then
        // this is the only method that need to be changed.

        final Transaction tx = dataDb.getInternalDb().beginTx();
        try {
            final Node departureNode = dataDb.getNodeById(line.get(movementFile.getDepartureLocationIdColumn()));
            final Node destinationNode = dataDb.getNodeById(line.get(movementFile.getDestinationLocationIdColumn()));

            // TODO check if these are null - if so then either the thread uploading the locations hasn't uploaded it yet
            // or there is no location in the data for this 
            // either way create the nodes and index them....
            final Relationship relationship = departureNode.createRelationshipTo(destinationNode,
                                                                                 MovementDatabaseFacade.MovementRelationship.MOVES);
            for (Table.Cell<String, Integer, String> cell : batchMvmtDescr.cellSet()) {
                final String property = cell.getRowKey();
                final String dataType = cell.getValue();
                final String value = line.get(cell.getColumnKey() - 1);

                if (value != null && !value.isEmpty()) {
                    switch (dataType) {
                        case DOUBLE:
                            relationship.setProperty(property, Double.parseDouble(value));
                            break;
                        case DATE:
                            final DateTime date = DateTime.parse(value, this.dateFormat);
                            relationship.setProperty(property, Days.daysBetween(dataDb.getZeroDate(), date).getDays());
                            break;
                        case INT:
                            relationship.setProperty(property, Integer.parseInt(value));
                            break;
                        default:
                            // add a string so we can catch VARCHAR here by default
                            relationship.setProperty(property, value);
                    }
                }
            }
            tx.success();

        } finally {
            tx.finish();
        }
    }

    private MovementDatabaseFacade dataDb;
    private DataFiles.BatchMovementFile movementFile;
    private Table<String, Integer, String> batchMvmtDescr = HashBasedTable.create();
    private final DateTimeFormatter dateFormat;
    private static final String VARCHAR = "VARCHAR";
    private static final String DOUBLE = "DOUBLE";
    private static final String INT = "INT";
    private static final String DATE = "DATE";
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
