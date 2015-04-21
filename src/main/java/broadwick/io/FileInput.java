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
package broadwick.io;

import com.google.common.base.Splitter;
import com.google.common.base.Throwables;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple interface for reading from a file. It simply wraps the java io classes to simplify the input of data.
 */
@Slf4j
public class FileInput implements AutoCloseable {

    /**
     * Open a handle to a file with the given name.
     * @param dataFileName the name of the file.
     * @throws IOException if the resource can't be found.
     */
    public FileInput(final String dataFileName) throws IOException {
        this(dataFileName, DEFAULT_SEP, DEFAULT_ENCODING);
    }

    /**
     * Open a handle to a file with the given name.
     * @param dataFileName the name of the file.
     * @param sep          The single char for the separator (not a list of separator characters).
     * @throws IOException if the resource can't be found.
     */
    public FileInput(final String dataFileName, final String sep) throws IOException {
        this(dataFileName, sep, DEFAULT_ENCODING);
    }

    /**
     * Open a handle to a file with the given name.
     * @param dataFileName the name of the file.
     * @param sep          The single char for the separator (not a list of separator characters).
     * @param encoding     the encoding used in the file e.g. UTF-8.
     * @throws IOException if the resource can't be found.
     */
    public FileInput(final String dataFileName, final String sep, final Charset encoding) throws IOException {
        fieldSep = Pattern.compile(sep);
        fileEncoding = encoding;
        path = Paths.get(dataFileName.replace(" ", "\\ "));
        reader = Files.newBufferedReader(path, fileEncoding);
    }

    /**
     * Read a line from the input file, split it according to the seperator specified in the constructor and get a list
     * of tokens from the line. Comment characters (#) are supported, where any character after the comment character is
     * not added to the collection of tokens.
     * @return a list of entries in the line.
     * @throws IOException if a line cannot be read, e.g if the object was closed.
     */
    public final List<String> readLine() throws IOException {

        List<String> tokens = new LinkedList<>();
        try {
            String line = reader.readLine();
            // we've not reached the end of the file, 
            while (line != null && (line.isEmpty() || line.charAt(0) == '#')) {
                // read until the next non-empty line
                line = reader.readLine();
                if (line == null) {
                    return tokens;
                }
            }

            if (line != null && !line.isEmpty()) {
                tokens = tokeniseLine(line);
            }

        } catch (IOException e) {
            final StringBuilder sb = new StringBuilder("Unable to read from ");
            sb.append(path.getFileName()).append(", Reason : ").append(e.getLocalizedMessage());
            log.error(sb.toString());
            sb.append("\n").append(Throwables.getStackTraceAsString(e));
            throw new IOException(sb.toString());
        }
        return tokens;
    }

    /**
     * Get the next (non-comment) line from the file.
     * @return a string of the next non-comment line in the file.
     * @throws IOException if a line cannot be read, e.g if the object was closed.
     */
    public final String readNextLine() throws IOException {

        String line = reader.readLine();
        try {
            // we've not reached the end of the file, 
            while (line != null && (line.isEmpty() || line.charAt(0) == '#')) {
                line = reader.readLine();
            }
        } catch (IOException e) {
            final StringBuilder sb = new StringBuilder("Unable to read from ");
            sb.append(path.getFileName()).append(", Reason : ").append(e.getLocalizedMessage());
            log.error(sb.toString());
            sb.append("\n").append(Throwables.getStackTraceAsString(e));
            throw new IOException(sb.toString());
        }
        if (line != null) {
            line = line.trim();
        }
        return line;
    }

    /**
     * Read the contents of the file into a single string object.
     * @return the contents of the file.
     * @throws IOException if a line cannot be read, e.g if the object was closed.
     */
    public final String read() throws IOException {
        final StringBuilder sb = new StringBuilder();

        String line = reader.readLine();
        while (line != null) {
            sb.append(line).append("\n");
            line = reader.readLine();
        }
        return sb.toString();
    }

    /**
     * Split a string (a line read from a file into tokens.
     * @param line the line that is to be tokenised.
     * @return a list of [string] tokens.
     */
    private List<String> tokeniseLine(final String line) {
        final List<String> tokens = new LinkedList<>();
        if (line != null && !line.isEmpty()) {
            String trimmedLine = line.trim();
            final int indexOfCommentchar = trimmedLine.indexOf(COMMENT_CHAR);

            if (indexOfCommentchar > 0) {
                trimmedLine = trimmedLine.substring(0, indexOfCommentchar);
            }

            for (String token : Splitter.on(fieldSep).trimResults().split(trimmedLine)) {
                tokens.add(token.trim());
            }

        }
        return tokens;
    }

    @Override
    public final void close() {
        try {
            reader.close();
        } catch (IOException ex) {
            log.error("Failed to close file {}.", path.getFileName());
        }
    }
    
    /**
     * Obtain an iterator for this input file.
     * @return the FileInputIterator for this file.
     */
    public final FileInputIterator iterator() {
        // the reader object will be instantiated by now so we should not have a NullPointerException.
        return new FileInputIterator(this);
    }
    
    private Path path;
    protected BufferedReader reader;
    private Pattern fieldSep;
    private Charset fileEncoding;
    private static final String DEFAULT_SEP = "[\\s,]";
    private static final Charset DEFAULT_ENCODING = StandardCharsets.UTF_8;
    private static final char COMMENT_CHAR = '#';

}
