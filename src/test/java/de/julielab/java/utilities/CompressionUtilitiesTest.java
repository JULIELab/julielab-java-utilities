package de.julielab.java.utilities;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.rauschig.jarchivelib.ArchiveEntry;
import org.rauschig.jarchivelib.ArchiveStream;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CompressionUtilitiesTest {

    @BeforeClass
    @AfterClass
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
    public void testExtractStreams() throws IOException {
        File from = new File("src/test/resources/mockarchive.tgz");
        final Archiver archiver = ArchiverFactory.createArchiver(from);
        final ArchiveStream stream = archiver.stream(from);
        while (stream.getNextEntry() != null) {
            final ArchiveEntry e = stream.getCurrentEntry();
            System.out.println(e.getName());
            e.extract(new File("tmp/"));
           // System.out.println(IOStreamUtilities.getStringFromInputStream(stream));
        }


        final Iterator<InputStream> streams = CompressionUtilities.getArchiveEntryInputStreams(from);
        assertTrue("The iterator is empty", streams.hasNext());
        while (streams.hasNext()) {
            System.out.println("hier");
            InputStream next = streams.next();
            System.out.println(IOStreamUtilities.getStringFromInputStream(next));
        }
    }
}
