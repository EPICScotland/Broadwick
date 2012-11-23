package broadwick.data;

import broadwick.BroadwickException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.NoSuchElementException;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;

/**
 * All the classes required to read the data section of the configuration file save data to a database. The database
 * functions are all encapsulated in this class.
 */
@Slf4j
public class MovementDatabaseFacade {

    /**
     * Open the connection to the internal database.
     * @param dbName       the name of the database to use as the internal database.
     * @param lookupDbName the name of the lookup table.
     */
    protected final void openDatabase(final String dbName, final String lookupDbName) {
        log.debug("Opening internal databases {}, {}.", dbName, lookupDbName);

        internalDb = new GraphDatabaseFactory().newEmbeddedDatabaseBuilder(dbName).newGraphDatabase();
        nodeIndex = internalDb.index().forNodes("nodes");

        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                internalDb.shutdown();
            }

        });

        try {
            Class.forName("org.h2.Driver");
            lookupDb = DriverManager.getConnection("jdbc:h2:" + lookupDbName);
            lookupDb.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException ex) {
            log.error("Could not open database for internal data. {}", ex.getLocalizedMessage());
        }
    }

    /**
     * Close the connection to the internal database.
     */
    protected final void closeDb() {
        log.debug("Closing internal database.");

        try {
            if (internalDb != null) {
                internalDb.shutdown();
            }

            if (lookupDb != null) {
                lookupDb.commit();
                lookupDb.close();
            }
        } catch (SQLException e) {
            log.trace("Could not close connection to internal DB. This isn't a problem as we're shutting down the server anyway. {}",
                      e.getLocalizedMessage());
        }
    }

    /**
     * Create the lookup tables for internal use.
     */
    public final void createLookupTables() {
        // todo create tables.
    }

    /**
     * Finds a node in the internal database by its id, if there is no node matching this id then one is created so this
     * method will never return null. This method should be used to create all nodes as it is synchronised and protects
     * against duplicate nodes appearing in the database.
     * @param id the id to search for.
     * @return the node referenced by the id value OR a new node with this id IF there is no node in the database with
     *         this id.
     */
    @Synchronized
    protected final Node getNodeById(final String id) {
        Node node = null;
        final Transaction tx = internalDb.beginTx();
        try {
            final IndexHits<Node> hits = nodeIndex.get(indexId, id);
            node = hits.getSingle();
            if (node == null) {
                //log.trace("Could not find node with id {} in database so creating new one", id);

                node = internalDb.createNode();
                node.setProperty(indexId, id);
                nodeIndex.add(node, indexId, id);
            }

            // Make things persistent
            tx.success();
        } catch (NoSuchElementException e) {
            log.error("Could not get unique node with this id from the network - node ids MUST be unique within the network. {}",
                      e.getLocalizedMessage());
            throw new BroadwickException(e);
        } finally {
            tx.finish();
        }
        return node;
    }

    /**
     * Definition of relationship types between locations and individuals for defining movements.
     */
    public static enum MovementRelationship implements RelationshipType {

        MOVES
    }
    @Getter
    private Connection lookupDb = null;
    @Getter
    private GraphDatabaseService internalDb;
    @Getter
    private Index<Node> nodeIndex;
    private String indexId = "index";
    @Getter
    private final DateTime zeroDate = new DateTime(1900, 1, 1, 0, 0);
    public static final String TYPE = "TYPE";
    public static final String LOCATION = "LOCATION";
    public static final String ANIMAL = "ANIMAL";
}
