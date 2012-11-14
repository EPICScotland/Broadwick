package broadwick.data;

import broadwick.BroadwickException;
import broadwick.config.generated.CustomTags;
import broadwick.config.generated.DataFiles;
import broadwick.io.FileInput;
import com.google.common.base.Throwables;
import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import java.io.IOException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.Callable;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

/**
 * Reader for the files describing the locations for the simulation.
 */
@Slf4j
public class LocationsFileReader implements Callable<Integer> {

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

        dataReader.addElement(ID, locationsFile.getLocationIdColumn(), VARCHAR, locationsDescr, errors, true, SECTION_NAME);
        dataReader.addElement(EASTING, locationsFile.getEastingColumn(), DOUBLE, locationsDescr, errors, true, SECTION_NAME);
        dataReader.addElement(NORTHING, locationsFile.getNorthingColumn(), DOUBLE, locationsDescr, errors, true, SECTION_NAME);

        if (locationsFile.getCustomTags() != null) {
            for (CustomTags.CustomTag tag : locationsFile.getCustomTags().getCustomTag()) {
                dataReader.addElement(tag.getName(), tag.getColumn(), tag.getType(), locationsDescr, errors, true, SECTION_NAME);
            }
        }


        if (errors.length() > 0) {
            throw new BroadwickException(errors.toString());
        }
    }

    @Override
    public final Integer call() {
        int readSoFar = 0;

        try (FileInput fle = new FileInput(locationsFile.getName(), locationsFile.getSeparator())) {
            List<String> line;
            while (!(line = fle.readLine()).isEmpty()) {
                createDatabaseNode(line);
                readSoFar++;
            }

        } catch (IndexOutOfBoundsException | NoSuchElementException | NumberFormatException | BroadwickException e) {
            final String errorMsg = "Adding to reading list for %s";
            log.trace(String.format(errorMsg, locationsFile.getName()));
            throw new BroadwickException(String.format(errorMsg, locationsFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        } catch (IOException e) {
            final String errorMsg = "Could not open file %s";
            log.trace(String.format(errorMsg, locationsFile.getName()));
            throw new BroadwickException(String.format(errorMsg, locationsFile.getName()) + NEWLINE + Throwables.getStackTraceAsString(e));
        }
        return readSoFar;
    }

    /**
     * Create a database entry for a line read from a data file.
     * @param line a list of tokens read from a locations data file.
     */
    private void createDatabaseNode(final List<String> line) {
        // This is the code that actually add the data to the database, if the underlying database changes then
        // this is the only method that need to be changed.
        final String nodeId = line.get(locationsFile.getLocationIdColumn() - 1);
        final Node node;
        final Transaction tx = dataDb.getInternalDb().beginTx();
        try {
            node = dataDb.getNodeById(nodeId);

            for (Table.Cell<String, Integer, String> cell : locationsDescr.cellSet()) {
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
    private DataFiles.LocationsFile locationsFile;
    private Table<String, Integer, String> locationsDescr = HashBasedTable.create();
    private static final String VARCHAR = "VARCHAR";
    private static final String DOUBLE = "DOUBLE";
    //private static final String DATE = "DATE";
    private static final String INT = "INT";
    private static final String ID = "Id";
    private static final String EASTING = "Easting";
    private static final String NORTHING = "Northing";
    private static final String NEWLINE = "\n";
    private static final String SECTION_NAME = "LocationsFile";
}
