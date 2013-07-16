package broadwick.data;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import lombok.extern.slf4j.Slf4j;
import org.h2.jdbcx.JdbcConnectionPool;
import org.jooq.SQLDialect;

/**
 * Implementation of a H2 database.
 */
@Slf4j
public final class H2Database implements DatabaseImpl {

    /**
     * Create a new H2 database instance for a given database name. If the database does not exist one is created.
     * @param dbName the name of the database to which we will connect.
     */
    public H2Database(final String dbName) {
        this(dbName, false);
    }

    /**
     * Create a new H2 database instance for a given database name. If the database does not exist one is created, if
     * one exists and the deleteDb is true then the contents of the database are deleted.
     * @param dbName   the name of the database to which we will connect.
     * @param deleteDb delete the contents of the database once a connection is made.
     */
    public H2Database(final String dbName, final boolean deleteDb) {
        this.open(dbName);

        if (deleteDb) {
            try (Connection conn = connectionPool.getConnection()) {
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("DROP ALL OBJECTS");
                }
            } catch (SQLException ex) {
                log.error("Could clean database {}", ex.getLocalizedMessage());
            }
        }
    }

    @Override
    public void open(final String database) {
        try {
            if (connection == null) {
                Class.forName("org.h2.Driver");
                connectionPool = JdbcConnectionPool.create(
                        String.format("jdbc:h2:%s;IGNORECASE=TRUE;TRACE_LEVEL_FILE=0;TRACE_LEVEL_SYSTEM_OUT=0;LOG=0;WRITE_DELAY=100000;MVCC=TRUE;LOCK_MODE=0", database),
                        "", "");
                connectionPool.setLogWriter(null);
            }

//            try {
//                java.sql.DatabaseMetaData meta = connectionPool.getConnection().getMetaData();
//                java.sql.ResultSet res = meta.getTables(null, null, null, new String[]{"TABLE"});
//                System.out.println("List of tables: ");
//                while (res.next()) {
//                    System.out.println(
//                            "   " + res.getString("TABLE_CAT")
//                            + ", " + res.getString("TABLE_SCHEM")
//                            + ", " + res.getString("TABLE_NAME")
//                            + ", " + res.getString("TABLE_TYPE")
//                            + ", " + res.getString("REMARKS"));
//                }
//                res.close();
//            } catch (SQLException e) {
//                log.error("{}", e.getLocalizedMessage());
//
//            }

        } catch (ClassNotFoundException ex) {
            log.error("Could not open database. {}", ex.getLocalizedMessage());
        }
    }

    @Override
    public void close() {
        // Close the database connection:
        try {
            if (connection != null) {
                connection.commit();
                connection.close();
                connectionPool.dispose();
            }
        } catch (SQLException ex) {
            log.error("Could not close database. {}", ex.getLocalizedMessage());
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return connectionPool.getConnection();
    }

    @Override
    public SQLDialect getDialect() {
        return org.jooq.SQLDialect.H2;
    }
    private Connection connection = null;
    private JdbcConnectionPool connectionPool = null;
}
