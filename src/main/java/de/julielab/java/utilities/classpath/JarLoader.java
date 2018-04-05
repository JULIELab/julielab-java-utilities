package de.julielab.java.utilities.classpath;

import net.bytebuddy.agent.ByteBuddyAgent;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Inspired by https://stackoverflow.com/a/46457506/1314955
 */
public class JarLoader {
    private final static Logger log = LoggerFactory.getLogger(JarLoader.class);

    private static File AGENT_JAR;

    public static void addJarToClassPath(File jarFile) {
        if (AGENT_JAR == null)
            findAgentJar();
        // TODO as soon as we use Java9, replace by: String.valueOf(ProcessHandle.current().pid())
        String processId = java.lang.management.ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
        if (AGENT_JAR != null)
            ByteBuddyAgent.attach(AGENT_JAR, String.valueOf(processId), jarFile.getPath());
    }

    /**
     * Checks the classpath as given by the system property java.class.path for the JAR containing
     * for this very library because it contains the Agent.
     */
    private synchronized static void findAgentJar() {
        String nameOfLibraryJar = "julielab-java-utilities";
        try {
            nameOfLibraryJar = new String(IOUtils.toByteArray(JarLoader.class.getResourceAsStream("/jarname.txt")), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.warn("Loading of the JAR name for the julielab-java-utilities from the file jarname.txt that should" +
                    " be included in the very same JAR failed. It will still be tried to find the JAR from the classpath" +
                    " but it may fail, resulting in the inability to load JARs at runtime.");
        }
        final String finalLibName = nameOfLibraryJar;
        String classpath = System.getProperty("java.class.path");
        String[] classpathEntries = classpath.split(File.pathSeparator);
        Optional<String> jarOpt = Stream.of(classpathEntries).filter(e -> e.contains(finalLibName)).findAny();

        File jarLocation = null;
        if (jarOpt.isPresent())
            jarLocation = new File(jarOpt.get());
        // For testing when working on the JarLoader itself.
        if (jarLocation == null)
            jarLocation = new File("target/" + nameOfLibraryJar);
        if (jarLocation != null)
            AGENT_JAR = jarLocation;
        else
            log.error("Unable to find julielab-java-utilities JAR on the classpath. Runtime loading of JAR files " +
                    "will not work.");
    }
}
