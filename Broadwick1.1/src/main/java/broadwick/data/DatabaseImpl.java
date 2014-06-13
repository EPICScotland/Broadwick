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
