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
package broadwick.data.readers;

import broadwick.BroadwickException;
import broadwick.config.generated.CustomTags;
import broadwick.config.generated.DataFiles;
import broadwick.data.DatabaseImpl;
import com.google.common.base.Throwables;
import java.sql.Connection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reader for the files describing the directed movements (i.e. those that only have a location and a movement_ON or
 * movement_OFF flag)for the simulation. The movements are read and stored in internal databases for later use.
 */
@Slf4j
public class DirectedMovementsFileReader extends DataFileReader {

    /**
     * Create the movement file reader.
     * @param movementFile the [XML] tag of the config file that is to be read.
     * @param dbImpl       the implementation of the database object.
     */
    public DirectedMovementsFileReader(final DataFiles.DirectedMovementFile movementFile,
                                       final DatabaseImpl dbImpl) {
        super();
        this.database = dbImpl;
        this.dataFile = movementFile.getName();
        this.dateFields = new HashSet<>();
        this.dateFormat = movementFile.getDateFormat();
        this.insertedColInfo = new TreeMap<>();
        final StringBuilder errors = new StringBuilder();

        this.createTableCommand = new StringBuilder();
        updateCreateTableCommand(ID, movementFile.getIdColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(MOVEMENT_DATE, movementFile.getMovementDateColumn(), " INT, ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(MOVEMENT_DIRECTION, movementFile.getMovementDirectionColumn(), " VARCHAR(32), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(LOCATION_ID, movementFile.getLocationColumn(), " VARCHAR(32), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(SPECIES, movementFile.getSpeciesColumn(), " VARCHAR(32), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);

        if (movementFile.getCustomTags() != null) {
            for (CustomTags.CustomTag tag : movementFile.getCustomTags().getCustomTag()) {
                updateCreateTableCommand(tag.getName(), tag.getColumn(), " VARCHAR(128), ",
                                         insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
                if ("date".equals(tag.getType())) {
                    dateFields.add(tag.getColumn());
                }
            }
        }

        createTableCommand.deleteCharAt(createTableCommand.length() - 1);
        createTableCommand.append(");");

        if (movementFile.getMovementDateColumn() > 0) {
            dateFields.add(movementFile.getMovementDateColumn());
        }

        final StringBuilder createIndexCommand = new StringBuilder();
        createIndexCommand.append(String.format(" CREATE INDEX IF NOT EXISTS IDX_DIR_MVMT_ID ON %s (%s);",
                                                TABLE_NAME, ID));
        createIndexCommand.append(String.format(" CREATE INDEX IF NOT EXISTS IDX_DIR_MVMT_ALL ON %s (%s,%s,%s,%s);",
                                                TABLE_NAME, ID, LOCATION_ID, MOVEMENT_DATE, MOVEMENT_DIRECTION));

        createTableCommand.append(createIndexCommand.toString());

        insertString = String.format("INSERT INTO %s (%s) VALUES (%s)",
                                     TABLE_NAME,
                                     asCsv(insertedColInfo.keySet()), asQuestionCsv(insertedColInfo.keySet()));

        if (errors.length() > 0) {
            log.error(errors.toString());
            throw new BroadwickException(errors.toString());
        }
    }

    /**
     * Insert the data from the input file into the database. The data structure has been read and the database set up
     * already so this method simply reads the file and extracts the relevant information, storing it in the database.
     * @return the number of rows read
     */
    @Override
    public final int insert() {
        log.trace("DirectedMovementsFileReader insert");

        int inserted = 0;
         try (Connection connection = database.getConnection()) {  
         createTable(TABLE_NAME, createTableCommand.toString(), connection);

         inserted = insert(connection, TABLE_NAME, insertString, dataFile, dateFormat,  insertedColInfo, dateFields);
         } catch (Exception ex) {
            log.error("{}", ex.getLocalizedMessage());
            log.error("Error reading movement data. {}", Throwables.getStackTraceAsString(ex));
            throw new BroadwickException(ex);
        }
        return inserted;
    }

    private DatabaseImpl database;
    private String dataFile;
    private String dateFormat;
    @Getter
    private static final String TABLE_NAME = "DirectedMovements";
    private StringBuilder createTableCommand;
    private String insertString;
    private Map<String, Integer> insertedColInfo;
    private Collection<Integer> dateFields;
    @Getter
    private static final String ID = "ID";
    @Getter
    private static final String SPECIES = "SPECIES";
    @Getter
    private static final String LOCATION_ID = "LOCATIONID";
    @Getter
    private static final String MOVEMENT_DATE = "MOVEMENTDATE";
    @Getter
    private static final String MOVEMENT_DIRECTION = "MOVEMENTDIRECTION";
    private static final String SECTION_NAME = "DirectedMovementFile";
}
