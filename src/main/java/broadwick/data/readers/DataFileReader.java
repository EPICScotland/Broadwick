package broadwick.data.readers;

import broadwick.BroadwickConstants;
import broadwick.BroadwickException;
import broadwick.io.FileInput;
import com.google.common.base.Throwables;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

/**
 * Base class for all data file readers, containing useful common functionality..
 */
@Slf4j
public abstract class DataFileReader {

    /**
     * Convert a collection of elements read from the data file (the column names in the internal database) into a CSV
     * string.
     * @param data the collection of column names.
     * @return a csv string of the input data.
     */
    protected final String asCsv(final Collection<String> data) {
        final StringBuilder sb = new StringBuilder();
        for (String name : data) {
            if (name != null) {
                sb.append(name).append(",");
            }
        }
        // remove the last character.
        final String csv = sb.toString();
        return csv.substring(0, csv.length() - 1);
    }

    /**
     * Convert a collection of elements read from the data file (the column names in the internal database) into a CSV
     * string of question marks.
     * @param data the collection of column names.
     * @return as csv string of the question marks containing the same number of data elements.
     */
    protected final String asQuestionCsv(final Collection<String> data) {
        final StringBuilder sb = new StringBuilder();
        for (String name : data) {
            if (name != null) {
                sb.append("?,");
            }
        }
        // remove the last character.
        final String csv = sb.toString();
        return csv.substring(0, csv.length() - 1);
    }

    /**
     * Create or append to a string command in the form "CREATE TABLE IF NOT EXISTS [table] ([col1], [col2], ...,
     * [coln]) values (?,?,...,?)" from the details read from the configuration file.
     * <pre>
     * updateCreateTableCommand(ID, 1, " VARCHAR(128), ", new HashMap<String, Integer>(), createTableCommand,
     * TABLE_NAME, SECTION_NAME, errors);
     * </pre>
     * @param columnName         the name of the column in the table
     * @param columnIndex        the location in the csv file where it appears.
     * @param columnType         the type of column e.g. "INT", "VARCHAR(128) NOT NULL" etc.
     * @param insertedColDetails a map containing the name of the column and the location in the csv file where it
     *                           appears i.e. columnName, columnIndex.
     * @param createTableCommand a StringBuilder object that we append to to create the create table command.
     * @param tableName          the name of the table that is being created.
     * @param sectionName        the name of the section in the configuration file that configures the data.
     * @param errors             a stringbuilder object to which we will append error messages.
     * @return the name of the column added.
     */
    protected final String updateCreateTableCommand(final String columnName, final Integer columnIndex, final String columnType,
                                                    final Map<String, Integer> insertedColDetails,
                                                    final StringBuilder createTableCommand, final String tableName,
                                                    final String sectionName, final StringBuilder errors) {
        if (createTableCommand.length() == 0) {
            createTableCommand.append(String.format("CREATE TABLE %s (", tableName));
        }

        String addedColumnName = null;
        if (columnIndex != null) {
            if (columnIndex != 0) {
                createTableCommand.append(columnName).append(columnType);
                insertedColDetails.put(columnName, columnIndex);
                addedColumnName = columnName;
            } else {
                errors.append("No ").append(columnName).append(" column set in ").append(sectionName).append(" section\n");
            }
        }

        return addedColumnName;
    }

    /**
     * Execute a command to create a table.
     * @param tableName          the name of the table to be created.
     * @param createTableCommand the command to create the table.
     * @param connection         the database connection to use to create the table.
     * @throws SQLException if a SQL error has been encountered.
     */
    protected final void createTable(final String tableName, final String createTableCommand, final Connection connection)
            throws SQLException {

        final DatabaseMetaData dbm = connection.getMetaData();

        // First check if the table already exists, some databases do not support
        // CREATE TABLE ??? IF NOT EXISTS
        // so we have to look at the database schema
        try (ResultSet resultSet = dbm.getTables(null, null, "%", null)) {
            boolean tableExists = false;
            while (resultSet.next()) {
                if (tableName.equalsIgnoreCase(resultSet.getString("TABLE_NAME"))) {
                    log.debug("Table {} already exists, ignoring", tableName);
                    tableExists = true;
                }
            }

            if (!tableExists) {
                try (Statement stmt = connection.createStatement()) {
                    final String[] commands = createTableCommand.split(";");
                    for (int i = 0; i < commands.length; i++) {
                        log.trace("Creating table {}", commands[i]);
                        stmt.execute(commands[i]);
                    }
                } catch (SQLException sqle) {
                    connection.rollback();
                    log.error("Error while creating the table '{}'. {}", createTableCommand,
                              Throwables.getStackTraceAsString(sqle));
                    throw sqle;
                }
            }

//        } catch (Exception e) {
//            log.error("Could not create database {}", Throwables.getStackTraceAsString(e));
        }

        connection.commit();
    }

    /**
     * Perform the insertion into the database.
     * @param connection      the connection to the database.
     * @param tableName       the name of the table into which the data will be put.
     * @param insertString    the command used to insert a row into the database.
     * @param dataFile        the [CSV] file that contained the data.
     * @param dateFormat      the format of the date in the file.
     * @param insertedColInfo a map of column name to column in the data file.
     * @param dateFields      a collection of columns in the csv file that contains date fields.
     * @return the number of rows inserted.
     */
    protected final int insert(final Connection connection, final String tableName,
                               final String insertString,
                               final String dataFile,
                               final String dateFormat,
                               final Map<String, Integer> insertedColInfo,
                               final Collection<Integer> dateFields) {

        int inserted = 0;
        try {
            // Now do the insertion.
            log.trace("Inserting into {} via {}", tableName, insertString);
            PreparedStatement pstmt = connection.prepareStatement(insertString);
            log.trace("Prepared statement = {}", pstmt.toString());

            try (FileInput instance = new FileInput(dataFile, ",")) {
                final StopWatch sw = new StopWatch();
                sw.start();
                List<String> data = instance.readLine();
                while (data != null && data.size() > 0) {
                    int parameterIndex = 1;
                    for (Map.Entry<String, Integer> entry : insertedColInfo.entrySet()) {
                        if (entry.getValue() == -1) {
                            pstmt.setObject(parameterIndex, null);
                        } else {
                            final String value = data.get(entry.getValue() - 1);
                            if (dateFields.contains(entry.getValue())) {
                                int dateField = Integer.MAX_VALUE;
                                if (value != null && !value.isEmpty()) {
                                    dateField = BroadwickConstants.getDate(value, dateFormat);
                                }
                                pstmt.setObject(parameterIndex, dateField);
                            } else {
                                pstmt.setObject(parameterIndex, value);
                            }
                        }
                        parameterIndex++;
                    }
                    pstmt.addBatch();
                    try {
                        pstmt.executeUpdate();
                        inserted++;
                    } catch (SQLException ex) {
                        if ("23505".equals(ex.getSQLState())) {
                            //Ignore found duplicate from database view
                            continue;
                        } else {
                            log.warn("Duplicate data found for {}: continuing despite errors: {}", data.get(0), ex.getLocalizedMessage());
                            log.trace("{}", Throwables.getStackTraceAsString(ex));
                            throw (ex);
                        }
                    }
                    if (inserted % 250000 == 0) {
                        log.trace("Inserted {} rows in {}", inserted, sw.toString());
                        connection.commit();
                        pstmt.close();
                        pstmt = connection.prepareStatement(insertString);
                    }

                    data = instance.readLine();
                }
                connection.commit();

            } catch (IOException ex) {
                log.error("IO error : {}", ex.getLocalizedMessage());
                log.trace("{}", Throwables.getStackTraceAsString(ex));
            } catch (SQLException ex) {
                log.error("SQL Error : {}", ex.getLocalizedMessage());
                log.trace("{}", Throwables.getStackTraceAsString(ex));
                throw ex;
            } finally {
                pstmt.close();
            }
        } catch (SQLException ex) {
            log.error("{}", ex.getLocalizedMessage());
            log.trace("{}", Throwables.getStackTraceAsString(ex));
            throw new BroadwickException(ex);
        }

        return inserted;
    }

    /**
     * Insert the data from the input file into the database. The data structure has been read and the database set up
     * already so this method simply reads the file and extracts the relevant information, storing it in the database.
     * @return the number of rows read
     */
    public abstract int insert();
}
