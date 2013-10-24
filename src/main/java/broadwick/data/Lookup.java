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
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.Record1;
import org.jooq.Record2;
import org.jooq.Result;
import org.jooq.conf.Settings;
import org.jooq.exception.DataAccessException;
import org.jooq.impl.DSL;

/**
 * This class works as an interface to the databases holding the movements, locations and animal data read from the
 * configuration file. When the methods to retrieve all the data in the DB (e.g. getMovements()) are called the results
 * are stored in a cache for speedier retrieval. This cache is not permanent however, freeing memory when required.
 */
@Slf4j
public final class Lookup {

    /**
     * Create the lookup object for accessing data in the internal databases.
     * @param dbFacade the object that is responsible for accessing the internal databases.
     */
    public Lookup(final DatabaseImpl dbFacade) {
        try {
            connection = dbFacade.getConnection();
            final Settings settings = new Settings();
            settings.setExecuteLogging(Boolean.FALSE);
            jooq = DSL.using(dbFacade.getConnection(), dbFacade.getDialect(), settings);
        } catch (SQLException e) {
            log.error("Could not create database lookup object. {}", Throwables.getStackTraceAsString(e));
        }
    }

    /**
     * Get the number of tests stored in the internal database.
     * @return the number of tests in the database.
     */
    public int getNumTests() {
        int numTests = 0;
        try {
            final Result<Record1<Integer>> fetch = jooq.selectCount().from(TestsFileReader.getTABLE_NAME()).fetch();
            numTests = fetch.get(0).value1();
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get number of tests - perhaps the table hasn't been created.");
        }
        return numTests;
    }

    /**
     * Get the number of animals stored in the internal database.
     * @return the number of animals in the database.
     */
    public int getNumAnimals() {
        int numAnimals = 0;
        try {
            final Result<Record1<Integer>> fetch = jooq.selectCount().from(PopulationsFileReader.getLIFE_HISTORIES_TABLE_NAME()).fetch();
            numAnimals = fetch.get(0).value1();
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get number of animals - perhaps the table hasn't been created.");
        }
        return numAnimals;
    }

    /**
     * Get the number of locations stored in the internal database.
     * @return the number of locations in the database.
     */
    public int getNumLocations() {
        int numLocations = 0;
        try {
            final Result<Record1<Integer>> fetch = jooq.selectCount().from(LocationsFileReader.getTABLE_NAME()).fetch();
            numLocations = fetch.get(0).value1();
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get number of locations - perhaps the table hasn't been created.");
        }
        return numLocations;
    }

    /**
     * Get the number of movements stored in the internal database.
     * @return the number of movements in the database.
     */
    public int getNumMovements() {
        int numMovements = 0;

        try {
            numMovements = jooq.selectCount().from(BatchedMovementsFileReader.getTABLE_NAME()).fetch().get(0).value1();
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get number of movements from {} - perhaps the table hasn't been created.",
                      BatchedMovementsFileReader.getTABLE_NAME());
        }
        try {
            numMovements += jooq.selectCount().from(FullMovementsFileReader.getTABLE_NAME()).fetch().get(0).value1();
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get number of movements from {} - perhaps the table hasn't been created.",
                      FullMovementsFileReader.getTABLE_NAME());
        }
        try {
            numMovements += jooq.selectCount().from(DirectedMovementsFileReader.getTABLE_NAME()).fetch().get(0).value1();
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get number of movements from {} - perhaps the table hasn't been created.",
                      DirectedMovementsFileReader.getTABLE_NAME());
        }
        return numMovements;
    }

    /**
     * Get all the movements that have been read from the file(s) specified in the configuration file.
     * @return a collection of movement events that have been recorded.
     */
    public Collection<Movement> getMovements() {
        final Collection<Movement> movements = new HashSet<>();
        final StopWatch sw = new StopWatch();
        sw.start();

        Result<Record> records;
        try {
            records = jooq.select().from(BatchedMovementsFileReader.getTABLE_NAME()).fetch();
            for (Record r : records) {
                final Movement movement = createMovement(r);
                if (movement != null) {
                    movements.add(movement);
                }
            }
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                      BatchedMovementsFileReader.getTABLE_NAME());
        }

        try {
            records = jooq.select().from(FullMovementsFileReader.getTABLE_NAME()).fetch();
            for (Record r : records) {
                final Movement movement = createMovement(r);
                if (movement != null) {
                    movements.add(movement);
                }
            }
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                      FullMovementsFileReader.getTABLE_NAME());
        }

        try {
            records = jooq.select().from(DirectedMovementsFileReader.getTABLE_NAME()).fetch();
            for (Record r : records) {
                final Movement movement = createMovement(r);
                if (movement != null) {
                    movements.add(movement);
                }
            }
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                      DirectedMovementsFileReader.getTABLE_NAME());
        }

        sw.stop();
        log.debug("Found {} movements in {}.", movements.size(), sw.toString());
        return movements;
    }

    /**
     * Get all the movements that have been read from the file(s) specified in the configuration file filtered on a date
     * range.
     * @param startDate the first date in the range with which we will filter the movements
     * @param endDate   the final date in the range with which we will filter the movements
     * @return a collection of movement events that have been recorded.
     */
    public Collection<Movement> getMovements(final int startDate, final int endDate) {
        log.trace("Getting all movements between {} and {}", startDate, endDate);
        final Collection<Movement> movements = new HashSet<>();
        final StopWatch sw = new StopWatch();
        sw.start();

        try {
            // try directed movements
            Result<Record> records;

            try {
                records = jooq.select().from(DirectedMovementsFileReader.getTABLE_NAME())
                        .where(String.format("%s >= %d and %s <= %d",
                                             DirectedMovementsFileReader.getMOVEMENT_DATE(), startDate,
                                             DirectedMovementsFileReader.getMOVEMENT_DATE(), endDate))
                        .fetch();
                for (Record r : records) {
                    final Movement movement = createMovement(r);
                    if (movement != null) {
                        movements.add(movement);
                    }
                }
            } catch (org.jooq.exception.DataAccessException e) {
                log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                          DirectedMovementsFileReader.getTABLE_NAME());
            }

            try {
                // try full movements
                records = jooq.select().from(FullMovementsFileReader.getTABLE_NAME())
                        .where(String.format("%s >= %d and %s <= %d",
                                             FullMovementsFileReader.getDEPARTURE_DATE(), startDate,
                                             FullMovementsFileReader.getDESTINATION_DATE(), endDate))
                        .fetch();
                for (Record r : records) {
                    final Movement movement = createMovement(r);
                    if (movement != null) {
                        movements.add(movement);
                    }
                }
            } catch (org.jooq.exception.DataAccessException e) {
                log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                          FullMovementsFileReader.getTABLE_NAME());
            }

            try {
                // try batched movements
                records = jooq.select().from(BatchedMovementsFileReader.getTABLE_NAME())
                        .where(String.format("%s >= %d and %s <= %d",
                                             BatchedMovementsFileReader.getDEPARTURE_DATE(), startDate,
                                             BatchedMovementsFileReader.getDESTINATION_DATE(), endDate))
                        .fetch();
                for (Record r : records) {
                    final Movement movement = createMovement(r);
                    if (movement != null) {
                        movements.add(movement);
                    }
                }
            } catch (org.jooq.exception.DataAccessException e) {
                log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                          BatchedMovementsFileReader.getTABLE_NAME());
            }

        } catch (org.jooq.exception.DataAccessException e) {
            // We WILL get an error here - we don't know the type of table here so we try directed, batched and full
            // movements. We can ignore the errors.
        }

        sw.stop();
        log.debug("Found {} movements in {}.", movements.size(), sw.toString());
        return movements;
    }

    /**
     * Get all the OFF movements that have been read from the file(s) specified in the configuration file filtered on a
     * date range.
     * @param startDate the first date in the range with which we will filter the movements
     * @param endDate   the final date in the range with which we will filter the movements
     * @return a collection of movement events that have been recorded.
     */
    public Collection<Movement> getOffMovements(final int startDate, final int endDate) {
        log.trace("Getting off movements between {} and {}", startDate, endDate);
        final Collection<Movement> movements = new HashSet<>();
        final StopWatch sw = new StopWatch();
        sw.start();

        try {
            // try directed movements
            Result<Record> records;

            try {
                records = jooq.select().from(DirectedMovementsFileReader.getTABLE_NAME())
                        .where(String.format("%s >= %d AND %s <= %d AND %s ='OFF'",
                                             DirectedMovementsFileReader.getMOVEMENT_DATE(), startDate,
                                             DirectedMovementsFileReader.getMOVEMENT_DATE(), endDate,
                                             DirectedMovementsFileReader.getMOVEMENT_DIRECTION()))
                        .fetch();
                for (Record r : records) {
                    final Movement movement = createMovement(r);
                    if (movement != null) {
                        movements.add(movement);
                    }
                }
            } catch (org.jooq.exception.DataAccessException e) {
                log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                          DirectedMovementsFileReader.getTABLE_NAME());
            }

            try {
                // try full movements
                records = jooq.select().from(FullMovementsFileReader.getTABLE_NAME())
                        .where(String.format("%s >= %d and %s <= %d",
                                             FullMovementsFileReader.getDEPARTURE_DATE(), startDate,
                                             FullMovementsFileReader.getDEPARTURE_DATE(), endDate))
                        .fetch();
                for (Record r : records) {
                    final Movement movement = createMovement(r);
                    if (movement != null) {
                        movements.add(movement);
                    }
                }
            } catch (org.jooq.exception.DataAccessException e) {
                log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                          FullMovementsFileReader.getTABLE_NAME());
            }

            try {
                // try batched movements
                records = jooq.select().from(BatchedMovementsFileReader.getTABLE_NAME())
                        .where(String.format("%s >= %d and %s <= %d",
                                             BatchedMovementsFileReader.getDEPARTURE_DATE(), startDate,
                                             BatchedMovementsFileReader.getDEPARTURE_DATE(), endDate))
                        .fetch();
                for (Record r : records) {
                    final Movement movement = createMovement(r);
                    if (movement != null) {
                        movements.add(movement);
                    }
                }
            } catch (org.jooq.exception.DataAccessException e) {
                log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                          BatchedMovementsFileReader.getTABLE_NAME());
            }

        } catch (org.jooq.exception.DataAccessException e) {
            // We WILL get an error here - we don't know the type of table here so we try directed, batched and full
            // movements. We can ignore the errors.
        }

        sw.stop();
        log.debug("Found {} off movements in {}.", movements.size(), sw.toString());
        return movements;
    }

    /**
     * Get all the ON movements that have been read from the file(s) specified in the configuration file filtered on a
     * date range.
     * @param startDate the first date in the range with which we will filter the movements
     * @param endDate   the final date in the range with which we will filter the movements
     * @return a collection of movement events that have been recorded.
     */
    public Collection<Movement> getOnMovements(final int startDate, final int endDate) {
        log.trace("Getting on movements between {} and {}", startDate, endDate);
        final Collection<Movement> movements = new HashSet<>();
        final StopWatch sw = new StopWatch();
        sw.start();

        try {
            // try directed movements
            Result<Record> records;

            try {
                records = jooq.select().from(DirectedMovementsFileReader.getTABLE_NAME())
                        .where(String.format("%s >= %d AND %s <= %d AND %s ='ON'",
                                             DirectedMovementsFileReader.getMOVEMENT_DATE(), startDate,
                                             DirectedMovementsFileReader.getMOVEMENT_DATE(), endDate,
                                             DirectedMovementsFileReader.getMOVEMENT_DIRECTION()))
                        .fetch();
                for (Record r : records) {
                    final Movement movement = createMovement(r);
                    if (movement != null) {
                        movements.add(movement);
                    }
                }
            } catch (org.jooq.exception.DataAccessException e) {
                log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                          DirectedMovementsFileReader.getTABLE_NAME());
            }

            try {
                // try full movements
                records = jooq.select().from(FullMovementsFileReader.getTABLE_NAME())
                        .where(String.format("%s >= %d and %s <= %d",
                                             FullMovementsFileReader.getDESTINATION_DATE(), startDate,
                                             FullMovementsFileReader.getDESTINATION_DATE(), endDate))
                        .fetch();
                for (Record r : records) {
                    final Movement movement = createMovement(r);
                    if (movement != null) {
                        movements.add(movement);
                    }
                }
            } catch (org.jooq.exception.DataAccessException e) {
                log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                          FullMovementsFileReader.getTABLE_NAME());
            }

            try {
                // try batched movements
                records = jooq.select().from(BatchedMovementsFileReader.getTABLE_NAME())
                        .where(String.format("%s >= %d and %s <= %d",
                                             BatchedMovementsFileReader.getDESTINATION_DATE(), startDate,
                                             BatchedMovementsFileReader.getDESTINATION_DATE(), endDate))
                        .fetch();
                for (Record r : records) {
                    final Movement movement = createMovement(r);
                    if (movement != null) {
                        movements.add(movement);
                    }
                }
            } catch (org.jooq.exception.DataAccessException e) {
                log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                          BatchedMovementsFileReader.getTABLE_NAME());
            }

        } catch (org.jooq.exception.DataAccessException e) {
            // We WILL get an error here - we don't know the type of table here so we try directed, batched and full
            // movements. We can ignore the errors.
        }

        sw.stop();
        log.debug("Found {} on movements in {}.", movements.size(), sw.toString());
        return movements;
    }

    /**
     * Get all the tests that have been read from the file(s) specified in the configuration file.
     * @return a collection of movement events that have been recorded.
     */
    public Collection<Test> getTests() {
        final Collection<Test> tests = new ArrayList<>();
        final StopWatch sw = new StopWatch();
        sw.start();

        final Result<Record> records = jooq.select().from(TestsFileReader.getTABLE_NAME()).fetch();
        for (Record r : records) {
            final Test test = createTest(r);
            if (test != null) {
                tests.add(test);
            }
        }

        sw.stop();
        log.debug("Found {} tests in {}.", tests.size(), sw.toString());
        return tests;
    }

    /**
     * Get all the tests that have been read from the file(s) specified in the configuration file.
     * @param startDate the first date in the range with which we will filter the tests.
     * @param endDate   the final date in the range with which we will filter the tests.
     * @return a collection of movement events that have been recorded.
     */
    public Collection<Test> getTests(final int startDate, final int endDate) {
        final Collection<Test> tests = new ArrayList<>();
        final StopWatch sw = new StopWatch();
        sw.start();

        final Result<Record> records = jooq.select().from(TestsFileReader.getTABLE_NAME())
                .where(String.format("%s >= %d and %s <= %d",
                                     TestsFileReader.getTEST_DATE(), startDate,
                                     TestsFileReader.getTEST_DATE(), endDate))
                .fetch();
        for (Record r : records) {
            final Test test = createTest(r);
            if (test != null) {
                tests.add(test);
            }
        }

        sw.stop();
        log.debug("Found {} tests in {}.", tests.size(), sw.toString());
        return tests;
    }

    /**
     * Get all the animals that have been read from the file(s) specified in the configuration file.
     * @return a collection of animal events that have been recorded.
     */
    public Collection<Animal> getAnimals() {
        final Collection<Animal> animals = new HashSet<>();
        final StopWatch sw = new StopWatch();
        sw.start();

        final Result<Record> records = jooq.select().from(PopulationsFileReader.getLIFE_HISTORIES_TABLE_NAME()).fetch();
        for (Record r : records) {
            final Animal animal = createAnimal(r);
            if (animal != null) {
                animals.add(animal);
            }
        }

        sw.stop();
        log.debug("Found {} animals in {}.", animals.size(), sw.toString());
        return animals;
    }

    /**
     * Get all the animals that have been read from the file(s) specified in the configuration file whose date of birth
     * is before or on a given date and whose date of death (it there is any) is on or after the same date.
     * @param date the date for which we reuqire the animals in the system.
     * @return a collection of animals that have been recorded whose DoB &ge; date and DoD &ge; date
     */
    public Collection<Animal> getAnimals(final int date) {
        final Collection<Animal> animals = new HashSet<>();
        final String whereClause = String.format("%s <= %d and (%s IS NULL or %s >= %d)",
                                                 PopulationsFileReader.getDATE_OF_BIRTH(), date,
                                                 PopulationsFileReader.getDATE_OF_DEATH(),
                                                 PopulationsFileReader.getDATE_OF_DEATH(), date);

        final StopWatch sw = new StopWatch();
        sw.start();

        final Result<Record> records = jooq.select().from(PopulationsFileReader.getLIFE_HISTORIES_TABLE_NAME()).where(whereClause)
                .fetch();
        for (Record r : records) {
            final Animal animal = createAnimal(r);
            if (animal != null) {
                animals.add(animal);
            }
        }

        sw.stop();
        log.debug("Found {} animals in {}.", animals.size(), sw.toString());
        return animals;
    }

    /**
     * Get all the movements that have been read from the file(s) specified in the configuration file.
     * @return a collection of movement events that have been recorded.
     */
    public Collection<Location> getLocations() {
        final Collection<Location> locations = new ArrayList<>();
        final StopWatch sw = new StopWatch();
        sw.start();

        final Result<Record> records = jooq.select().from(LocationsFileReader.getTABLE_NAME()).fetch();
        for (Record r : records) {
            final Location location = createLocation(r);
            if (location != null) {
                locations.add(location);
            }
        }

        sw.stop();
        log.debug("Found {} locations in {}.", locations.size(), sw.toString());
        return locations;
    }

    /**
     * Get a location from the list of locations in the system. If there is no location matching the id a
     * BroadwickException is thrown because we should only be looking for valid locations. Note, this method returns a
     * live view of the movements so changes to one affect the other and in a worst case scenario can cause a
     * ConcurrentModificationException. The returned collection isn't threadsafe or serializable, even if unfiltered is.
     * @param locationId the id of the location we are looking for.
     * @return the Location object with the required id.
     */
    public Location getLocation(final String locationId) {
        Location location = locationsCache.getIfPresent(locationId);
        if (location == null) {
            final Result<Record> records = jooq.select().from(LocationsFileReader.getTABLE_NAME())
                    .where(String.format("%s = '%s'", LocationsFileReader.getID(), locationId)).fetch();
            for (Record r : records) {
                location = createLocation(r);
                locationsCache.put(location.getId(), location);
            }
        }
        return location;
    }

    /**
     * Get an animal from the list of animals in the system. If there is no animal matching the id a BroadwickException
     * is thrown because we should only be looking for valid animals. Note, this method returns a live view of the
     * movements so changes to one affect the other and in a worst case scenario can cause a
     * ConcurrentModificationException. The returned collection isn't threadsafe or serializable, even if unfiltered is.
     * @param animalId the id of the animal we are looking for.
     * @return the Animal object with the required id.
     */
    public Animal getAnimal(final String animalId) {
        Animal animal = animalsCache.getIfPresent(animalId);
        if (animal == null) {
            final Result<Record> records = jooq.select().from(PopulationsFileReader.getLIFE_HISTORIES_TABLE_NAME())
                    .where(String.format("ID = '%s'", animalId)).fetch();
            for (Record r : records) {
                animal = createAnimal(r);
                animalsCache.put(animal.getId(), animal);
            }
        }
        return animal;
    }

    /**
     * Get all the recorded movements for a given animal. Note, this method returns a live view of the movements so
     * changes to one affect the other and in a worst case scenario can cause a ConcurrentModificationException. The
     * returned collection isn't threadsafe or serializable, even if unfiltered is.
     * @param animalId the id of the animal whose movements are to be returned.
     * @return a collection of movement events that have been recorded for the animal with the given id.
     */
    public Collection<Movement> getMovementsForAnimal(final String animalId) {
        final Collection<Movement> movements = new HashSet<>();
        final StopWatch sw = new StopWatch();
        sw.start();

        Result<Record> records;
        try {
            records = jooq.select().from(FullMovementsFileReader.getTABLE_NAME())
                    .where(String.format("%s = '%s'", FullMovementsFileReader.getID(), animalId))
                    .orderBy(DSL.fieldByName(FullMovementsFileReader.getDEPARTURE_DATE()).asc())
                    .fetch();
            for (Record r : records) {
                movements.add(createMovement(r));
            }
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                      FullMovementsFileReader.getTABLE_NAME());
        }

        try {
            records = jooq.select().from(DirectedMovementsFileReader.getTABLE_NAME())
                    .where(String.format("%s = '%s'", DirectedMovementsFileReader.getID(), animalId))
                    .fetch();
            for (Record r : records) {
                movements.add(createMovement(r));
            }
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                      DirectedMovementsFileReader.getTABLE_NAME());
        }

        sw.stop();
        log.debug("Found {} movements in {}.", movements.size(), sw.toString());
        return movements;
    }

    /**
     * Get an animals location at a specified date. If the animal does not have a specified location, e.g. if we are
     * asking for its location before it's born or in the middle of a movement where the departure and destination dates
     * span several days then a null location will be returned.
     * @param animalId the id of the animal.
     * @param date     the date for which we want the animals location.
     * @return the location of the animal on date or Location.getNullLocation if there isn't a valid location.
     */
    public String getAnimalLocationIdAtDate(final String animalId, final int date) {

        String locationId = "";
        int locationDate = Integer.MIN_VALUE;
        Result<Record2<Object, Object>> records;
        try {
            // get the destination id of the last movement BEFORE the given date.
            records = jooq.select(DSL.fieldByName(FullMovementsFileReader.getDESTINATION_ID()),
                                  DSL.fieldByName(FullMovementsFileReader.getDESTINATION_DATE()))
                    .from(FullMovementsFileReader.getTABLE_NAME())
                    .where(String.format("%s = '%s' and (%s <= %d or %s <= %d)",
                                         FullMovementsFileReader.getID(), animalId,
                                         FullMovementsFileReader.getDEPARTURE_DATE(), date,
                                         FullMovementsFileReader.getDESTINATION_DATE(), date))
                    .orderBy(DSL.fieldByName(FullMovementsFileReader.getDESTINATION_DATE()).desc())
                    .limit(1)
                    .fetch();
            if (records.isNotEmpty()) {
                final int thisDate = (int) records.getValue(0, DSL.fieldByName(FullMovementsFileReader.getDESTINATION_DATE()));
                if (thisDate > locationDate) {
                    locationDate = (int) records.getValue(0, DSL.fieldByName(FullMovementsFileReader.getDESTINATION_DATE()));
                    locationId = (String) records.getValue(0, DSL.fieldByName(FullMovementsFileReader.getDESTINATION_ID()));
                }
            }
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                      FullMovementsFileReader.getTABLE_NAME());
        }

        try {
            records = jooq.select(DSL.fieldByName(DirectedMovementsFileReader.getLOCATION_ID()),
                                  DSL.fieldByName(DirectedMovementsFileReader.getMOVEMENT_DATE()))
                    .from(DirectedMovementsFileReader.getTABLE_NAME())
                    .where(String.format("%s = '%s' and %s <= %d",
                                         DirectedMovementsFileReader.getID(), animalId,
                                         DirectedMovementsFileReader.getMOVEMENT_DATE(), date))
                    .orderBy(DSL.fieldByName(DirectedMovementsFileReader.getMOVEMENT_DATE()).desc())
                    .limit(1)
                    .fetch();
            if (records.isNotEmpty()) {
                final int thisDate = (int) records.getValue(0, DSL.fieldByName(DirectedMovementsFileReader.getMOVEMENT_DATE()));
                if (thisDate > locationDate) {
                    locationDate = (int) records.getValue(0, DSL.fieldByName(DirectedMovementsFileReader.getMOVEMENT_DATE()));
                    locationId = (String) records.getValue(0, DSL.fieldByName(DirectedMovementsFileReader.getLOCATION_ID()));
                }
            }
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get movements from {} - this is not an error; the project might be configutred with one.",
                      DirectedMovementsFileReader.getTABLE_NAME());
        }

        if (locationDate > Integer.MIN_VALUE) {
            return locationId;
        } else {
            // else no movement => it is still on it's location of birth.
            final Animal animal = getAnimal(animalId);
            if (animal != null) {
                return animal.getLocationOfBirth();
            } else {
                log.error("Could not find location for {} at {}", animalId, date);
            }
        }
        return null;
    }

    /**
     * Run a custom query against the database. This method is not intended to be used in general situations as it
     * exposed the underlying jooq data structures but in some situations it may be used as a last resort.
     * @param query the SQL query to be run.
     * @return a Result set of records that were returned by the database.
     */
    public Result<Record> runCustomQuery(final String query) {
        Result<Record> records = null;
        try {
            records = jooq.fetch(query);
        } catch (DataAccessException e) {
            log.error("Could not execute SQL {}. {}", query, e.getLocalizedMessage());
        }
        return records;
    }

    /**
     * Create a location object from the node object defining it in the graph database.
     * @param locationRecord the record object from the database defining the location.
     * @return the created location object.
     */
    private Location createLocation(final Record locationRecord) {
        String id = "";
        Double easting = null;
        Double northing = null;

        try {
            id = (String) locationRecord.getValue(LocationsFileReader.getID());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            easting = (Double) locationRecord.getValue(LocationsFileReader.getEASTING());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            northing = (Double) locationRecord.getValue(LocationsFileReader.getNORTHING());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }

        // TODO add custom tags
//        for (int i = 6; i < locationRecord.size(); i++) {
//            locationRecord.getValue(i);
//        }
        final Map<String, Integer> populations = new HashMap<>();
        log.trace("Creating location object for {}",
                  String.format("%s %f,%f %s", id, easting, northing, populations));

        return new Location(id, easting, northing, populations);
    }

    /**
     * Create an animal object from the node object defining it in the graph database.
     * @param animalRecord the record object from the database defining the animal.
     * @return the created animal object.
     */
    private Animal createAnimal(final Record animalRecord) {

        String id = "";
        Integer dob = null;
        String lob = "";
        Integer dod = null;
        String lod = "";
        String species = "";

        try {
            id = (String) animalRecord.getValue(PopulationsFileReader.getID());
        } catch (ArrayIndexOutOfBoundsException e) {
            log.trace("error {}", e.getLocalizedMessage());
            // ignore - the field was not in the record.
        }
        try {
            dob = (Integer) animalRecord.getValue(PopulationsFileReader.getDATE_OF_BIRTH());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            lob = (String) animalRecord.getValue(PopulationsFileReader.getLOCATION_OF_BIRTH());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            dod = (Integer) animalRecord.getValue(PopulationsFileReader.getDATE_OF_DEATH());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            lod = (String) animalRecord.getValue(PopulationsFileReader.getLOCATION_OF_DEATH());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            species = (String) animalRecord.getValue(PopulationsFileReader.getSPECIES());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }

        // TODO add custom tags
//        for (int i = 6; i < animalRecord.size(); i++) {
//            animalRecord.getValue(i);
//        }

        log.trace("Creating animal object for {}",
                  String.format("%s (%s) dob:%d[%s] dod:%d[%s]", id, species, dob, lob, dod, lod));

        return new Animal(id, species, dob, lob, dod, lod);

    }

    /**
     * Create a Test object from the node object defining it in the graph database.
     * @param testRecord the record object from the database defining the test.
     * @return the created test object.
     */
    private Test createTest(final Record testRecord) {

        String id = "";
        String group = "";
        String location = "";
        Integer testDate = null;
        Boolean positiveResult = null;
        Boolean negativeResult = null;

        try {
            id = (String) testRecord.getValue(TestsFileReader.getID());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            group = (String) testRecord.getValue(TestsFileReader.getGROUP_ID());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            location = (String) testRecord.getValue(TestsFileReader.getLOCATION_ID());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            testDate = (Integer) testRecord.getValue(TestsFileReader.getTEST_DATE());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            final Integer val = (Integer) testRecord.getValue(TestsFileReader.getPOSITIVE_RESULT());
            if (val == 0) {
                positiveResult = Boolean.FALSE;
            } else {
                positiveResult = Boolean.TRUE;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            final Integer val = (Integer) testRecord.getValue(TestsFileReader.getNEGATIVE_RESULT());
            if (val == 0) {
                negativeResult = Boolean.FALSE;
            } else {
                negativeResult = Boolean.TRUE;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }

        // TODO add custom tags
//        for (int i = 6; i < testRecord.size(); i++) {
//            testRecord.getValue(i);
//        }

        log.trace("Creating test object for {}",
                  String.format("%s group:%s location:%s date:%d pos:%s neg:%s", id, group, location, testDate, positiveResult, negativeResult));

        return new Test(id, group, location, testDate, positiveResult, negativeResult);
    }

    /**
     * Create a movement object from the relationship object defining it in the graph database.
     * @param movementRecord the record object from the database defining the movement.
     * @return the created movement object.
     */
    private Movement createMovement(final Record movementRecord) {

        String id = "";
        Integer batchSize = null;
        Integer departureDate = null;
        String departureId = "";
        Integer destinationDate = null;
        String destinationId = "";
        Integer marketDate = null;
        String marketId = "";
        String species = "";

        try {
            id = (String) movementRecord.getValue(FullMovementsFileReader.getID());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            batchSize = (Integer) movementRecord.getValue(BatchedMovementsFileReader.getBATCH_SIZE());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            departureDate = (Integer) movementRecord.getValue(FullMovementsFileReader.getDEPARTURE_DATE());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            departureId = (String) movementRecord.getValue(FullMovementsFileReader.getDEPARTURE_ID());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            destinationDate = (Integer) movementRecord.getValue(FullMovementsFileReader.getDESTINATION_DATE());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            destinationId = (String) movementRecord.getValue(FullMovementsFileReader.getDESTINATION_ID());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            marketDate = (Integer) movementRecord.getValue(BatchedMovementsFileReader.getMARKET_DATE());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            marketId = (String) movementRecord.getValue(BatchedMovementsFileReader.getMARKET_ID());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        try {
            species = (String) movementRecord.getValue(DirectedMovementsFileReader.getSPECIES());
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }
        // For directed movements, set the appropriate destination/departure id and dates.
        try {
            final String direction = (String) movementRecord.getValue(DirectedMovementsFileReader.getMOVEMENT_DIRECTION());
            if ("ON".equalsIgnoreCase(direction)) {
                destinationId = (String) movementRecord.getValue(DirectedMovementsFileReader.getLOCATION_ID());
                destinationDate = (Integer) movementRecord.getValue(DirectedMovementsFileReader.getMOVEMENT_DATE());
            } else {
                departureId = (String) movementRecord.getValue(DirectedMovementsFileReader.getLOCATION_ID());
                departureDate = (Integer) movementRecord.getValue(DirectedMovementsFileReader.getMOVEMENT_DATE());
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            // ignore - the field was not in the record.
        }

        // TODO add custom tags
//        for (int i = 6; i < testRecord.size(); i++) {
//            testRecord.getValue(i);
//        }

        log.trace("Creating movement object for {}",
                  String.format("%s batchSize:%d departureDate:%d departureId:%s destinationDate:%d destinationId:%s marketDate:%s marketId:%s species:%s",
                                id, batchSize, departureDate, departureId, destinationDate, destinationId, marketDate, marketId, species));

        return new Movement(id, batchSize, departureDate, departureId, destinationDate, destinationId, marketDate, marketId, species);
    }
    Cache<String, Collection<Movement>> movementsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    Cache<String, Location> locationsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    Cache<String, Animal> animalsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    Cache<String, Test> testsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    private DSLContext jooq;
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private Connection connection;
}

/**
 * Implement a comparator for movements so that movements can be stored in ascending date order with OFF movements
 * appearing before ON movements in the movements cache.
 */
class MovementsComparator implements Comparator<Movement> {

    @Override
    public int compare(final Movement m1, final Movement m2) {
        // it's probably easiest to use the natural ordering determined by the toString() methods.
        // The departure information appears in the string before the destination information so we, in effect are 
        // ordering by departure date.
        return m1.toString().compareTo(m2.toString());
    }
}
