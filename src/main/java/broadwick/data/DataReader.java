package broadwick.data;

import broadwick.BroadwickException;
import broadwick.config.generated.DataFiles;
import broadwick.config.generated.DataFiles.BatchMovementFile;
import broadwick.config.generated.DataFiles.DirectedMovementFile;
import broadwick.config.generated.DataFiles.FullMovementFile;
import broadwick.config.generated.DataFiles.LocationsFile;
import broadwick.config.generated.DataFiles.PopulationFile;
import broadwick.config.generated.Project;
import com.google.common.base.Throwables;
import com.google.common.collect.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.time.StopWatch;

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
        if (data != null) {
            if (data.getDatabases() != null) {
                dbName = data.getDatabases().getName();
                lookupDbName = data.getDatabases().getLookupDatabase();
                drDatabase.openDatabase(dbName, lookupDbName);
            } else {
                final String db = "broadwick_" + RandomStringUtils.randomAlphanumeric(8);
                dbName = db + "_db";
                lookupDbName = db + "_lookup_db";
                drDatabase.openDatabase(dbName, lookupDbName);
                drDatabase.createLookupTables();
                readDataSection();
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
     * Read the datafiles, each section type of the <datafiles/> element (e.g. all the <MovementFile/>s) are read in
     * parallel and each individual element is its read in parallel with the data saved to an internal database. Once
     * the datafiles have been read some post-processing is run to determine the locations of individuals at each time
     * step throughout the simulation.
     * @param files the Datafiles object from the configuration file.
     */
    private void readDataFiles(final DataFiles files) {

        // This may be sub-optimal because we could simply create one collection of all files.getMovementFile(),
        // files.getLocationsFile() etc and pass that to the readDataSection method but we would have not record
        // of the number of files/lines read. Instead we create a collection of each type of data file and send that
        // to readDataSection which makes gathering the statistics of the number of lines read from each file type
        // easier to obtain.

        final List<Callable<Integer>> elements = new ArrayList<>();
        final String addingFileMsg = "Adding %s to reading list";

        // add a new callable object to read the directed movements.
        elements.add(readAllDirectedMovementSections(files.getDirectedMovementFile(), addingFileMsg));

        // add a new callable object to read the full movements.
        elements.add(readAllFullMovementSections(files.getFullMovementFile(), addingFileMsg));

        // add a new callable object to read the batched movements.
        elements.add(readAllBatchedMovementSections(files.getBatchMovementFile(), addingFileMsg));

        // add a new callable object to read the locations.
        elements.add(readAllLocationSections(files.getLocationsFile(), addingFileMsg));

        // add a new callable object to read the population data.
        elements.add(readAllPopulationSections(files.getPopulationFile(), addingFileMsg));

        ExecutorService es = null;
        try {
            es = Executors.newFixedThreadPool(8);
            final List<Future<Integer>> callableResults = es.invokeAll(elements);

            // need to do a get here to catch any exceptions thrown in the Callables.
            for (Future<Integer> ftr : callableResults) {
                ftr.get();
            }
        } catch (InterruptedException | ExecutionException | BroadwickException e) {
            log.error("Failure reading data file section. {}", e.getLocalizedMessage());
            throw new BroadwickException(Throwables.getStackTraceAsString(e));
        } finally {
            if (es != null) {
                es.shutdown();
            }
        }
    }

    /**
     * Read the data section of the configuration file. This consists of a series of [movement, population and location]
     * files.
     * @param elements A collection of classes implementing the Callable interface that can read a
     * @return the total number of lines read from the files.
     */
    private int readDataSection(final List<Callable<Integer>> elements) {
        final ExecutorService es = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        int elementsRead = 0;
        try {
            final List<Future<Integer>> results = es.invokeAll(elements);

            for (Future<Integer> i : results) {
                elementsRead += i.get();
            }

        } catch (InterruptedException | ExecutionException e) {
            throw new BroadwickException(Throwables.getStackTraceAsString(e));
        } finally {
            es.shutdown();
        }
        return elementsRead;
    }

    /**
     * Add a description of a configuration file element to an internal table form which we can parse a data file.
     * @param elementName  the name of the element in the configuration file.
     * @param elementIndex the location of the element in each row of the data file.
     * @param elementType  the type of the element in the configuration file.
     * @param elements     a table of the elements name index and type.
     * @param errors       a description of any parsing errors found.
     * @param recordErrors flag to control whether or not error are recorded in the errors parameter.
     * @param sectionName  the name of the section from which the element being added to the table has been read.
     */
    protected final void addElement(final String elementName, final int elementIndex, final String elementType,
                                    final Table<String, Integer, String> elements, final StringBuilder errors,
                                    final boolean recordErrors, final String sectionName) {
        if (elementIndex != 0) {
            elements.put(elementName, elementIndex, elementType);
        } else {
            if (recordErrors) {
                errors.append("No ").append(elementName).append(" column set in ").append(sectionName).append(" section\n");
            }
        }
    }

    /**
     * Create a new callable object to read the directed movements.
     * @param directedMovementFiles a collection of directedMovement sections.
     * @param addingFileMsg         formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return a Callable object that will read and save the section.
     */
    private Callable<Integer> readAllDirectedMovementSections(final List<DirectedMovementFile> directedMovementFiles, final String addingFileMsg) {
        final DataReader reader = this;
        return new Callable<Integer>() {
            @Override
            public Integer call() {
                final List<Callable<Integer>> col = new ArrayList<>();
                for (DataFiles.DirectedMovementFile file : directedMovementFiles) {
                    log.trace(String.format(addingFileMsg, file.getName()));
                    col.add(new DirectedMovementsFileReader(file, reader, drDatabase));
                }
                final int elementsRead = readDataSection(col);
                if (elementsRead > 0) {
                    log.info("Read {} directed movements from {} files.", elementsRead, col.size());
                }
                return elementsRead;
            }

        };
    }

    /**
     * Create a new callable object to read the full movements.
     * @param fullMovementFiles a collection of fullMovement sections.
     * @param addingFileMsg     formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return a Callable object that will read and save the section.
     */
    private Callable<Integer> readAllFullMovementSections(final List<FullMovementFile> fullMovementFiles, final String addingFileMsg) {
        final DataReader reader = this;
        return new Callable<Integer>() {
            @Override
            public Integer call() {
                final List<Callable<Integer>> col = new ArrayList<>();
                for (DataFiles.FullMovementFile file : fullMovementFiles) {
                    log.trace(String.format(addingFileMsg, file.getName()));
                    col.add(new FullMovementsFileReader(file, reader, drDatabase));
                }
                final int elementsRead = readDataSection(col);
                if (elementsRead > 0) {
                    log.info("Read {} full movements from {} files.", elementsRead, col.size());
                }
                return elementsRead;
            }

        };
    }

    /**
     * Create a new callable object to read the batched movements.
     * @param batchMovementFiles a collection of batchMovement sections.
     * @param addingFileMsg      formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return a Callable object that will read and save the section.
     */
    private Callable<Integer> readAllBatchedMovementSections(final List<BatchMovementFile> batchMovementFiles, final String addingFileMsg) {
        final DataReader reader = this;
        return new Callable<Integer>() {
            @Override
            public Integer call() {
                final List<Callable<Integer>> col = new ArrayList<>();
                for (DataFiles.BatchMovementFile file : batchMovementFiles) {
                    log.trace(String.format(addingFileMsg, file.getName()));
                    col.add(new BatchedMovementsFileReader(file, reader, drDatabase));
                }
                final int elementsRead = readDataSection(col);
                if (elementsRead > 0) {
                    log.info("Read {} batched movements from {} files.", elementsRead, col.size());
                }
                return elementsRead;
            }

        };
    }

    /**
     * Create a new callable object to read the locations section.
     * @param locationsFile a collection of locations sections.
     * @param addingFileMsg formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return a Callable object that will read and save the section.
     */
    private Callable<Integer> readAllLocationSections(final List<LocationsFile> locationsFile, final String addingFileMsg) {
        final DataReader reader = this;
        return new Callable<Integer>() {
            @Override
            public Integer call() {
                final List<Callable<Integer>> col = new ArrayList<>();
                for (DataFiles.LocationsFile file : locationsFile) {
                    log.trace(String.format(addingFileMsg, file.getName()));
                    col.add(new LocationsFileReader(file, reader, drDatabase));
                }
                final int elementsRead = readDataSection(col);
                if (elementsRead > 0) {
                    log.info("Read {} locations from {} files.", elementsRead, col.size());
                }
                return elementsRead;
            }

        };
    }

    /**
     * Create a new callable object to read the populations section.
     * @param populationsFiles a collection of populations sections.
     * @param addingFileMsg    formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return a Callable object that will read and save the section.
     */
    private Callable<Integer> readAllPopulationSections(final List<PopulationFile> populationsFiles, final String addingFileMsg) {
        final DataReader reader = this;
        return new Callable<Integer>() {
            @Override
            public Integer call() {
                final List<Callable<Integer>> col = new ArrayList<>();
                for (DataFiles.PopulationFile file : populationsFiles) {
                    log.trace(String.format(addingFileMsg, file.getName()));
                    col.add(new PopulationsFileReader(file, reader, drDatabase));
                }
                final int elementsRead = readDataSection(col);
                if (elementsRead > 0) {
                    log.info("Read {} populations from {} files.", elementsRead, col.size());
                }
                return elementsRead;
            }

        };
    }

    @SuppressWarnings("PMD.UnusedPrivateField")
    @Getter
    private Lookup lookup;
    private Project.Data data;
    private MovementDatabaseFacade drDatabase;
    private String dbName;
    private String lookupDbName;
    private static final int THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
}
