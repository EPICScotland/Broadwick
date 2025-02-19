/*
 * Copyright 2013 University of Glasgow.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package broadwick.data;

import broadwick.data.readers.FullMovementsFileReader;
import broadwick.data.readers.PopulationsFileReader;
import broadwick.data.readers.BatchedMovementsFileReader;
import broadwick.data.readers.LocationsFileReader;
import broadwick.data.readers.DirectedMovementsFileReader;
import broadwick.data.readers.TestsFileReader;
import broadwick.BroadwickException;
import broadwick.config.generated.DataFiles;
import broadwick.config.generated.DataFiles.BatchMovementFile;
import broadwick.config.generated.DataFiles.DirectedMovementFile;
import broadwick.config.generated.DataFiles.FullMovementFile;
import broadwick.config.generated.DataFiles.LocationsFile;
import broadwick.config.generated.DataFiles.PopulationFile;
import broadwick.config.generated.DataFiles.TestsFile;
import broadwick.config.generated.Project;
import java.util.List;
import java.util.Map;
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

        // if there is a data section in the config file let's read it.
        if (data != null) {
            if (data.getDatabases() != null) {
                // there is a database mentioned in the config file so let's use it
                dbName = data.getDatabases().getName();
//                dbImpl = new FirebirdDatabase(dbName, false);
//                dbImpl = new DerbyDatabase(dbName, false);
//                dbImpl = new HyperSqlDatabase(dbName, false);
                dbImpl = new H2Database(dbName, false);
            } else if (data.getDatafiles() != null) {
                // there is a datafiles section in the config file so we will read them and create a randomly named 
                // database.
                final String db = "broadwick_" + RandomStringUtils.randomAlphanumeric(8);
                dbName = db + "_db";
//                dbImpl = new FirebirdDatabase(dbName, true);
//                dbImpl = new DerbyDatabase(dbName, true);
//                dbImpl = new HyperSqlDatabase(dbName, true);
                dbImpl = new H2Database(dbName, true);
                readDataSection();
            } else {
                throw new BroadwickException("There is a <data> section in the configuration file but neither a <database> nor <datafiles> section.");
            }

            lookup = new Lookup(dbImpl);
            logDbStatistics();
        }

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
        if (dbImpl != null) {
            log.trace("Closing database connection");
            dbImpl.close();
        }
    }

    /**
     * Save a message giving the numbers of elements in the database.
     */
    private void logDbStatistics() {
        log.info("Read {} population data from the database.", lookup.getNumAnimals());
        log.info("Read {} locations data from the database.", lookup.getNumLocations());
        log.info("Read {} tests data from the database.", lookup.getNumTests());
        log.info("Read {} movements data from the database.", lookup.getNumMovements());
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
            readAllPopulationSections(files.getPopulationFile(), addingFileMsg);
            readAllTestSections(files.getTestsFile(), addingFileMsg);
            readAllFullMovementSections(files.getFullMovementFile(), addingFileMsg);
            readAllDirectedMovementSections(files.getDirectedMovementFile(), addingFileMsg);
            readAllBatchedMovementSections(files.getBatchMovementFile(), addingFileMsg);
        } catch (Exception e) {
            log.error("Failure reading data file section. {}", e.getLocalizedMessage());
            throw new BroadwickException(String.format("Failure reading data file section. %s", e.getLocalizedMessage()));
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
        int elementsRead = 0;

        for (DataFiles.DirectedMovementFile file : directedMovementFiles) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final DirectedMovementsFileReader movementsFileReader = new DirectedMovementsFileReader(file, dbImpl);
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
        int elementsRead = 0;

        for (DataFiles.FullMovementFile file : fullMovementFiles) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final FullMovementsFileReader movementsFileReader = new FullMovementsFileReader(file, dbImpl);
            elementsRead += movementsFileReader.insert();
        }

        if (elementsRead > 0) {
            log.info("Read {} full movements from {} files.", elementsRead, fullMovementFiles.size());
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
        int elementsRead = 0;

        for (DataFiles.BatchMovementFile file : batchMovementFiles) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final BatchedMovementsFileReader movementsFileReader = new BatchedMovementsFileReader(file, dbImpl);
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
        int elementsRead = 0;

        for (DataFiles.LocationsFile file : locationsFile) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final LocationsFileReader locationsFileReader = new LocationsFileReader(file, dbImpl);
            elementsRead += locationsFileReader.insert();
        }

        if (elementsRead > 0) {
            log.info("Read {} locations from {} files.", elementsRead, locationsFile.size());
        }
        return elementsRead;
    }

    /**
     * Read the test sections of the configuration file.
     * @param testsFile     a collection of locations sections.
     * @param addingFileMsg formatted string (in the form "Adding %s to reading list") to be added to log files.
     * @return the number of tests read.
     */
    private int readAllTestSections(final List<TestsFile> testsFile, final String addingFileMsg) {
        int elementsRead = 0;

        for (DataFiles.TestsFile file : testsFile) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final TestsFileReader testsFileReader = new TestsFileReader(file, dbImpl);
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
        int elementsRead = 0;

        for (DataFiles.PopulationFile file : populationsFiles) {
            log.trace(String.format(addingFileMsg, file.getName()));
            final PopulationsFileReader populationsFileReader = new PopulationsFileReader(file, dbImpl);
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
    private DatabaseImpl dbImpl;
    private String dbName;
}
