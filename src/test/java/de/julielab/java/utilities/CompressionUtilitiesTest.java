package de.julielab.java.utilities;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.Assert.assertEquals;

public class CompressionUtilitiesTest {

    @BeforeClass
    @After
    public static void clean() throws IOException {
        File to = new File("src/test/resources/");
        if (to.exists())
            FileUtils.deleteDirectory(to);
    }

    @Test
    public void testExtract() throws IOException {
        File from = new File("src/test/resources/mockarchive.tgz");
        File to = new File("src/test/resources/");
        File extract = CompressionUtilities.extract(from, to, false);
        assertEquals("mockDir", extract);
    }
}
