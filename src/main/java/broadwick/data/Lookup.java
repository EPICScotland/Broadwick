package broadwick.data;

import broadwick.data.MovementDatabaseFacade.MovementRelationship;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.tooling.GlobalGraphOperations;

/**
 * This class works as an interface to the databases holding the movements, locations and animal data read from the
 * configuration file.
 */
@Slf4j
public final class Lookup {

    /**
     * Create the lookup object for accessing data in the internal databases.
     * @param dbFacade the object that is responsible for accessing the internal databases.
     */
    public Lookup(final MovementDatabaseFacade dbFacade) {
        this.db = dbFacade.getInternalDb();
        this.ops = GlobalGraphOperations.at(this.db);
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
        }
        return movements;
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
    private final GraphDatabaseService db;
    private final GlobalGraphOperations ops;
}
