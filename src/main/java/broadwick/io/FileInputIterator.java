package broadwick.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import lombok.extern.slf4j.Slf4j;

/**
 * Iterator object for a FileInput object.
 */
@Slf4j
public class FileInputIterator implements Iterator<String> {

    /**
     * Create a iterator over the contents of a file.
     * @param fileInput the FileInput object over which the iterator should run.
     */
    public FileInputIterator(final FileInput fileInput) {
        this.fileInput = fileInput;
        this.reader = fileInput.reader;
    }

    @Override
    public final boolean hasNext() {
        try {
            return reader.ready();
        } catch (IOException ioe) {
            log.error("Could not determine if FileInputReader hasNext()? {}", ioe.getLocalizedMessage());
            return false;
        }

    }

    @Override
    public final String next() {
        try {
            return fileInput.readNextLine();
        } catch (IOException ioe) {
            log.error("Could not get next from FileInputIterator. {}", ioe.getLocalizedMessage());
            return null;
        }
    }

    @Override
    public final void remove() {
        throw new UnsupportedOperationException("FileInputIterator.remove() is not supported");
    }
    private FileInput fileInput;
    private BufferedReader reader;
}
