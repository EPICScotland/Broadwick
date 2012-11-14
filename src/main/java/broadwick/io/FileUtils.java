package broadwick.io;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Simple utilities for file objects.
 */
public final class FileUtils {

    /**
     * Constructor; hidden.
     */
    private FileUtils() {
        // hiding utility constructor
    }

    /**
     * Change the extension of a file name, e.g. filename = changeExtension("data.txt", ".java") will assign "data.java"
     * to filename.
     * @param source       the name of the file whose extension we wish to change
     * @param newExtension the new extension.
     * @return the new filename with modified extension.
     */
    public static String renameFileExtension(final String source, final String newExtension) {
        String target;
        final String currentExtension = getFileExtension(source);

        if (currentExtension.isEmpty()) {
            target = source + DOT + newExtension;
        } else {
            target = source.replaceFirst(Pattern.quote(DOT + currentExtension) + "$", Matcher.quoteReplacement(DOT + newExtension));

        }
        return target;
    }

    /**
     * Get the extension of a file, given the name of the file.
     * @param filename the name of the file.
     * @return the extension of the file.
     */
    public static String getFileExtension(final String filename) {
        String ext = "";
        final int i = filename.lastIndexOf(DOT);
        if (i > 0 && i < filename.length() - 1) {
            ext = filename.substring(i + 1);
        }
        return ext;
    }

    private static final String DOT = ".";
}
