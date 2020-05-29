package de.julielab.java.utilities;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.io.FileBased;
import org.apache.commons.configuration2.io.FileHandler;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.apache.commons.configuration2.tree.xpath.XPathExpressionEngine;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;

/**
 * Utilities for the work with commons configuration 2. Note that the dependency for the configuration 2 project
 * is set with scope provided. Thus, the dependency will not automatically be introduced into a project
 * depending on this project. This is done to keep transitive dependencies at a minimum.
 */
public class ConfigurationUtilities {
    public static final String LS = System.getProperty("line.separator");

    /**
     * <p>
     * This method is meant to check if a configuration has a value for the given <tt>key</tt> It throws an Exception if not..
     * </p>
     * <p>To this end, the function <tt>f</tt> should have the form <tt>key -> configuration.getString(key)</tt>. Getters for other types can also be used.</p>
     *
     * @param key The configuration key to check for a value.
     * @param f   The function that does the value checking.
     * @param <T> The datatype of the expected configuration value.
     * @return The configuration value if it exists.
     * @throws ConfigurationException If the value does not exist.
     */
    public static <T> T requirePresent(String key, Function<String, T> f) throws ConfigurationException {
        T value = f.apply(key);
        if (value == null)
            throw new ConfigurationException("The passed configuration does not have a value for key " + key + ".");
        return value;
    }


    /**
     * <p>Checks if the configuration keys given with <tt>parameters</tt> are defined in the passed configuration.</p>
     * @param configuration The configuration to check for existing keys.
     * @param parameters The keys to check.
     * @throws ConfigurationException If a key was not found, i.e. does not have a value (the empty string is a value).
     */
    public static void checkParameters(HierarchicalConfiguration<ImmutableNode> configuration, String... parameters) throws ConfigurationException {
        List<String> parameterNotFound = new ArrayList<>();
        for (String parameter : parameters) {
            if (configuration.getProperty(parameter) == null)
                parameterNotFound.add(parameter);
        }
        if (!parameterNotFound.isEmpty())
            throw new ConfigurationException("The following required parameters are not set in the configuration:" + LS
                    + parameterNotFound.stream().collect(joining(LS)));
    }

    /**
     * <p>Meant for configuration values denoting files.
     * Checks if the configuration keys given with <tt>parameters</tt> exist in the passed configuration and makes sure that their values point to existing files.</p>
     * <p>If a parameter is not defined our its value does not point to an existing file, an Exception with a explaining message will be raised.</p>
     *
     * @param configuration The config to check for keys and file-path-values.
     * @param parameters    The configuration keys whose values should point to files.
     * @throws ConfigurationException If a key does not exist in <tt>configuration</tt> or its value does not point to an existing file.
     */
    public static void checkFilesExist(HierarchicalConfiguration<ImmutableNode> configuration, String... parameters) throws ConfigurationException {
        checkParameters(configuration, parameters);
        List<String> parameterNotFound = new ArrayList<>();
        for (String parameter : parameters) {
            if (!new File(configuration.getString(parameter)).exists())
                parameterNotFound.add(parameter);
        }
        if (!parameterNotFound.isEmpty())
            throw new ConfigurationException("The following required files given by the configuration do not exist: " + LS
                    + parameterNotFound.stream().collect(joining(LS)));
    }

    /**
     * Loads the Apache Commons Configuration2 {@link XMLConfiguration} from the given file. By default,
     * the {@link XPathExpressionEngine} is set.
     *
     * @param configurationFile
     * @return
     * @throws ConfigurationException
     */
    public static XMLConfiguration loadXmlConfiguration(File configurationFile) throws ConfigurationException {
        try {
            Parameters params = new Parameters();
            FileBasedConfigurationBuilder<XMLConfiguration> configBuilder =
                    new FileBasedConfigurationBuilder<>(XMLConfiguration.class).configure(params
                            .xml()
                            .setExpressionEngine(new XPathExpressionEngine())
                            .setEncoding(StandardCharsets.UTF_8.name())
                            .setFile(configurationFile));
            return configBuilder.getConfiguration();
        } catch (ConfigurationException e) {
            throw new ConfigurationException(e);
        }
    }

    /**
     * This default implementation constructs an empty {@link XMLConfiguration} with an {@link XPathExpressionEngine}
     * and a UTF-8 encoding. Note that when using this template generation method, the keys of the configuration
     * must be given in XPath form, i.e. 'key/subkey' instead of the default dotted notation 'key.subkey'.
     * @return An empty XMLConfiguration template.
     * @throws ConfigurationException If the template generation fails.
     */
    public static HierarchicalConfiguration<ImmutableNode> createEmptyConfiguration() throws ConfigurationException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<XMLConfiguration> builder =
                new FileBasedConfigurationBuilder<>(XMLConfiguration.class).configure(
                        params.xml()
                        .setExpressionEngine(new XPathExpressionEngine())
                        .setEncoding(StandardCharsets.UTF_8.name()));
        XMLConfiguration c;
        try {
            c = builder.getConfiguration();
        } catch (ConfigurationException e) {
            throw new ConfigurationException();
        }
        return c;
    }

    /**
     * Generates the configuration template by calling {@link #createEmptyConfiguration()} and stores the
     * template to <tt>destination</tt>.
     * @param destination The file path where the template should be written to.
     * @throws ConfigurationException It template generation of population fails.
     */
    public static void writeConfiguration(HierarchicalConfiguration<ImmutableNode> configuration, File destination) throws ConfigurationException {
        try {
            if (!(configuration instanceof FileBased))
                throw new ConfigurationException("The created configuration cannot be stored to file " +
                        "because the chosen configuration implementation " + configuration.getClass().getCanonicalName() + " " +
                        "does not implement the " + FileBased.class.getCanonicalName() + " interface");
            FileHandler fh = new FileHandler((FileBased) configuration);
            fh.save(destination);
        } catch (ConfigurationException e) {
            throw new ConfigurationException();
        }
    }

    /**
     * Convenience method for quick concatenation of hierarchical configuration keys. When using
     * Apache Commons Configuration, the {@link org.apache.commons.configuration2.tree.DefaultExpressionEngine} uses
     * dots as configuration path key separation values by default.
     *
     * @param keys Configuration keys to concatenate into a single hierarchical key.
     * @return The input keys joined with dots.
     */
    public static String dot(String... keys) {
        return Stream.of(keys).collect(joining("."));
    }

    /**
     * Convenience method for quick concatenation of hierarchical configuration keys into an XPath expression.
     *
     * @param keys Configuration keys to concatenate into a single hierarchical key.
     * @return The input keys joined with slashes for xpath expressions.
     */
    public static String slash(String... keys) {
        return Stream.of(keys).collect(joining("/"));
    }

    /**
     * Concatenates the input elements with a whitespace.
     *
     * @param baseElement The left element.
     * @param newElement  The right element.
     * @return The whitespace-concatenated elements.
     */
    public static String ws(String baseElement, String newElement) {
        return baseElement + " " + newElement;
    }

    /**
     * Appends the <tt>[last()] predicate to the given XPath.</tt>
     *
     * @param path
     * @return
     */
    public static String last(String path) {
        return path + "[last()]";
    }

    /**
     * <p>Creates expressions in the form <tt>[attr='value']</tt> to be used in predicate XPath expressions.</p>
     *
     * @param attribute The attribute name.
     * @param value The value to check the attribute for.
     * @return The XPath predicate expression.
     * @see #attrEqMultiPred(String, String...)
     */
    public static String attrEqPred(String attribute, String value) {
        return attrEqMultiPred("", attribute, value);
    }

    /**
     * <p>Creates expressions in the form <tt>element[attr='value' AND attr2='value2']</tt> to be used in predicate XPath expressions.</p>
     * <p>Results in the selection of elements fulfulling the specified predicate.</p>
     *
     * @param element             The name of element to apply the predicate to.
     * @param attributesAndValues A sequence of attribute names and the values they are matched against, beginning with an attribute name.
     * @return The XPath predicate expression.
     * @see #attrEqMultiPred(String, String...)
     */
    public static String elementEqMultiPred(String element, String... attributesAndValues) {
        return element + attrEqMultiPred("", attributesAndValues);
    }

    /**
     * <p>Creates expressions in the form <tt>element[attr='value']</tt> to be used in predicate XPath expressions.</p>
     *
     * @param element
     * @param attribute The attribute name.
     * @param value     The value to check the attribute for.
     * @return The XPath predicate expression.
     * @see #attrEqMultiPred(String, String...)
     */
    public static String elementEqPred(String element, String attribute, String value) {
        return element + attrEqMultiPred("", attribute, value);
    }

    /**
     * <p>Creates expressions in the form <tt>[attr='value' AND attr2='value2']</tt> to be used in predicate XPath expressions.</p>
     *
     * @param operator            'and' or 'or', case sensitive.
     * @param attributesAndValues A sequence of attribute names and the values they are matched against, beginning with an attribute name.
     * @return The XPath predicate expression.
     */
    public static String attrEqMultiPred(String operator, String... attributesAndValues) {
        if (attributesAndValues.length % 2 == 1)
            throw new IllegalStateException("There is an uneven number of arguments.");
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < attributesAndValues.length; i++) {
            String attributesAndValue = attributesAndValues[i];
            if (i % 2 == 0) {
                sb.append(attrEq(attributesAndValue, attributesAndValues[i + 1]));
                if (i + 2 < attributesAndValues.length)
                    sb.append(" ").append(operator).append(" ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    /**
     * Creates the string <tt>attr='value'</tt> to be used in a predicate. Note that this is not yet an XPath
     * predicate expression due to the missing parenthesis.
     *
     * @param attribute The attribute.
     * @param value     The value.
     * @return The attribute-equals-value XPath expression.
     * @see #attrEq(String, String)
     * @see #attrEqMultiPred(String, String...)
     */
    public static String attrEq(String attribute, String value) {
        return attribute + "='" + value + "'";
    }
}
