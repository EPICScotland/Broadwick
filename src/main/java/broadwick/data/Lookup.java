package broadwick.data;

import broadwick.BroadwickException;
import broadwick.data.MovementDatabaseFacade.MovementRelationship;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;

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
    public Lookup(final MovementDatabaseFacade dbFacade) {
        this.dbFacade = dbFacade;
        this.db = dbFacade.getInternalDb();
        if (this.db != null) {
            this.ops = GlobalGraphOperations.at(this.db);
        } else {
            this.ops = null;
        }
    }

    /**
     * Get all the movements that have been read from the file(s) specified in the configuration file.
     * @return a collection of movement events that have been recorded.
     */
    public Collection<Movement> getMovements() {
        final Iterable<Relationship> allRelationships = ops.getAllRelationships();
        final Iterator<Relationship> movementRelationships = Iterables.filter(allRelationships,
                                                                              isMovementPredicate).iterator();
        final Collection<Movement> movements = new ArrayList<>();
        while (movementRelationships.hasNext()) {
            final Relationship movementRelationship = movementRelationships.next();

            // create a Movement and add it to the collection...
            final Movement movement = createMovement(movementRelationship);
            movements.add(movement);

            // store the movements for the animal in the cache.
            Collection<Movement> movementsForAnimal = movementsCache.getIfPresent(movement.getId());
            if (movementsForAnimal == null) {
                movementsForAnimal = new TreeSet<>(new MovementsComparator());
                movementsForAnimal.add(movement);
                movementsCache.put(movement.getId(), movementsForAnimal);
            } else {
                movementsForAnimal.add(movement);
            }
        }
        return movements;
    }

    /**
     * Get all the movements that have been read from the file(s) specified in the configuration file.
     * @return a collection of movement events that have been recorded.
     */
    public Collection<Animal> getAnimals() {
        final Collection<Animal> animals = new ArrayList<>();

        final Iterable<Node> allNodes = ops.getAllNodes();
        final Iterator<Node> animalNodes = Iterables.filter(allNodes, isAnimalPredicate).iterator();

        while (animalNodes.hasNext()) {
            final Node animalNode = animalNodes.next();

            // create a Movement and add it to the collection...
            final Animal animal = createAnimal(animalNode);
            animals.add(animal);
            if (animalsCache.getIfPresent(animal.getId()) == null) {
                animalsCache.put(animal.getId(), animal);
            }
        }
        return animals;
    }

    /**
     * Get all the movements that have been read from the file(s) specified in the configuration file.
     * @return a collection of movement events that have been recorded.
     */
    public Collection<Location> getLocations() {
        final Collection<Location> locations = new ArrayList<>();

        final Iterable<Node> allNodes = ops.getAllNodes();
        final Iterator<Node> locationNodes = Iterables.filter(allNodes, isLocationPredicate).iterator();

        while (locationNodes.hasNext()) {
            final Node locationNode = locationNodes.next();

            // create a Movement and add it to the collection...
            final Location location = createLocation(locationNode);
            locations.add(location);
            if (locationsCache.getIfPresent(location.getId()) == null) {
                locationsCache.put(location.getId(), location);
            }
        }
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
            try {
                location = Iterables.find(getLocations(), new Predicate<Location>() {
                    @Override
                    public boolean apply(final Location loc) {
                        return loc.getId().equals(locationId);
                    }

                });
            } catch (NoSuchElementException e) {
                // this is an error, we are looking for a location that does not exist.
                log.error("No location with id {} exists.", locationId);
                throw new BroadwickException("No location with id " + locationId + " exists.");
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
            try {
                animal = Iterables.find(getAnimals(), new Predicate<Animal>() {
                    @Override
                    public boolean apply(final Animal anm) {
                        return anm.getId().equals(animalId);
                    }

                });
            } catch (NoSuchElementException e) {
                // this is an error, we are looking for a animal that does not exist.
                log.error("No animal with id {} exists.", animalId);
                throw new BroadwickException("No animal with id " + animalId + " exists.");
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

        final Collection<Movement> movementsForAnimal = movementsCache.getIfPresent(animalId);
        if (movementsForAnimal == null) {
            // the cache have been destryed so do the long lookup
            return Collections2.filter(getMovements(), new Predicate<Movement>() {
                @Override
                public boolean apply(final Movement movement) {
                    return animalId.equals(movement.getId());
                }

            });
        }

        return movementsForAnimal;
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
        final Collection<Movement> movements = getMovementsForAnimal(animalId);

        String locationId = "";
        // Simply iterate over all the movements up to and including this date setting the Location
        // as we go. This should be ok since we are have ordered the movements.
        for (Movement movement : movements) {
            if (movement.getDestinationDate() != null && movement.getDestinationDate() <= date) {
                final String departureId = movement.getDepartureId();
                final String destinationId = movement.getDestinationId();

                if (destinationId.isEmpty()) {
                    locationId = departureId;
                } else {
                    locationId = destinationId;
                }
            } else if (movement.getDepartureDate() != null && movement.getDepartureDate() <= date) {
                // we might have a movement that straddles the required date if so the animal moved off the departure
                // location but not arrived at the destination so they have no location for this date.
                locationId = "";
            }
        }

        if ("".equals(locationId)) {
            return Location.getNullLocation();
        }

        return getLocation(locationId);
    }

    /**
     * Convert a date object to an integer (number of days from a fixed start date, here 1/1/1900). All dates in the
     * database are stored as integer values using this method.
     * @param date the date object we are converting.
     * @return the number of days from a fixed 'zero date'.
     */
    public int getDate(final DateTime date) {
        return Days.daysBetween(dbFacade.getZeroDate(), date).getDays();
    }

    /**
     * Convert a date object to an integer (number of days from a fixed start date, here 1/1/1900). All dates in the
     * database are stored as integer values using this method.
     * @param date       the date object we are converting.
     * @param dateFormat the format the date is in when doing the conversion.
     * @return the number of days from a fixed 'zero date'.
     */
    public int getDate(final String date, final String dateFormat) {
        final DateTimeFormatter pattern = DateTimeFormat.forPattern(dateFormat);
        final DateTime dateTime = pattern.parseDateTime(date);
        return this.getDate(dateTime);
    }

    /**
     * Create a location object from the node object defining it in the graph database.
     * @param locationNode the node object from the database defining the location.
     * @return the created location object.
     */
    private Location createLocation(final Node locationNode) {
        log.trace("Creating location object from data node: {}", locationNode.getPropertyKeys());

        final String id = (String) locationNode.getProperty(LocationsFileReader.getID());
        final Double easting = (Double) locationNode.getProperty(LocationsFileReader.getEASTING());
        final Double northing = (Double) locationNode.getProperty(LocationsFileReader.getNORTHING());
        final Map<String, Integer> populations = new HashMap<>();

        return new Location(id, easting, northing, populations);
    }

    /**
     * Create an animal object from the node object defining it in the graph database. A node ma
     * @param animalNode the node object from the database defining the animal.
     * @return the created animal object.
     */
    private Animal createAnimal(final Node animalNode) {
        log.trace("Creating animal object from data node: {}", animalNode.getPropertyKeys());

        if (Iterables.contains(animalNode.getPropertyKeys(), PopulationsFileReader.getID())) {
            final String id = (String) animalNode.getProperty(PopulationsFileReader.getID());
            final String species = (String) animalNode.getProperty(PopulationsFileReader.getSPECIES());
            final Integer dateOfBirth = (Integer) animalNode.getProperty(PopulationsFileReader.getDATE_OF_BIRTH());
            final String locationOfBirth = (String) animalNode.getProperty(PopulationsFileReader.getLOCATION_OF_BIRTH());
            final Integer dateOfDeath = (Integer) animalNode.getProperty(PopulationsFileReader.getDATE_OF_DEATH());
            final String locationOfDeath = (String) animalNode.getProperty(PopulationsFileReader.getLOCATION_OF_DEATH());

            return new Animal(id, species, dateOfBirth, locationOfBirth, dateOfDeath, locationOfDeath);
        }


        return null;
    }

    /**
     * Create a movement object from the relationship object defining it in the graph database.
     * @param relationship the relationship object from the database defining the movement.
     * @return the created movement object.
     */
    private Movement createMovement(final Relationship relationship) {

        final ArrayList<String> relationshipProperties = Lists.newArrayList(relationship.getPropertyKeys());

        if (relationshipProperties.size() == directedMovementKeys.size()
                && relationshipProperties.containsAll(directedMovementKeys)) {
            //is a directed movement so read the elements.
            return createMovementFromDirectedMovementData(relationship);
        } else if (relationshipProperties.size() == fullMovementKeys.size()
                && relationshipProperties.containsAll(fullMovementKeys)) {
            //is a full movement so read the elements.
            return createMovementFromFullMovementData(relationship);
        } else if (relationshipProperties.size() == batchedMovementKeys.size()
                && relationshipProperties.containsAll(batchedMovementKeys)) {
            //is a batched movement so read the elements.
            return createMovementFromBatchedMovementData(relationship);
        } else {
            log.error("Could not determine type of movement for {}", relationship.toString());
        }

        try {
            // The nodes at both ends should have been read already, if they haven't been then it means that the
            // location or animal id does not exist in the data files specified in the configuration file.
            // If the nodes have been read and stored correctly they will have a 'TYPE' attribute, as a simple check 
            // we will check for it and catch the resulting NotFoundException and log the resulting error if 
            // necessary.
            if (relationship.getStartNode().getProperty(MovementDatabaseFacade.TYPE) == null
                    || relationship.getEndNode().getProperty(MovementDatabaseFacade.TYPE) == null) {
                log.error("Invalid movement. Either the source, destination or individual of the move does not appear in the data files.");
            }

        } catch (org.neo4j.graphdb.NotFoundException nfe) {
            log.error("Invalid movement. Either the source, destination or individual of the move does not appear in the data files. {}", nfe.getLocalizedMessage());
        } finally {
            return null;
        }
    }

    /**
     * Create a movement object from the [directed movement] relationship object defining it in the graph database.
     * @param relationship the relationship object from the database defining the movement.
     * @return the created movement object.
     */
    private Movement createMovementFromDirectedMovementData(final Relationship relationship) {
        log.trace("Creating movement object from directed movement data: {}", relationship.getPropertyKeys());

        final String locationId = (String) relationship.getProperty(DirectedMovementsFileReader.getLOCATION_ID());
        final String direction = (String) relationship.getProperty(DirectedMovementsFileReader.getMOVEMENT_DIRECTION());
        final String id = (String) relationship.getProperty(DirectedMovementsFileReader.getID());
        final String species = (String) relationship.getProperty(DirectedMovementsFileReader.getSPECIES());
        final Integer date = (Integer) relationship.getProperty(DirectedMovementsFileReader.getMOVEMENT_DATE());

        String departureId = "";
        Integer departureDate = null;
        String destinationId = "";
        Integer destinationDate = null;

        if ("OFF".equalsIgnoreCase(direction) || "DEATH".equalsIgnoreCase(direction)) {
            // the end node is the departure node
            departureId = locationId;
            departureDate = date;
        } else {
            // the end node is the destination node
            destinationId = locationId;
            destinationDate = date;
        }

        return new Movement(id, 1, departureDate, departureId, destinationDate, destinationId,
                            null, "", species);
    }

    /**
     * Create a movement object from the [batched movement] relationship object defining it in the graph database.
     * @param relationship the relationship object from the database defining the movement.
     * @return the created movement object.
     */
    private Movement createMovementFromBatchedMovementData(final Relationship relationship) {
        log.trace("Creating movement object from batched movement data: {}", relationship.getPropertyKeys());

        final Integer batchSize = (Integer) relationship.getProperty(BatchedMovementsFileReader.getBATCH_SIZE());
        final String departureId = (String) relationship.getProperty(BatchedMovementsFileReader.getDEPARTURE_ID());
        final Integer departureDate = (Integer) relationship.getProperty(BatchedMovementsFileReader.getDEPARTURE_DATE());
        final String destinationId = (String) relationship.getProperty(BatchedMovementsFileReader.getDESTINATION_ID());
        final Integer destinationDate = (Integer) relationship.getProperty(BatchedMovementsFileReader.getDESTINATION_DATE());
        final String marketId = (String) relationship.getProperty(BatchedMovementsFileReader.getMARKET_ID());
        final Integer marketDate = (Integer) relationship.getProperty(BatchedMovementsFileReader.getMARKET_DATE());
        final String species = (String) relationship.getProperty(BatchedMovementsFileReader.getSPECIES());

        return new Movement("", batchSize, departureDate, departureId, destinationDate, destinationId,
                            marketDate, marketId, species);
    }

    /**
     * Create a movement object from the [full movement] relationship object defining it in the graph database.
     * @param relationship the relationship object from the database defining the movement.
     * @return the created movement object.
     */
    private Movement createMovementFromFullMovementData(final Relationship relationship) {
        log.trace("Creating movement object from full movement data: {}", relationship.getPropertyKeys());

        final String id = (String) relationship.getProperty(FullMovementsFileReader.getID());
        final String departureId = (String) relationship.getProperty(FullMovementsFileReader.getDEPARTURE_ID());
        final Integer departureDate = (Integer) relationship.getProperty(FullMovementsFileReader.getDEPARTURE_DATE());
        final String destinationId = (String) relationship.getProperty(FullMovementsFileReader.getDESTINATION_ID());
        final Integer destinationDate = (Integer) relationship.getProperty(FullMovementsFileReader.getDESTINATION_DATE());
        final String marketId = (String) relationship.getProperty(FullMovementsFileReader.getMARKET_ID());
        final Integer marketDate = (Integer) relationship.getProperty(FullMovementsFileReader.getMARKET_DATE());
        final String species = (String) relationship.getProperty(FullMovementsFileReader.getSPECIES());

        return new Movement(id, 1, departureDate, departureId, destinationDate, destinationId,
                            marketDate, marketId, species);
    }

    private final Predicate<Node> isAnimalPredicate = new Predicate<Node>() {
        @Override
        public boolean apply(final Node node) {
            return node.hasProperty(MovementDatabaseFacade.TYPE)
                    && MovementDatabaseFacade.ANIMAL.equals(node.getProperty(MovementDatabaseFacade.TYPE));
        }

    };
    private final Predicate<Node> isLocationPredicate = new Predicate<Node>() {
        @Override
        public boolean apply(final Node node) {
            return node.hasProperty(MovementDatabaseFacade.TYPE)
                    && MovementDatabaseFacade.LOCATION.equals(node.getProperty(MovementDatabaseFacade.TYPE));
        }

    };
    private final Predicate<Relationship> isMovementPredicate = new Predicate<Relationship>() {
        @Override
        public boolean apply(final Relationship input) {
            return input.isType(MovementRelationship.MOVES);
        }

    };
    private final List<String> directedMovementKeys = Arrays.asList(
            DirectedMovementsFileReader.getLOCATION_ID(),
            DirectedMovementsFileReader.getMOVEMENT_DIRECTION(),
            DirectedMovementsFileReader.getID(),
            DirectedMovementsFileReader.getSPECIES(),
            DirectedMovementsFileReader.getMOVEMENT_DATE());
    private final List<String> fullMovementKeys = Arrays.asList(
            FullMovementsFileReader.getID(),
            FullMovementsFileReader.getDEPARTURE_DATE(),
            FullMovementsFileReader.getDEPARTURE_ID(),
            FullMovementsFileReader.getDESTINATION_DATE(),
            FullMovementsFileReader.getDESTINATION_ID(),
            FullMovementsFileReader.getMARKET_ID(),
            FullMovementsFileReader.getMARKET_DATE(),
            FullMovementsFileReader.getSPECIES());
    private final List<String> batchedMovementKeys = Arrays.asList(
            BatchedMovementsFileReader.getBATCH_SIZE(),
            BatchedMovementsFileReader.getDEPARTURE_DATE(),
            BatchedMovementsFileReader.getDEPARTURE_ID(),
            BatchedMovementsFileReader.getDESTINATION_DATE(),
            BatchedMovementsFileReader.getDESTINATION_ID(),
            BatchedMovementsFileReader.getMARKET_ID(),
            BatchedMovementsFileReader.getMARKET_DATE(),
            BatchedMovementsFileReader.getSPECIES());
    Cache<String, Collection<Movement>> movementsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    Cache<String, Location> locationsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    Cache<String, Animal> animalsCache = CacheBuilder.newBuilder().maximumSize(1000).build();
    private final MovementDatabaseFacade dbFacade;
    private final GraphDatabaseService db;
    private final GlobalGraphOperations ops;
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
