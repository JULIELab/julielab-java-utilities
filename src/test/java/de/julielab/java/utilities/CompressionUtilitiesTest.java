package de.julielab.java.utilities;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.rauschig.jarchivelib.ArchiveEntry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


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

    @Test
    public void testGetArchiveInputStreams() throws IOException {
        File archive = new File("src/test/resources/mockarchive.tgz");

        Iterator<Pair<ArchiveEntry, InputStream>> streams = CompressionUtilities.getArchiveEntryInputStreams(archive);
        boolean filefound = false;
        while (streams.hasNext()) {
            Pair<ArchiveEntry, InputStream> stream = streams.next();
            // the File object is used to easily get the actual file name since ArchiveEntry.getName() is the whole
            // path, including directories
            if (!stream.getLeft().isDirectory() && !new File(stream.getLeft().getName()).getName().startsWith(".")) {
                String entryContent = IOStreamUtilities.getStringFromInputStream(stream.getRight());
                assertEquals("Just a mock file.", entryContent.trim());
                filefound = true;
            }
        }
        assertTrue(filefound);
    }

}
