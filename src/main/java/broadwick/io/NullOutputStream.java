package broadwick.io;

import java.io.BufferedOutputStream;
import java.io.OutputStream;

/**
 * A Null output stream similar to > /dev/null.
 */
public class NullOutputStream extends BufferedOutputStream {
    
     /**
     * Creates a new null output stream to write data to the
     * null output stream.
     */
    public NullOutputStream() {
        super((OutputStream) com.google.common.io.ByteStreams.nullOutputStream());
    }
}
