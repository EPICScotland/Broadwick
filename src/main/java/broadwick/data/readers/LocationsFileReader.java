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
 * Reader for the files describing the locations for the simulation.
 */
@Slf4j
public class LocationsFileReader extends DataFileReader {

    /**
     * Create the locations file reader.
     * @param locationsFile the [xml] tag of the config file that is to be read.
     * @param dbImpl        the implementation of the database object.
     */
    public LocationsFileReader(final DataFiles.LocationsFile locationsFile,
                               final DatabaseImpl dbImpl) {

        super();
        this.database = dbImpl;
        this.dataFile = locationsFile.getName();
        this.dateFields = new HashSet<>();
        this.dateFormat = locationsFile.getDateFormat();
        this.insertedColInfo = new TreeMap<>();
        final StringBuilder errors = new StringBuilder();

        this.createTableCommand = new StringBuilder();
        updateCreateTableCommand(ID, locationsFile.getLocationIdColumn(), " VARCHAR(128) NOT NULL, ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(EASTING, locationsFile.getEastingColumn(), " DOUBLE, ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(NORTHING, locationsFile.getNorthingColumn(), " DOUBLE, ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);

        if (locationsFile.getCustomTags() != null) {
            for (final CustomTags.CustomTag tag : locationsFile.getCustomTags().getCustomTag()) {
                updateCreateTableCommand(tag.getName(), tag.getColumn(), " VARCHAR(128), ",
                                         insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
                if ("date".equals(tag.getType())) {
                    dateFields.add(tag.getColumn());
                }
            }
        }

        final StringBuilder createIndexCommand = new StringBuilder();

        createTableCommand.deleteCharAt(createTableCommand.length() - 1);
        createTableCommand.append(");");

        createIndexCommand.append(String.format(" CREATE INDEX IF NOT EXISTS IDX_LOC_ID ON %s (%s);",
                                                TABLE_NAME, ID));
        createIndexCommand.append(String.format(" CREATE INDEX IF NOT EXISTS IDX_LOC_ALL ON %s (%s,%s,%s);",
                                                TABLE_NAME, ID, EASTING, NORTHING));

        createTableCommand.append(createIndexCommand.toString());

        insertString = String.format("INSERT INTO %s (%s) VALUES (%s)",
                                     TABLE_NAME, asCsv(insertedColInfo.keySet()), asQuestionCsv(insertedColInfo.keySet()));

        if (errors.length() > 0) {
            log.error(errors.toString());
            throw new BroadwickException(errors.toString());
        }
    }

    @Override
    public final int insert() {
        log.trace("LocationsFileReader insert");

        int inserted = 0;
        try (Connection connection = database.getConnection()) {
            createTable(TABLE_NAME, createTableCommand.toString(), connection);

            inserted = insert(connection, TABLE_NAME, insertString, dataFile, dateFormat, insertedColInfo, dateFields);
        } catch (Exception ex) {
            log.error("{}", ex.getLocalizedMessage());
            log.error("Error reading location data. {}", Throwables.getStackTraceAsString(ex));
            throw new BroadwickException(ex);
        }
        return inserted;
    }

    private DatabaseImpl database;
    private String dataFile;
    private String dateFormat;
    private StringBuilder createTableCommand;
    private String insertString;
    private Map<String, Integer> insertedColInfo;
    private Collection<Integer> dateFields;
    @Getter
    private static final String TABLE_NAME = "LOCATIONS";
    @Getter
    private static final String ID = "ID";
    @Getter
    private static final String EASTING = "EASTING";
    @Getter
    private static final String NORTHING = "NORTHING";
    private static final String SECTION_NAME = "LocationsFile";
}
