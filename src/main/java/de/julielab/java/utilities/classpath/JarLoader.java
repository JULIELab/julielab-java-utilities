package de.julielab.java.utilities.classpath;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import de.julielab.java.utilities.IOStreamUtilities;
import net.bytebuddy.agent.ByteBuddyAgent;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.management.Agent;
import sun.tools.jar.resources.jar;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This class adds JAR files to the classpath at runtime. There are two different ways chosen depending on the
 * JVM version, up to 8 or beginning with 9. The Java 9 approach requires an instrumentation agent which is
 * located at {@link de.julielab.java.utilities.classpath.Agent}. The agent must be included in a JAR that
 * registers it by the Agent-Class property of its MANIFEST.MF file.
 * This class can either used inside the julielab-java-utilities JAR file directly, which has the Agent-Class
 * property set for its manifest. When building a fat JAR, containing this class, the fat JAR must
 * have the property itself.
 */
public class JarLoader {
    private final static Logger log = LoggerFactory.getLogger(JarLoader.class);

    private static File AGENT_JAR;

    public static void addJarToClassPath(File jarFile) {
        if (isJavaVersionAbove8()) {
            // Inspired by https://stackoverflow.com/a/46457506/1314955
            if (AGENT_JAR == null)
                findAgentJar();
            // TODO as soon as we use Java9, replace by: String.valueOf(ProcessHandle.current().pid())
            String processId = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0];

            if (AGENT_JAR != null)
                ByteBuddyAgent.attach(AGENT_JAR, String.valueOf(processId), jarFile.getPath());
        } else {
            try {
                loadLibraryWithURLClassloader(jarFile);
            } catch (Exception e) {
                log.error("Could not add {} to the classloader: {}", jarFile, e);
            }
        }
    }

    /**
     * https://stackoverflow.com/questions/27187566/load-jar-dynamically-at-runtime
     * <p>
     * WARNING: This only works until Java9. Beginning with Java9, the system classloader is not an URLClassLoader
     * any more.
     * Adds the supplied Java Archive library to java.class.path. This is benign
     * if the library is already loaded.
     */
    public static synchronized void loadLibraryWithURLClassloader(java.io.File jar) throws Exception {
        try {
            /*We are using reflection here to circumvent encapsulation; addURL is not public*/
            java.net.URLClassLoader loader = (java.net.URLClassLoader) ClassLoader.getSystemClassLoader();
            java.net.URL url = jar.toURI().toURL();
            /*Disallow if already loaded*/
            for (java.net.URL it : java.util.Arrays.asList(loader.getURLs())) {
                if (it.equals(url)) {
                    return;
                }
            }
            java.lang.reflect.Method method = java.net.URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{java.net.URL.class});
            method.setAccessible(true); /*promote the method to public access*/
            method.invoke(loader, new Object[]{url});
        } catch (final java.lang.NoSuchMethodException |
                java.lang.IllegalAccessException |
                java.net.MalformedURLException |
                java.lang.reflect.InvocationTargetException e) {
            throw new Exception(e);
        }
    }

    private static boolean isJavaVersionAbove8() {
        String version = System.getProperty("java.version");
        if (version.startsWith("1")) {
            // Version format up to Java 8, e.g. 1.7.0
            return false;
        } else {
            // Version format beginning at Java 9, the major version stands in front
            return true;
        }
    }

    /**
     * Checks the classpath as given by the system property java.class.path for the JAR containing
     * for this very library because it contains the Agent.
     */
    private synchronized static void findAgentJar() {
        File jarLocation = null;
        if (System.getProperty("de.julielab.jarloader.agent") != null) {
            File propFile = new File(System.getProperty("de.julielab.jarloader.agent"));
            if (propFile.exists())
                jarLocation = propFile;
        }

        String nameOfLibraryJar = "julielab-java-utilities";
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);
        if (jarLocation == null) {
            try {
                nameOfLibraryJar = new String(IOUtils.toByteArray(JarLoader.class.getResourceAsStream("/jarname.txt")), StandardCharsets.UTF_8);
            } catch (IOException e) {
                log.warn("Loading of the JAR name for the julielab-java-utilities from the file jarname.txt that should" +
                        " be included in the very same JAR failed. It will still be tried to find the JAR from the classpath" +
                        " but it may fail, resulting in the inability to load JARs at runtime.");
            }
            final String finalLibName = nameOfLibraryJar;
            Optional<String> jarOpt = Stream.of(classpathEntries).filter(e -> e.contains(finalLibName)).findAny();

            if (jarOpt.isPresent())
                jarLocation = new File(jarOpt.get());
        }
        // For testing when working on the JarLoader itself.
        if (jarLocation == null) {
            jarLocation = new File("target/" + nameOfLibraryJar);
            if (!jarLocation.exists())
                jarLocation = null;
        }

        if (jarLocation == null) {
            // Try to find out the name of the containing JAR using the protection domain.
            // This will work most of the time but might not in security critical environments.
            try {
                File jarFilePath = new File(JarLoader.class.getProtectionDomain()
                        .getCodeSource()
                        .getLocation().toURI());
                if (checkForAgentClassInJarManifest(jarFilePath))
                    jarLocation = jarLocation;
            } catch (URISyntaxException | SecurityException e) {
                log.debug("Exception when trying to resolve the JAR location for the JarLoader Agent " +
                        "using the protection domain.", e);
            }
        }
        if (jarLocation == null && classpathEntries.length == 1 && classpathEntries[0].endsWith(".jar")) {
            // an only JAR on the classpath points towards a single uber-JAR; then we won't find the
            // julielab-java-utilities since they will be contained in the uber-JAR. Just try to use the JAR
            // itself
            File jarFilePath = new File(classpathEntries[0]);
            if (checkForAgentClassInJarManifest(jarFilePath))
                jarLocation = jarLocation;
        }

        if (jarLocation != null && jarLocation.exists())
            AGENT_JAR = jarLocation;
        else
            log.error("Unable to find julielab-java-utilities JAR or the correct Agent-Class manifest entry " +
                    "on the classpath. Runtime loading of JAR files " +
                    "will not work.");
    }

    private static boolean checkForAgentClassInJarManifest(File jarFilePath) {
        try (ZipFile zipFile = new ZipFile(jarFilePath)) {

            ZipEntry zipEntry = zipFile.getEntry("META-INF/MANIFEST.MF");

            if (zipEntry != null) {
                List<String> manifestLines = IOStreamUtilities.getLinesFromInputStream(zipFile.getInputStream(zipEntry));
                Optional<String> agentLine = manifestLines.stream().filter(line -> line.contains("Agent-Class:") && line.contains(Agent.class.getCanonicalName())).findAny();
                if (agentLine.isPresent())
                    return true;
            }
        } catch (Exception e) {
            log.debug("Exception when trying to resolve the JAR location for the JarLoader Agent " +
                    "using the single JAR file on the classpath.", e);
        }
        log.debug("There is only a single JAR file on the classpath indicating that the current " +
                "program is packaged as one single JAR. However, the manifest of this JAR does not " +
                "specify the Agent required for runtime classpath additions. Add the entry " +
                "'Agent-Class: " + Agent.class.getCanonicalName() + "' to the manifest to enable the ability " +
                "to add elements to the classpath on runtime.");
        return false;
    }
}
