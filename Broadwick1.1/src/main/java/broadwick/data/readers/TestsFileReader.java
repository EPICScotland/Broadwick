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
 * Read a file containing the results of tests.
 */
@Slf4j
public class TestsFileReader extends DataFileReader {

    /**
     * Create the tests file reader.
     * @param testsFile the [xml] tag of the config file that is to be read.
     * @param dbImpl    the implementation of the database object.
     */
    public TestsFileReader(final DataFiles.TestsFile testsFile,
                           final DatabaseImpl dbImpl) {

        super();
        this.database = dbImpl;
        this.dataFile = testsFile.getName();
        this.dateFields = new HashSet<>();
        this.dateFormat = testsFile.getDateFormat();
        this.insertedColInfo = new TreeMap<>();
        final StringBuilder errors = new StringBuilder();

        this.createTableCommand = new StringBuilder();
        updateCreateTableCommand(ID, testsFile.getIdColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(GROUP_ID, testsFile.getGroupIdColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(LOCATION_ID, testsFile.getLocationIdColumn(), " VARCHAR(128), ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(TEST_DATE, testsFile.getTestDateColumn(), " INT, ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(POSITIVE_RESULT, testsFile.getPostiveResultColumn(), " INT, ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
        updateCreateTableCommand(NEGATIVE_RESULT, testsFile.getNegativeResultColumn(), " INT, ",
                                 insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);

        if (testsFile.getTestDateColumn() > 0) {
            dateFields.add(testsFile.getTestDateColumn());
        }

        if (testsFile.getCustomTags() != null) {
            for (CustomTags.CustomTag tag : testsFile.getCustomTags().getCustomTag()) {
                updateCreateTableCommand(tag.getName(), tag.getColumn(), " VARCHAR(128), ",
                                         insertedColInfo, createTableCommand, TABLE_NAME, SECTION_NAME, errors);
                if ("date".equals(tag.getType())) {
                    dateFields.add(tag.getColumn());
                }
            }
        }

        final StringBuilder createIndexCommand = new StringBuilder();
        String primaryKeyId = ID;
        if (testsFile.getIdColumn() != null) {
            primaryKeyId = ID;
        } else if (testsFile.getGroupIdColumn() != null) {
            primaryKeyId = GROUP_ID;
        } else if (testsFile.getLocationIdColumn() != null) {
            primaryKeyId = LOCATION_ID;
        }
//        // we arent adding a primary key. 
//        createTableCommand.append(String.format("PRIMARY KEY (%s,%s,%s,%s) );", primaryKeyId, TEST_DATE, POSITIVE_RESULT, NEGATIVE_RESULT)); 

        createTableCommand.deleteCharAt(createTableCommand.length() - 1);
        createTableCommand.append(");");

        createIndexCommand.append(String.format(" CREATE INDEX IF NOT EXISTS IDX_TEST_ID ON %s (%s);",
                                                TABLE_NAME, primaryKeyId));
        createIndexCommand.append(String.format(" CREATE INDEX IF NOT EXISTS IDX_TEST_POS ON %s (%s,%s,%s);",
                                                TABLE_NAME, primaryKeyId, TEST_DATE, POSITIVE_RESULT));
        createIndexCommand.append(String.format(" CREATE INDEX IF NOT EXISTS IDX_TEST_ALL ON %s (%s,%s,%s,%s);",
                                                TABLE_NAME, primaryKeyId, TEST_DATE, POSITIVE_RESULT, NEGATIVE_RESULT));

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
        log.trace("TestsFileReader insert");

        int inserted = 0;
        try (Connection connection = database.getConnection()) {
            createTable(TABLE_NAME, createTableCommand.toString(), connection);

            inserted = insert(connection, TABLE_NAME, insertString, dataFile, dateFormat, insertedColInfo, dateFields);
        } catch (Exception ex) {
            log.error("Error reading test data {}. {}", ex.getLocalizedMessage(), Throwables.getStackTraceAsString(ex));
            throw new BroadwickException(ex);
        }
        return inserted;
    }

    private DatabaseImpl database;
    private String dataFile;
    private String dateFormat;
    @Getter
    private static final String TABLE_NAME = "Tests";
    private StringBuilder createTableCommand;
    private String insertString;
    private Map<String, Integer> insertedColInfo;
    private Collection<Integer> dateFields;
    @Getter
    private static final String ID = "ID";
    @Getter
    private static final String GROUP_ID = "GROUPID";
    @Getter
    private static final String LOCATION_ID = "LOCATIONID";
    @Getter
    private static final String TEST_DATE = "TESTDATE";
    @Getter
    private static final String POSITIVE_RESULT = "POSITIVERESULT";
    @Getter
    private static final String NEGATIVE_RESULT = "NEGATIVERESULT";
    private static final String SECTION_NAME = "TestsFile";
}
