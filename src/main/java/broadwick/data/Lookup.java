package broadwick.data;

import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.jooq.Record;
import org.jooq.Result;
import org.jooq.impl.Factory;

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
            jooq = new Factory(dbFacade.getConnection(), dbFacade.getDialect());
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
            final Result<Record> fetch = jooq.selectCount().from(TestsFileReader.getTABLE_NAME()).fetch();
            numTests = fetch.get(0).getValueAsInteger(0);
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
            final Result<Record> fetch = jooq.selectCount().from(PopulationsFileReader.getLIFE_HISTORIES_TABLE_NAME()).fetch();
            numAnimals = fetch.get(0).getValueAsInteger(0);
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
            final Result<Record> fetch = jooq.selectCount().from(LocationsFileReader.getTABLE_NAME()).fetch();
            numLocations = fetch.get(0).getValueAsInteger(0);
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
            numMovements = jooq.selectCount().from(BatchedMovementsFileReader.getTABLE_NAME()).fetch().get(0).getValueAsInteger(0);
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get number of movements from {} - perhaps the table hasn't been created.",
                      BatchedMovementsFileReader.getTABLE_NAME());
        }
           try {
             numMovements += jooq.selectCount().from(FullMovementsFileReader.getTABLE_NAME()).fetch().get(0).getValueAsInteger(0);
        } catch (org.jooq.exception.DataAccessException e) {
            log.trace("Could not get number of movements from {} - perhaps the table hasn't been created.",
                      FullMovementsFileReader.getTABLE_NAME());
        }
            try {
            numMovements += jooq.selectCount().from(DirectedMovementsFileReader.getTABLE_NAME()).fetch().get(0).getValueAsInteger(0);
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

        Result<Record> records = jooq.select().from(BatchedMovementsFileReader.getTABLE_NAME()).fetch();
        for (Record r : records) {
            movements.add(createMovement(r));
        }

        records = jooq.select().from(FullMovementsFileReader.getTABLE_NAME()).fetch();
        for (Record r : records) {
            movements.add(createMovement(r));
        }

        records = jooq.select().from(DirectedMovementsFileReader.getTABLE_NAME()).fetch();
        for (Record r : records) {
            movements.add(createMovement(r));
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
        final Collection<Movement> movements = new HashSet<>();
        final StopWatch sw = new StopWatch();
        sw.start();

        try {
            // try directed movements
            Result<Record> records = jooq.select().from(DirectedMovementsFileReader.getTABLE_NAME())
                    .where("MovementDate >= " + startDate + " and MovementDate <= endDate")
                    .fetch();
            for (Record r : records) {
                movements.add(createMovement(r));
            }

            // try full movements
            records = jooq.select().from(FullMovementsFileReader.getTABLE_NAME())
                    .where("DepartureDate >= " + startDate + " and DestinationDate <= endDate")
                    .fetch();
            for (Record r : records) {
                movements.add(createMovement(r));
            }

            // try batched movements
            records = jooq.select().from(BatchedMovementsFileReader.getTABLE_NAME())
                    .where("DepartureDate >= " + startDate + " and DestinationDate <= endDate")
                    .fetch();
            for (Record r : records) {
                movements.add(createMovement(r));
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
     * Get all the movements that have been read from the file(s) specified in the configuration file.
     * @return a collection of movement events that have been recorded.
     */
    public Collection<Test> getTests() {
        final Collection<Test> tests = new ArrayList<>();
        final StopWatch sw = new StopWatch();
        sw.start();

        final Result<Record> records = jooq.select().from(TestsFileReader.getTABLE_NAME()).fetch();
        for (Record r : records) {
            tests.add(createTest(r));
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

        final Result<Record> records = jooq.select().from("LifeHistories").fetch();
        for (Record r : records) {
            animals.add(createAnimal(r));
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
        final StringBuilder sb = new StringBuilder();
        sb.append(PopulationsFileReader.getDATE_OF_BIRTH()).append(" <= ").append(date);
        sb.append(" and (").append(PopulationsFileReader.getDATE_OF_DEATH()).append(" IS NULL or");
        sb.append(PopulationsFileReader.getDATE_OF_DEATH()).append(" >= ").append(date);
        sb.append(")");

        final StopWatch sw = new StopWatch();
        sw.start();

        // TODO add filter
        final Result<Record> records = jooq.select().from(PopulationsFileReader.getTableName())
                .where(sb.toString()).fetch();
        for (Record r : records) {
            animals.add(createAnimal(r));
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
            locations.add(createLocation(r));
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
                    .where(LocationsFileReader.getID() + " = " + locationId).fetch();
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
            final Result<Record> records = jooq.select().from(PopulationsFileReader.getTableName())
                    .where("ID = " + animalId).fetch();
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

        Result<Record> records = jooq.select().from(FullMovementsFileReader.getTABLE_NAME())
                .where("Id = " + animalId)
                .fetch();
        for (Record r : records) {
            movements.add(createMovement(r));
        }

        records = jooq.select().from(DirectedMovementsFileReader.getTABLE_NAME())
                .where("Id = " + animalId)
                .fetch();
        for (Record r : records) {
            movements.add(createMovement(r));
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
    public Location getAnimalLocationAtDate(final String animalId, final int date) {

        //TODO
        return null;
    }

    /**
     * Create a location object from the node object defining it in the graph database.
     * @param locationRecord the record object from the database defining the location.
     * @return the created location object.
     */
    private Location createLocation(final Record locationRecord) {
        log.trace("Creating location object for {}", locationRecord.getField(LocationsFileReader.getID()));

        final String id = (String) locationRecord.getValue(LocationsFileReader.getID());
        final Double easting = (Double) locationRecord.getValue(LocationsFileReader.getEASTING());
        final Double northing = (Double) locationRecord.getValue(LocationsFileReader.getNORTHING());
        // TODO add custom tags
//        for (int i = 6; i < locationRecord.size(); i++) {
//            locationRecord.getValue(i);
//        }
        final Map<String, Integer> populations = new HashMap<>();
        return new Location(id, easting, northing, populations);
    }

    /**
     * Create an animal object from the node object defining it in the graph database.
     * @param animalRecord the record object from the database defining the animal.
     * @return the created animal object.
     */
    private Animal createAnimal(final Record animalRecord) {
        log.trace("Creating animal object for {}", animalRecord.getField(PopulationsFileReader.getID()));

        final String id = (String) animalRecord.getValue(PopulationsFileReader.getID());
        final Integer dob = (Integer) animalRecord.getValue(PopulationsFileReader.getDATE_OF_BIRTH());
        final String lob = (String) animalRecord.getValue(PopulationsFileReader.getLOCATION_OF_BIRTH());
        final Integer dod = (Integer) animalRecord.getValue(PopulationsFileReader.getDATE_OF_DEATH());
        final String lod = (String) animalRecord.getValue(PopulationsFileReader.getLOCATION_OF_DEATH());
        final String species = (String) animalRecord.getValue(PopulationsFileReader.getSPECIES());

        // TODO add custom tags
//        for (int i = 6; i < animalRecord.size(); i++) {
//            animalRecord.getValue(i);
//        }
        return new Animal(id, species, dob, lob, dod, lod);
    }

    /**
     * Create a Test object from the node object defining it in the graph database.
     * @param testRecord the record object from the database defining the test.
     * @return the created test object.
     */
    private Test createTest(final Record testRecord) {
        log.trace("Creating test object for {}", testRecord.getField(TestsFileReader.getID()));

        final String id = (String) testRecord.getValue(TestsFileReader.getID());
        final String group = (String) testRecord.getValue(TestsFileReader.getGROUP_ID());
        final String location = (String) testRecord.getValue(TestsFileReader.getLOCATION_ID());
        final Integer testDate = (Integer) testRecord.getValue(TestsFileReader.getTEST_DATE());
        final Boolean positiveResult = (Boolean) testRecord.getValue(TestsFileReader.getPOSITIVE_RESULT());
        final Boolean negativeResult = (Boolean) testRecord.getValue(TestsFileReader.getNEGATIVE_RESULT());

        // TODO add custom tags
//        for (int i = 6; i < testRecord.size(); i++) {
//            testRecord.getValue(i);
//        }
        return new Test(id, group, location, testDate, positiveResult, negativeResult);
    }

    /**
     * Create a movement object from the relationship object defining it in the graph database.
     * @param movementRecord the record object from the database defining the movement.
     * @return the created movement object.
     */
    private Movement createMovement(final Record movementRecord) {
        log.trace("Creating movement object for {}", movementRecord.toString());

        final String id = (String) movementRecord.getValue(FullMovementsFileReader.getID());
        final Integer batchSize = (Integer) movementRecord.getValue(BatchedMovementsFileReader.getBATCH_SIZE());
        final Integer departureDate = (Integer) movementRecord.getValue(FullMovementsFileReader.getDEPARTURE_DATE());
        final String departureId = (String) movementRecord.getValue(FullMovementsFileReader.getDEPARTURE_ID());
        final Integer destinationDate = (Integer) movementRecord.getValue(FullMovementsFileReader.getDESTINATION_DATE());
        final String destinationId = (String) movementRecord.getValue(FullMovementsFileReader.getDESTINATION_ID());
        final Integer marketDate = (Integer) movementRecord.getValue(BatchedMovementsFileReader.getMARKET_DATE());
        final String marketId = (String) movementRecord.getValue(BatchedMovementsFileReader.getMARKET_ID());
        final String species = (String) movementRecord.getValue(DirectedMovementsFileReader.getSPECIES());

        // TODO add custom tags
//        for (int i = 6; i < testRecord.size(); i++) {
//            testRecord.getValue(i);
//        }
        return new Movement(id, batchSize, departureDate, departureId, destinationDate, destinationId, marketDate, marketId, species);
    }

    Cache<String, Collection<Movement>> movementsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    Cache<String, Location> locationsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    Cache<String, Animal> animalsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    Cache<String, Test> testsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    private Factory jooq;
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
