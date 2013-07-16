package broadwick.io;

import broadwick.BroadwickVersion;
import com.google.common.base.Throwables;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;

/**
 * Simple interface to printing values to a file. It simply wraps the java io classes to simplify the output of data.
 */
@Slf4j
public class FileOutput implements AutoCloseable {
    
    
    /**
     * Create a null file output object similar to writing to /dev/null. This allows for empty FileOutput objects
     * to be used without throwing NullPointerExceptions.
     */
    public FileOutput() {
        buffer = new NullOutputStream();
    }

    /**
     * Create a file with the given name.
     * @param dataFileName the name of the file.
     */
    public FileOutput(final String dataFileName) {
        this(dataFileName, false, DEFAULT_ENCODING);
    }

    /**
     * Create a file with the given name.
     * @param dataFileName the name of the file.
     * @param addVersion   boolean that determines whether or not the project version
     *                     number should be applied to the start of the file.
     */
    public FileOutput(final String dataFileName, final boolean addVersion) {
        this(dataFileName, addVersion, DEFAULT_ENCODING);
    }

    /**
     * Create a file with the given name.
     * @param dataFileName the name of the file.
     * @param addVersion   boolean that determines whether or not the project version
     *                     number should be applied to the start of the file.
     * @param encoding     the character encoding used in the file.
     */
    public FileOutput(final String dataFileName, final boolean addVersion, final String encoding) {
        try {
            fileEncoding = encoding;
            buffer = new BufferedOutputStream(new FileOutputStream(dataFileName));
            if (addVersion) {
                buffer.write("# Version : ".getBytes(fileEncoding));
                buffer.write(BroadwickVersion.getVersionAndTimeStamp().getBytes(fileEncoding));
                buffer.write("\n".getBytes(fileEncoding));
                buffer.flush();
            }
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
            throw new BroadwickIOException(ex.getLocalizedMessage() + Throwables.getStackTraceAsString(ex));
        }
    }

    /**
     * Write the string to the file stream.
     * @param str the string to write to the stream.
     */
    public final void write(final String str) {
        try {
            buffer.write(str.getBytes(fileEncoding));
            buffer.flush();
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
            throw new BroadwickIOException(ex.getLocalizedMessage() + Throwables.getStackTraceAsString(ex));
        }
    }

    /**
     * Writes a formatted string to the file stream.
     * @param format A format string as described in <a href="#syntax">Format string syntax</a>
     * @param args   Arguments referenced by the format specifiers in the format string. If there are more arguments
     *               than format specifiers, the extra
     *               arguments are ignored. The maximum number of arguments is limited by the maximum dimension of a
     *               Java array as defined by the <a
     *               href="http://java.sun.com/docs/books/vmspec/">Java Virtual Machine Specification</a>
     * @return the string written to the buffer.
     */
    public final String write(final String format, final Object... args) {
        String str = "";
        try {
            str = String.format(format, args);
            buffer.write(str.getBytes(fileEncoding));
            buffer.flush();
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
            throw new BroadwickIOException(ex.getLocalizedMessage() + Throwables.getStackTraceAsString(ex));
        }
        return str;
    }

    @Override
    public final void close() {
        try {
            buffer.close();
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
    }

    /**
     * Flushes the file stream.
     */
    public final void flush() {
        try {
            buffer.flush();
        } catch (IOException ex) {
            log.error(ex.getLocalizedMessage());
        }
    }

    /**
     * Save data in the form of a string to a named file.
     * @param dataFileName the name of the file in which the data is to be saved.
     * @param data         the data to be saved.
     * @param addVersion   add the project version number to the start of the file.
     */
    public static void saveToFile(final String dataFileName, final String data, final boolean addVersion) {
        try (FileOutput fl = new FileOutput(dataFileName, addVersion)) {
            fl.write(data);
            // we can let any thrown BroadIOException get caught further up the stack.
        }
    }

    /**
     * Save data in the form of a string to a named file.
     * @param dataFileName the name of the file in which the data is to be saved.
     * @param format       A format string as described in <a href="#syntax">Format string syntax</a>
     * @param addVersion   add the project version number to the start of the file.
     * @param args         Arguments referenced by the format specifiers in the format string. If there are more
     *                     arguments than format specifiers, the extra
     *                     arguments are ignored. The maximum number of arguments is limited by the maximum dimension
     *                     of
     *                     a Java array as defined by the <a
     *                     href="http://java.sun.com/docs/books/vmspec/">Java Virtual Machine Specification</a>
     */
    public static void saveToFile(final String dataFileName, final String format, final boolean addVersion, final Object... args) {
        try (FileOutput fl = new FileOutput(dataFileName, addVersion)) {
            fl.write(format, args);
            // we can let any thrown BroadIOException get caught further up the stack.
        }
    }

    private BufferedOutputStream buffer;
    private String fileEncoding;
    private static final String DEFAULT_ENCODING = "UTF-8";

}
