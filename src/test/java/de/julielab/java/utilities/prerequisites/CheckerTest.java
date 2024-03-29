package de.julielab.java.utilities.prerequisites;


import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

public class CheckerTest {

    @BeforeAll
    public static void setup() {
        System.setProperty(PrerequisiteChecker.PREREQUISITE_CHECKS_ENABLED, "true");
    }

    @AfterAll
    public static void shutdown() {
        System.setProperty(PrerequisiteChecker.PREREQUISITE_CHECKS_ENABLED, "false");
    }

    @Test
    public void testNullValueCheck() {
        assertThatIllegalArgumentException().isThrownBy(() -> PrerequisiteChecker.checkThat().notEmpty((Collection)null).execute()).withMessageContaining("\"null\" is null");
    }

    @Test
    public void testNullItemCheck() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                PrerequisiteChecker.checkThat().notEmpty(new Collection<?>[]{null}).execute()).withMessageContaining("is null")
        .withMessageContaining("is not a collection");
    }

    @Test
    public void testMixedValid() {
        assertThatCode(() -> PrerequisiteChecker.checkThat().notEmpty(Arrays.asList("hallo")).withNames("meinarray").notNull(new Object()).withNames("MeinObj").execute()).doesNotThrowAnyException();
    }

    @Test
    public void testDetectNull() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                PrerequisiteChecker.checkThat().notNull(null).withNames("MeinObj").execute()
        ).withMessageContaining("\"MeinObj\" is null");
    }

    @Test
    public void testDetectNull2() {
        assertThatIllegalArgumentException().isThrownBy(() ->
                PrerequisiteChecker.checkThat().notNull(null, null).withNames("MeinObj1", "MeinObj2").execute()
        ).withMessageContaining("\"MeinObj1\" is null").withMessageContaining("\"MeinObj2\" is null");
    }

    @Test
    public void testMixedError() {
        assertThatIllegalArgumentException().isThrownBy(() -> PrerequisiteChecker.checkThat().notEmpty(Arrays.asList()).
                withNames("meinarray").notNull(null).withNames("MeinObj").execute()).
                withMessageContaining("meinarray").
                withMessageContaining("is empty").
                withMessageContaining("MeinObj").
                withMessageContaining("is null");
    }

    @Test
    public void testSupplier() {
        assertThatCode(() -> PrerequisiteChecker.checkThat().supplyNotEmpty(() -> Arrays.asList("huhu")).execute()).doesNotThrowAnyException();

    }

    @Test
    public void testSupplier2() {
        assertThatIllegalArgumentException().isThrownBy(() -> PrerequisiteChecker.checkThat().supplyNotEmpty(() -> Arrays.asList()).execute()).withMessageContaining("\"null\" is empty");

    }

    @Test
    public void testSupplier3() {
        assertThatIllegalArgumentException().isThrownBy(() -> PrerequisiteChecker.checkThat().supplyNotEmpty(() -> new HashMap<String, List<String>>().get("nothing").subList(0, 0)).execute()).withMessageContaining("\"null\" caused a NullPointerException");

    }

    @Test
    public void testAllNamesAtTheEnd() {
        assertThatIllegalArgumentException().isThrownBy(() -> PrerequisiteChecker.checkThat().notNull(null).
                notEmpty(Collections.emptySet()).
                withNames("TheOb", "TheColl").execute())
                .withMessageContaining("TheOb")
                .withMessageContaining("is null")
                .withMessageContaining("TheColl")
                .withMessageContaining("is empty");
    }
}
