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
 * Reader for the files describing the directed movements (i.e. those that only have a location and a movement_ON or
 * movement_OFF flag)for the simulation. The movements are read and stored in internal databases for later use.
 */
@Slf4j
public class DirectedMovementsFileReader implements Callable<Integer> {

    /**
     * Create the movement file reader.
     * @param movementFile the [xml] tag of the config file that is to be read.
     * @param dataReader   the reader object that contains some useful functionality.
     * @param dataDb       the database facade object used to save the data in the movements file section.
     */
    public DirectedMovementsFileReader(final DataFiles.DirectedMovementFile movementFile,
                                       final DataReader dataReader, final MovementDatabaseFacade dataDb) {
        this.movementFile = movementFile;
        this.dataDb = dataDb;
        dateFormat = DateTimeFormat.forPattern(movementFile.getDateFormat());

        try {
            final StringBuilder errors = new StringBuilder();
            dataReader.addElement(ID, movementFile.getIdColumn(), VARCHAR, directedMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(MOVEMENT_DATE, movementFile.getMovementDateColumn(), DATE, directedMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(MOVEMENT_DIRECTION, movementFile.getMovementDirectionColumn(), VARCHAR, directedMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(LOCATION_ID, movementFile.getLocationColumn(), VARCHAR, directedMvmtDescr, errors, true, SECTION_NAME);
            dataReader.addElement(SPECIES, movementFile.getSpeciesColumn(), VARCHAR, directedMvmtDescr, errors, true, SECTION_NAME);

            if (movementFile.getCustomTags() != null) {
                for (CustomTags.CustomTag tag : movementFile.getCustomTags().getCustomTag()) {
                    dataReader.addElement(tag.getName(), tag.getColumn(), tag.getType(), directedMvmtDescr, errors, true, SECTION_NAME);
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

        try (FileInput fle = new FileInput(movementFile.getName(), movementFile.getSeparator())) {
            List<String> line;
            while (!(line = fle.readLine()).isEmpty()) {
                createDatabaseEntry(line);
                readSoFar++;
            }
        } catch (IndexOutOfBoundsException | NoSuchElementException | ParseException | NumberFormatException | BroadwickException e) {
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
     * @param line a list of tokens read from a movement data file.
     * @throws ParseException if a date element cannot be parsed.
     */
    private void createDatabaseEntry(final List<String> line) throws ParseException {

        final Transaction tx = dataDb.getInternalDb().beginTx();
        try {

            final Node animalNode = dataDb.getNodeById(line.get(movementFile.getIdColumn()));
            final Node locationNode = dataDb.getNodeById(line.get(movementFile.getLocationColumn()));
            final Relationship relationship = animalNode.createRelationshipTo(locationNode,
                                                                              MovementDatabaseFacade.MovementRelationship.MOVES);

            for (Table.Cell<String, Integer, String> cell : directedMvmtDescr.cellSet()) {
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
    private DataFiles.DirectedMovementFile movementFile;
    private Table<String, Integer, String> directedMvmtDescr = HashBasedTable.create();
    private final DateTimeFormatter dateFormat;
    private static final String VARCHAR = "VARCHAR";
    private static final String DATE = "DATE";
    private static final String ID = "Id";
    private static final String SPECIES = "Sprcies";
    private static final String LOCATION_ID = "LocationId";
    private static final String MOVEMENT_DATE = "MovementDate";
    private static final String MOVEMENT_DIRECTION = "MovementDirection";
    private static final String NEWLINE = "\n";
    private static final String SECTION_NAME = "DirectedMovementFile";
}
