package broadwick.data;

import broadwick.BroadwickException;
import broadwick.config.generated.DataFiles;
import broadwick.config.generated.DataFiles.BatchMovementFile;
import broadwick.config.generated.DataFiles.DirectedMovementFile;
import broadwick.config.generated.DataFiles.FullMovementFile;
import broadwick.config.generated.DataFiles.LocationsFile;
import broadwick.config.generated.DataFiles.PopulationFile;
import broadwick.config.generated.DataFiles.TestsFile;
import broadwick.config.generated.Project;
import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * Utility class to read, store and manipulate the data files.
 */
@Slf4j
public class DataReader implements java.lang.AutoCloseable {

    /**
     * Create the object that will read the data element of the configuration file.
     * @param data the <data/> element of the configuration file.
     */
    public DataReader(final Project.Data data) {
        this.data = data;
        drDatabase = new MovementDatabaseFacade();

        // if there is a data section in the config file let's read it.
        if (data != null) {
            if (data.getDatabases() != null) {
                // there is a database mentioned in the config file so let's use it
                dbName = data.getDatabases().getName();
                lookupDbName = data.getDatabases().getLookupDatabase();
                drDatabase.openDatabase(dbName, lookupDbName);
                logDbStatistics();
            } else if (data.getDatafiles() != null) {
                // there is a datafiles section in the config file so we will read them and create a randomly named 
                // database.
                final String db = "broadwick_" + RandomStringUtils.randomAlphanumeric(8);
                dbName = db + "_db";
                lookupDbName = db + "_lookup_db";
                drDatabase.openDatabaseForInserting(dbName, lookupDbName);
                drDatabase.createLookupTables();
                readDataSection();
                drDatabase.openDatabase(dbName, lookupDbName);
            } else {
                throw new BroadwickException("There is a <data> section in the configuration file but neither a <database> nor <datafiles> section.");
            }
        }
        lookup = new Lookup(drDatabase);
    }

    /**
     * Read the data section from the configuration file. If a database section is found then it is used and any files
     * specified are ignored.
     */
    public final void readDataSection() {
        final StopWatch sw = new StopWatch();
        final DataFiles files = data.getDatafiles();
        if (files != null) {
            sw.start();
            readDataFiles(files);
            sw.stop();
            log.info("Processed input data in {} ms", sw.toString());
            log.info("Data stored internally in {}.", dbName);
        }
    }

    @Override
    public final void close() {
        drDatabase.closeDb();
    }

    /**
     * Save a message giving the numbers of elements in the database.
     */
    private void logDbStatistics() {

        if (drDatabase.getDbService() != null) {
            final GlobalGraphOperations ops = GlobalGraphOperations.at(drDatabase.getDbService());
            final Iterable<Node> allNodes = ops.getAllNodes();

            final Iterable<Node> animals = Iterables.filter(allNodes, Lookup.ANIMAL_PREDICATE);
            log.info("Read {} population data from the database.", Iterables.size(animals));

            final Iterable<Node> locations = Iterables.filter(allNodes, Lookup.LOCATION_PREDICATE);
            log.info("Read {} locations data from the database.", Iterables.size(locations));

            final Iterable<Node> tests = Iterables.filter(allNodes, Lookup.TEST_PREDICATE);
            log.info("Read {} tests data from the database.", Iterables.size(tests));

            final Iterable<Relationship> allRelationships = ops.getAllRelationships();
            final Iterable<Relationship> movements = Iterables.filter(allRelationships, Lookup.MOVEMENT_PREDICATE);
            log.info("Read {} movements data from the database.", Iterables.size(movements));
        }
    }

    /**
     * Read the datafiles, each section type of the <datafiles/> element (e.g. all the <MovementFile/>s) are read in
     * parallel and each individual element is its read in parallel with the data saved to an internal database. Once
     * the datafiles have been read some post-processing is run to determine the locations of individuals at each time
     * step throughout the simulation.
     * @param files the Datafiles object from the configuration file.
     */
    private void readDataFiles(final DataFiles files) {

        try {
            final String addingFileMsg = "Reading %s ...";

            readAllLocationSections(files.getLocationsFile(), addingFileMsg);
            drDatabase.getIndex().flush();

            readAllPopulationSections(files.getPopulationFile(), addingFileMsg);
            drDatabase.getIndex().flush();

            readAllFullMovementSections(files.getFullMovementFile(), addingFileMsg);
            drDatabase.getIndex().flush();

            readAllDirectedMovementSections(files.getDirectedMovementFile(), addingFileMsg);
            drDatabase.getIndex().flush();

            readAllBatchedMovementSections(files.getBatchMovementFile(), addingFileMsg);
            drDatabase.getIndex().flush();

            readAllTestSections(files.getTestsFile(), addingFileMsg);
            drDatabase.getIndex().flush();

        } catch (Exception e) {
            log.error("Failure reading data file section. {}", e.getLocalizedMessage());
            throw new BroadwickException(Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * Add a description of a configuration file section to an internal key value pair so that the correct column of a
     * data file can be read. Each <datafile> section in the configuration contains an element and the column location
     * in the data file where this element can be found. This method update a key-value pair of the element name and
     * location, recording errors as it finds them.
     * @param elementName     the name of the element in the configuration file.
     * @param indexInDataFile the location of the element in each row of the data file.
     * @param keyValueMapping a table of the keyValueMapping name index and type.
     * @param errors          a description of any parsing errors found.
     * @param recordErrors    flag to control whether or not error are recorded in the errors parameter.
     * @param sectionName     the name of the section from which the element being added to the table has been read.
     */
    protected final void updateSectionDefiniton(final String elementName, final int indexInDataFile,
                                                final Map<String, Integer> keyValueMapping, final StringBuilder errors,
                                                final boolean recordErrors, final String sectionName) {
        if (indexInDataFile != 0) {
            keyValueMapping.put(elementName, indexInDataFile);
        } else {
            if (recordErrors) {
                errors.append("No ").append(elementName).append(" column set in ").append(sectionName).append(" section\n");
            }
        }
    }

    /**
     * Read the directed movements sections of the configuration file.
     * @param directedMovementFiles a collection of directedMovement sections.
     * @param addingFileMsg         formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return the number of movements read.
     */
    private int readAllDirectedMovementSections(final List<DirectedMovementFile> directedMovementFiles, final String addingFileMsg) {
        final DataReader reader = this;

        int elementsRead = 0;

        for (DataFiles.DirectedMovementFile file : directedMovementFiles) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final DirectedMovementsFileReader movementsFileReader = new DirectedMovementsFileReader(file, reader, drDatabase);
            elementsRead += movementsFileReader.insert();
        }

        if (elementsRead > 0) {
            log.info("Read {} directed movements from {} files.", elementsRead, directedMovementFiles.size());
        }
        return elementsRead;
    }

    /**
     * Read the full movements sections of the configuration file.
     * @param fullMovementFiles a collection of fullMovement sections.
     * @param addingFileMsg     formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return the number of movements read.
     */
    private int readAllFullMovementSections(final List<FullMovementFile> fullMovementFiles, final String addingFileMsg) {
        final DataReader reader = this;

        int elementsRead = 0;

        for (DataFiles.FullMovementFile file : fullMovementFiles) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final FullMovementsFileReader movementsFileReader = new FullMovementsFileReader(file, reader, drDatabase);
            elementsRead += movementsFileReader.insert();
        }

        if (elementsRead > 0) {
            log.info("Read {} full movementss from {} files.", elementsRead, fullMovementFiles.size());
        }
        return elementsRead;
    }

    /**
     * Read the batched movements sections of the configuration file.
     * @param batchMovementFiles a collection of batchMovement sections.
     * @param addingFileMsg      formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return the number of movements read.
     */
    private int readAllBatchedMovementSections(final List<BatchMovementFile> batchMovementFiles, final String addingFileMsg) {
        final DataReader reader = this;

        int elementsRead = 0;

        for (DataFiles.BatchMovementFile file : batchMovementFiles) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final BatchedMovementsFileReader movementsFileReader = new BatchedMovementsFileReader(file, reader, drDatabase);
            elementsRead += movementsFileReader.insert();
        }

        if (elementsRead > 0) {
            log.info("Read {} batched ovements from {} files.", elementsRead, batchMovementFiles.size());
        }
        return elementsRead;
    }

    /**
     * Read the location sections of the configuration file.
     * @param locationsFile a collection of locations sections.
     * @param addingFileMsg formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return the number of locations read.
     */
    private int readAllLocationSections(final List<LocationsFile> locationsFile, final String addingFileMsg) {
        final DataReader reader = this;

        int elementsRead = 0;

        for (DataFiles.LocationsFile file : locationsFile) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final LocationsFileReader locationsFileReader = new LocationsFileReader(file, reader, drDatabase);
            elementsRead += locationsFileReader.insert();
        }

        if (elementsRead > 0) {
            log.info("Read {} locations from {} files.", elementsRead, locationsFile.size());
        }
        return elementsRead;
    }

    /**
     * Read the test sections of the configuration file.
     * @param testsFile a collection of locations sections.
     * @param addingFileMsg formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return the number of tests read.
     */
    private int readAllTestSections(final List<TestsFile> testsFile, final String addingFileMsg) {
        final DataReader reader = this;

        int elementsRead = 0;

        for (DataFiles.TestsFile file : testsFile) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final TestsFileReader testsFileReader = new TestsFileReader(file, reader, drDatabase);
            elementsRead += testsFileReader.insert();
        }

        if (elementsRead > 0) {
            log.info("Read {} tests from {} files.", elementsRead, testsFile.size());
        }
        return elementsRead;
    }

    /**
     * Read the populations sections of the configuration file.
     * @param populationsFiles a collection of populations sections.
     * @param addingFileMsg    formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return the number of populations read.
     */
    private int readAllPopulationSections(final List<PopulationFile> populationsFiles, final String addingFileMsg) {
        final DataReader reader = this;

        int elementsRead = 0;

        for (DataFiles.PopulationFile file : populationsFiles) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final PopulationsFileReader populationsFileReader = new PopulationsFileReader(file, reader, drDatabase);
            elementsRead += populationsFileReader.insert();
        }

        if (elementsRead > 0) {
            log.info("Read {} population data from {} files.", elementsRead, populationsFiles.size());
        }
        return elementsRead;
    }

    @SuppressWarnings("PMD.UnusedPrivateField")
    @Getter
    private Lookup lookup;
    private Project.Data data;
    private MovementDatabaseFacade drDatabase;
    private String dbName;
    private String lookupDbName;
}
