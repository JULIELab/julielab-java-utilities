package de.julielab.java.utilities;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Utilities for the work with commons configuration 2. Note that the dependency for the configuration 2 project
 * is set with scope provided. Thus, the dependency will not automatically be introduced into a project
 * depending on this project. This is done to keep transitive dependencies at a minimum.
 */
public class ConfigurationUtilities {
    public static final String LS = System.getProperty("line.separator");

    public static <T> T requirePresent(String key, Function<String, T> f) throws ConfigurationException {
        T value = f.apply(key);
        if (value == null)
            throw new ConfigurationException("The passed configuration does not have a value for key " + key + ".");
        return value;
    }

    private static void requireNonNull(Configuration configuration) throws ConfigurationException {
        if (configuration == null)
            throw new ConfigurationException("The passed configuration is null.");
    }


    public static void checkParameters(HierarchicalConfiguration<ImmutableNode> importConfig, String... parameters) throws ConfigurationException {
        List<String> parameterNotFound = new ArrayList<>();
        for (String parameter : parameters) {
            if (importConfig.getProperty(parameter) == null)
                parameterNotFound.add(parameter);
        }
        if (!parameterNotFound.isEmpty())
            throw new ConfigurationException("The following required parameters are not set in the configuration:" + LS
                    + parameterNotFound.stream().collect(joining(LS)));
    }

    public static void checkFilesExist(HierarchicalConfiguration<ImmutableNode> importConfig, String... parameters) throws ConfigurationException {
        checkParameters(importConfig, parameters);
        List<String> parameterNotFound = new ArrayList<>();
        for (String parameter : parameters) {
            if (!new File(importConfig.getString(parameter)).exists())
                parameterNotFound.add(parameter);
        }
        if (!parameterNotFound.isEmpty())
            throw new ConfigurationException("The following required files given by the configuration do not exist: " + LS
                    + parameterNotFound.stream().collect(joining(LS)));
    }

    public static XMLConfiguration loadXmlConfiguration(File configurationFile) throws ConfigurationException {
        try {
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<XMLConfiguration> configBuilder =
                    new FileBasedConfigurationBuilder<>(XMLConfiguration.class).configure(params
                            .xml()
                            .setFile(configurationFile));
            return configBuilder.getConfiguration();
        } catch (org.apache.commons.configuration2.ex.ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * Convenience method for quick concatenation of hierarchical configuration keys.
     *
     * @param keys Configuration keys to concatenate into a single hierarchical key.
     * @return The input keys joined with dots.
     */
    public static String dot(String... keys) {
        return Stream.of(keys).collect(Collectors.joining("."));
    }

    /**
     * Convenience method for quick concatenation of hierarchical configuration keys into an XPath expression.
     *
     * @param keys Configuration keys to concatenate into a single hierarchical key.
     * @return The input keys joined with slashes for xpath expressions.
     */
    public static String slash(String... keys) {
        return Stream.of(keys).collect(Collectors.joining("/"));
    }

    public static String ws(String baseElement, String newElement) {
        return baseElement + " " + newElement;
    }

    public static String last(String path) {
        return path + "[last()]";
    }
}
