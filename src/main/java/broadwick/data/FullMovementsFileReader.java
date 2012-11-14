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
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

/**
 * Reader for the files describing the full movements (i.e. those that specify both the departure location and the
 * destination location of a movement)for the simulation. The movements are read and stored in internal databases for
 * later use.
 */
@Slf4j
public class FullMovementsFileReader implements Callable<Integer> {

    /**
     * Create the movement file reader.
     * @param movementFile the [xml] tag of the config file that is to be read.
     * @param dataReader   the reader object that contains some useful functionality.
     * @param dataDb       the database facade object used to save the data in the movements file section.
     */
    public FullMovementsFileReader(final DataFiles.FullMovementFile movementFile,
                                   final DataReader dataReader, final MovementDatabaseFacade dataDb) {
        this.movementFile = movementFile;
        this.dataDb = dataDb;
        dateFormat = DateTimeFormat.forPattern(movementFile.getDateFormat());

        try {
            final StringBuilder errors = new StringBuilder();

            dataReader.addElement(ID, movementFile.getIdColumn(), VARCHAR, fullMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(DEPARTURE_DATE, movementFile.getDepartureDateColumn(), DATE, fullMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(DEPARTURE_ID, movementFile.getDepartureLocationIdColumn(), VARCHAR, fullMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(DESTINATION_DATE, movementFile.getDestinationDateColumn(), DATE, fullMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(DESTINATION_ID, movementFile.getDestinationLocationIdColumn(), VARCHAR, fullMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(SPECIES, movementFile.getSpeciesColumn(), VARCHAR, fullMvmtDescr, errors, true, SECTION_NAME);

            if (movementFile.getMarketIdColumn() != null) {
                dataReader.addElement(MARKET_ID, movementFile.getMarketIdColumn(), VARCHAR, fullMvmtDescr, errors, true, SECTION_NAME);
            }
            if (movementFile.getMarketDateColumn() != null) {
                dataReader.addElement(MARKET_DATE, movementFile.getMarketDateColumn(), DATE, fullMvmtDescr, errors, true, SECTION_NAME);
            }

            if (movementFile.getCustomTags() != null) {
                for (CustomTags.CustomTag tag : movementFile.getCustomTags().getCustomTag()) {
                    dataReader.addElement(tag.getName(), tag.getColumn(), tag.getType(), fullMvmtDescr, errors, true, SECTION_NAME);
                }
            }

            if (errors.length() > 0) {
                throw new BroadwickException(errors.toString());
            }
        } catch (BroadwickException e) {
            log.error("Cannot read Movements section correctly.\n{}", e.getLocalizedMessage());
        }
    }

    @Override
    public final Integer call() {
        int readSoFar = 0;

        // for each line of the full movement section add the movement to the database.
        try (FileInput fle = new FileInput(movementFile.getName(), movementFile.getSeparator())) {
            List<String> line;
            while (!(line = fle.readLine()).isEmpty()) {
                createDatabaseEntry(line);
                readSoFar++;
            }
        } catch (IndexOutOfBoundsException | ParseException | NoSuchElementException | NumberFormatException | BroadwickException e) {
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
            for (Table.Cell<String, Integer, String> cell : fullMvmtDescr.cellSet()) {
                final String property = cell.getRowKey();
                final String dataType = cell.getValue();
                final String value = line.get(cell.getColumnKey() - 1);

                if (value != null && !value.isEmpty()) {
                    switch (dataType) {
                        case DATE:
                            final DateTime date = DateTime.parse(value, this.dateFormat);
                            relationship.setProperty(property, Days.daysBetween(dataDb.getZeroDate(), date).getDays());
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
    private DataFiles.FullMovementFile movementFile;
    private Table<String, Integer, String> fullMvmtDescr = HashBasedTable.create();
    private final DateTimeFormatter dateFormat;
    private static final String VARCHAR = "VARCHAR";
    private static final String DATE = "DATE";
    private static final String ID = "Id";
    private static final String DEPARTURE_DATE = "DepartureDate";
    private static final String DEPARTURE_ID = "DepartureId";
    private static final String DESTINATION_DATE = "DestinationDate";
    private static final String DESTINATION_ID = "DestinationId";
    private static final String MARKET_ID = "MarketId";
    private static final String MARKET_DATE = "MarketDate";
    private static final String SPECIES = "Sprcies";
    private static final String NEWLINE = "\n";
    private static final String SECTION_NAME = "FullMovementsFile";
}
