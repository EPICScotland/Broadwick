package broadwick.data;

import broadwick.BroadwickException;
import com.google.common.base.Throwables;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.index.lucene.unsafe.batchinsert.LuceneBatchInserterIndexProvider;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.unsafe.batchinsert.BatchInserter;
import org.neo4j.unsafe.batchinsert.BatchInserterIndex;
import org.neo4j.unsafe.batchinsert.BatchInserterIndexProvider;
import org.neo4j.unsafe.batchinsert.BatchInserters;

/**
 * All the classes required to read the data section of the configuration file save data to a database. The database
 * functions are all encapsulated in this class.
 */
@Slf4j
public class MovementDatabaseFacade {

    /**
     * Open the connection to the internal database. This method uses a batch inserter to do the inserting.
     * @param dbName       the name of the database to use as the internal database.
     * @param lookupDbName the name of the lookup table.
     */
    protected final void openDatabaseForInserting(final String dbName, final String lookupDbName) {
        try {
            dbService = null;
            dbInserter = BatchInserters.inserter(dbName);

            indexProvider = new LuceneBatchInserterIndexProvider(dbInserter);
            index = indexProvider.nodeIndex("actors", MapUtil.stringMap("type", "exact"));
            index.setCacheCapacity("name", 100000);

            // Registers a shutdown hook for the Neo4j instance so that it
            // shuts down nicely when the VM exits (even if you "Ctrl-C" the
            // running example before it's completed)
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    closeDb();
                }

            });
        } catch (Exception e) {
            log.error("Error setting up databases.\n{}", Throwables.getStackTraceAsString(e));
            closeDb();
        }

        try {
            Class.forName("org.h2.Driver");
            lookupDb = DriverManager.getConnection("jdbc:h2:" + lookupDbName);
            lookupDb.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException ex) {
            log.error("Could not open database for internal data. {}", ex.getLocalizedMessage());
        }
    }

    /**
     * Open the connection to the internal database.
     * @param dbName       the name of the database to use as the internal database.
     * @param lookupDbName the name of the lookup table.
     */
    protected final void openDatabase(final String dbName, final String lookupDbName) {

        // first close any open hooks used for batch inserting.
        closeDb();

        log.debug("Opening internal databases {}, {}.", dbName, lookupDbName);
        try {
            dbInserter = null;
            dbService = new EmbeddedGraphDatabase(dbName);

            // Registers a shutdown hook for the Neo4j instance so that it
            // shuts down nicely when the VM exits (even if you "Ctrl-C" the
            // running example before it's completed)
            Runtime.getRuntime().addShutdownHook(new Thread() {
                @Override
                public void run() {
                    closeDb();
                }

            });
        } catch (Exception e) {
            log.error("Error setting up databases.\n{}", Throwables.getStackTraceAsString(e));
            closeDb();
        }

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
        log.trace("Closing internal database.");

        try {
            if (dbInserter != null) {
                // shutdown the index provide so that the indexes are written to disk...
                indexProvider.shutdown();
                dbInserter.shutdown();
            }

            if (dbService != null) {
                dbService.shutdown();
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
    protected final Long getNodeById(final String id) {
        Long node;

        final IndexHits<Long> hits = index.get(indexId, id);
        if (hits.size() > 1) {
            log.error("Found the following nodes with id = {}\n", id);
            for (long hit : hits) {
                log.error("{}", hit);
            }
            throw new BroadwickException("More than one node exists with id = " + id);
        }

        node = hits.getSingle();
        if (node == null) {
            //log.trace("Could not find node with id {} in database so creating new one", id);

            final Map<String, Object> properties = new HashMap<>();
            properties.put(indexId, id);

            node = dbInserter.createNode(properties);
        }

        return node;
    }

    /**
     * Create a node in the database with the given id returning it's automatically generated index or, if the id is
     * already in the database, return the index for the node with the given id.
     * @param id         the id of the node we wish to create
     * @param properties the properties we are assigning to the created node.
     * @return the index (auomatically generated id) of the created node.
     */
    @Synchronized
    protected final Long createNode(final String id, final Map<String, Object> properties) {
        final IndexHits<Long> hits = index.get(indexId, id);
        if (hits.size() == 0) {
            return dbInserter.createNode(properties);
        } else if (hits.size() == 1) {
            return hits.getSingle();
        }
        return null;
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
    private GraphDatabaseService dbService;
    @Getter
    private BatchInserter dbInserter;
    @Getter
    private BatchInserterIndex index;
    private BatchInserterIndexProvider indexProvider;
    private String indexId = "index";
    @Getter
    @SuppressWarnings("PMD.UnusedPrivateField")
    private final DateTime zeroDate = new DateTime(1900, 1, 1, 0, 0);
    public static final String TYPE = "TYPE";
    public static final String LOCATION = "LOCATION";
    public static final String ANIMAL = "ANIMAL";
    public static final String TEST = "TEST";
}
