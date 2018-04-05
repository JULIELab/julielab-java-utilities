package de.julielab.java.utilities.classpath;

import java.io.IOException;
import java.lang.instrument.Instrumentation;
import java.util.jar.JarFile;

/**
 * Taken from https://stackoverflow.com/a/46457506/1314955
 */
public class Agent {
    public static void agentmain(String args, Instrumentation instrumentation) throws IOException {
        instrumentation.appendToSystemClassLoaderSearch(new JarFile(args));
    }
}
