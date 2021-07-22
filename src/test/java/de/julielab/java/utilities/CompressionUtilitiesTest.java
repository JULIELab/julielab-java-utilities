package de.julielab.java.utilities;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class CompressionUtilitiesTest {

    @BeforeAll
    @AfterAll
    public static void clean() throws IOException {
        File to = new File("src/test/resources/mockDir");
        if (to.exists())
            FileUtils.deleteDirectory(to);
    }

    @Test
    public void testExtract() throws IOException {
        File from = new File("src/test/resources/mockarchive.tgz");
        File to = new File("src/test/resources/");
        File extract = CompressionUtilities.extract(from, to, false);
        assertEquals("mockdir", extract.getName());
    }

}
