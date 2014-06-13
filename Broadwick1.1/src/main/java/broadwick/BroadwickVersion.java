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
package broadwick;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import lombok.extern.slf4j.Slf4j;

/**
 * This is a simple class to obtain the version number of the project from the manifest file of the packaged jar.
 * To locate the version and build information of the framework, we locate the jar in which this object resides and
 * interrogate the Manifest.MF file of that jar for the build info. This approach depends very much on how the jar 
 * file is used, for example, if the project using the jar in which this framework is delivered uses one-jar (and the 
 * maven plug-in top use this) then the framework jar is contained neatly with the other dependent jars. Using the
 * maven assembly plug-in though expands the dependent jar files (and overwrites the manifest files and any other 
 * files with duplicate names it finds) so that at best the manifest file that is found will be the one of the project
 * using the framework and it is that build info that will be displayed and NOT the frameworks.
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
     * most recent commit to the SCM and adds it as the implementation number to the jars manifest.
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
            // First find the jar that contains this object.
            final String className = version.getClass().getSimpleName() + ".class";
            final String classPath = version.getClass().getResource(className).toString();
            final String manifestPath = classPath.substring(0, classPath.lastIndexOf('!') + 1) + "/META-INF/MANIFEST.MF";

            if (classPath.startsWith("jar")) {

                // Find the manifest path in the list of resources in the current jar file.
                final Enumeration<URL> allManifestFilesInJar = version.getClass().getClassLoader().getResources(java.util.jar.JarFile.MANIFEST_NAME);
                while (allManifestFilesInJar.hasMoreElements()) {
                    final URL element = allManifestFilesInJar.nextElement();

                    if (element.toString().equals(manifestPath)) {
                        manifest = new Manifest(element.openStream());
                        break;
                    }
                }
            }
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
     * <code>Build-timestamp</code> If the manifest file is NOT the one for the framework (i.e. a model that utilises
     * the framework) then it will report the versions of both.
     * @return a string containing the version and timestamp.
     */
    public static String getVersionAndTimeStamp() {
        try {
            final Manifest manifest = getManifest();
            final StringBuilder sb = new StringBuilder();
            final Attributes attr = manifest.getMainAttributes();

            sb.append(String.format("Version %s ", attr.getValue(IMPL_VERSION)));
            sb.append(String.format("Build (%s - %s : %s) ", InetAddress.getLocalHost().getHostName(),
                                    attr.getValue(IMPL_BUILD), attr.getValue(BUILD_TIMESTAMP)));
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
