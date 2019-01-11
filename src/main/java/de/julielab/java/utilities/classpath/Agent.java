package de.julielab.java.utilities.classpath;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * Taken from https://stackoverflow.com/a/46457506/1314955
 */
public class Agent {

    private final static Logger log = LoggerFactory.getLogger(Agent.class);

    public static void agentmain(String args, Instrumentation instrumentation) throws IOException {
        if (!new File(args).isDirectory()) {
            instrumentation.appendToSystemClassLoaderSearch(new JarFile(args));
        } else {
            log.debug("Not adding {} to the classpath because it is a directory but only JAR files can be added to the classpath at runtime.", args);
        }
    }
}
