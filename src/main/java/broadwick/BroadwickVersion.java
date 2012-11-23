package broadwick;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a simple class to obtain the version number of the project from SCM.
 */
@Slf4j
public final class BroadwickVersion {

    static {
        version = new BroadwickVersion();
    }

    /**
     * Private constructor to provide a singleton instance.
     */
    private BroadwickVersion() {
    }

    /**
     * Get the implementation number defined in the Manifest.MF of the jar. The build process get the label/tag of the
     * most recent commit to the SCM
     * and adds it as the implementation number to the jars manifest.
     * @return the project version number that is obtained from the SCM.
     */
    public static String getImplementationVersion() {
        return BroadwickVersion.getImplementationVersion(getManifest());
    }

    /**
     * Get the build time in the Manifest.MF of the jar. The build process adds the time of the build to the jars
     * manifest.
     * @return the project build timestamp that is obtained from the SCM.
     */
    public static String getImplementationTimeStamp() {
        return BroadwickVersion.getImplementationTimeStamp(getManifest());
    }

    /**
     * Get the object corresponding to the Manifest.mf file.
     * @return the object for the Manifest file.
     */
    private static Manifest getManifest() {
        Manifest manifest = null;
        try {
            final String className = version.getClass().getSimpleName() + ".class";
            final String classPath = version.getClass().getResource(className).toString();
            if (!classPath.startsWith("jar")) {
                return null;
            }
            final String manifestPath = classPath.substring(0, classPath.lastIndexOf('!') + 1) + "/META-INF/MANIFEST.MF";
            manifest = new Manifest(new URL(manifestPath).openStream());
        } catch (IOException ex) {
            log.error("Cannot read manifest file, {}", ex.getLocalizedMessage());
        }
        return manifest;
    }

    /**
     * Get the build timestamp from the supplied Manifest.mf file.
     * @param manifest the manifest file containing the timestamp info.
     * @return a string containing the project build timestamp.
     */
    private static String getImplementationTimeStamp(final Manifest manifest) {
        final StringBuilder sb = new StringBuilder();
        final Attributes attr = manifest.getMainAttributes();

        sb.append(attr.getValue(BUILD_TIMESTAMP));

        return sb.toString();
    }

    /**
     * Get the build version number from the supplied Manifest.mf file..
     * @param manifest the manifest file containing the version info.
     * @return a string containing the project version.
     */
    private static String getImplementationVersion(final Manifest manifest) {
        final StringBuilder sb = new StringBuilder();
        final Attributes attr = manifest.getMainAttributes();

        sb.append(attr.getValue(IMPL_VERSION));
        sb.append(String.format(BUILD_STRING_FORMAT, attr.getValue(IMPL_BUILD)));

        return sb.toString();
    }

    /**
     * Get the build version number and timestamp. This gets the following attributes in the manifest file
     * <code>Implementation-Version</code>
     * <code>Implementation-Build</code>
     * <code>Build-timestamp</code>
     * If the manifest file is NOT the one for the framework (i.e. a model that utilises the framework) then
     * it will report the versions of both.
     * @return a string containing the version and timestamp.
     */
    public static String getVersionAndTimeStamp() {
        try {
            final Manifest manifest = getManifest();
            final StringBuilder sb = new StringBuilder();
            final Attributes attr = manifest.getMainAttributes();

            //TODO this will get the info form the main manifest file.
            // HOWEVER, this framework is packaged as an executable jar and when added to a project
            // the manifest is overwritten so the build information for the framework is lost - this needs to
            // be fixed

            sb.append(String.format("Version %s ", attr.getValue(IMPL_VERSION)) );
            sb.append(String.format("Build (%s - %s : %s) ", InetAddress.getLocalHost().getHostName(),
                    attr.getValue(IMPL_BUILD), attr.getValue(BUILD_TIMESTAMP)) );
            return sb.toString();
        } catch (Exception e) {
            return "UNKNOWN VERSION";
        }
    }
    private static BroadwickVersion version;
    private static final String BUILD_STRING_FORMAT = " : build %s ";
    private static final String IMPL_VERSION = "Implementation-Version";
    private static final String IMPL_BUILD = "Implementation-Build";
    private static final String BUILD_TIMESTAMP = "Build-timestamp";
}
