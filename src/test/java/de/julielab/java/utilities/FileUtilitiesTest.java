package de.julielab.java.utilities;

import org.junit.Test;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertTrue;

public class FileUtilitiesTest {
    @Test
    public void testWriteJarFile() throws IOException {
        File outputFile = new File("src/test/resources/test.jar");
        if (outputFile.exists())
            outputFile.delete();
        FileUtilities.createJarFile(outputFile, new File("src/test/resources/jarcontent.txt"), new File("src/test/resources/jarcontentdir"));
        assertTrue(outputFile.exists());
    }
}
