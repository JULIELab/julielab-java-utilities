package de.julielab.java.utilities;

import org.apache.commons.configuration2.HierarchicalConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.configuration2.tree.ImmutableNode;
import org.junit.jupiter.api.Test;

import java.io.File;

import static de.julielab.java.utilities.ConfigurationUtilities.slash;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ConfigurationUtilitiesTest {

    @Test
    public void attrEqPred() {
        final String s = ConfigurationUtilities.attrEqMultiPred("and", "attr1", "val1", "attr2", "val2", "attr3", "val3");
        assertThat(s).isEqualTo("[@attr1='val1' and @attr2='val2' and @attr3='val3']");
    }

    @Test
    public void attrEq() {
        final String s = ConfigurationUtilities.attrEq("myattr", "theval");
        assertThat(s).isEqualTo("@myattr='theval'");
    }

    @Test
    public void getConfigurationValueWithPredicate() throws ConfigurationException {
        final HierarchicalConfiguration<ImmutableNode> configuration = ConfigurationUtilities.loadXmlConfiguration(new File("src/test/resources/configuration/testconfig.xml"));
        final String value = configuration.getString(slash("level1", "level2" + ConfigurationUtilities.attrEqPred("myatt", "value1")));
        assertEquals("level 2 content", value);

        final String value2 = configuration.getString(slash("level1", "level2" + ConfigurationUtilities.attrEqMultiPred("and", "myatt", "value1", "myatt2", "value2")));
        assertEquals("level 2 content", value2);

        final String value3 = configuration.getString(slash("level1", "level2" + ConfigurationUtilities.attrEqMultiPred("and", "myatt", "value50", "myatt2", "value2")));
        assertNull(value3);

        final String value4 = configuration.getString(slash("level1", "level2" + ConfigurationUtilities.attrEqMultiPred("or", "myatt", "value50", "myatt2", "value2")));
        assertEquals("level 2 content", value4);
    }
}