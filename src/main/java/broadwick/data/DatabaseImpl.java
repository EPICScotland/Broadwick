package broadwick.data;

import java.sql.Connection;
import java.sql.SQLException;
import org.jooq.SQLDialect;

/**
 * Interface defining the database implementations.
 */
public interface DatabaseImpl extends java.lang.AutoCloseable {

    /**
     * Open the database for connection.
     * @param dbName the name of the database to open/connect.
     */
    void open(final String dbName);

    /**
     * Close the database connection.
     */
    @Override
    void close();

    /**
     * Get a connection to the database.
     * @return the connection object.
     * @throws SQLException if the connection cannot be obtained.
     */
    Connection getConnection() throws SQLException ;
    
    /**
     * Obtain the database dialect e.g. SQLDialect.MYSQL or SQLDialect.H2.
     * @return the SQLDialect of the database.
     */
    SQLDialect getDialect();

}
