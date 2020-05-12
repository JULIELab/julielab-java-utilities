package de.julielab.java.utilities;

import org.rauschig.jarchivelib.ArchiveEntry;
import org.rauschig.jarchivelib.ArchiveStream;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Utility class to work with compressed files. Note that the dependency for the jarchivelib project
 * is set with scope provided. Thus, the dependency will not automatically be introduced into a project
 * depending on this project. This is done to keep transitive dependencies at a minimum.
 */
public class CompressionUtilities {
    private final static Logger log = LoggerFactory.getLogger(CompressionUtilities.class);

    /**
     * Extracts the given file into the given directory. Returns a file to the extraction point of the
     * first archive entry. This is for convenience if the archive contains a directory for quick access. The archive
     * format is automatically determined by the underlying (de)compression library.
     *
     * @param from          An archive file.
     * @param to            The directory to extract the archive to.
     * @param deleteArchive Whether the archive file should be deleted after extraction.
     * @return The path to the extracted first entry of the archive.
     * @throws IOException If the archive cannot be read or files cannot be created or deleted.
     */
    public static File extract(File from, File to, boolean deleteArchive) throws IOException {
        // the parameter here does only serve as the information about what kind of archiver we need (ZIP, tgz, ...)
        Archiver archiver = ArchiverFactory.createArchiver(from);
        log.debug("Extracting archive {} to {}", from, to);
        archiver.extract(from, to);
        ArchiveStream archiveStream = archiver.stream(from);
        ArchiveEntry entry;
        String firstEntryName = null;
        while ((entry = archiveStream.getNextEntry()) != null && firstEntryName == null) {
            if (entry.isDirectory()) {
                firstEntryName = entry.getName();
            }
        }
        File firstEntryFile = new File(to.getAbsolutePath() + File.separator + firstEntryName);
        if (deleteArchive) {
            log.debug("Deleting archive file {}", from);
            if (!from.delete())
                throw new IOException("Could not delete the archive at " + from.getAbsolutePath());
        }
        return firstEntryFile;
    }

    public static Iterator<InputStream> getArchiveEntryInputStreams(File archive) throws IOException {
        final Archiver archiver = ArchiverFactory.createArchiver(archive);
        final ArchiveStream stream = archiver.stream(archive);
        return new Iterator<InputStream>() {
            private ArchiveEntry currentEntry;
            boolean exhausted = false;
            @Override
            public boolean hasNext() {
                if (currentEntry == null && !exhausted) {
                    try {
                        currentEntry = stream.getNextEntry();
                        while (currentEntry != null && currentEntry.isDirectory())
                            currentEntry = stream.getNextEntry();
                        if (currentEntry == null)
                            exhausted = true;
                    } catch (IOException e) {
                        log.error("Could not get next archive entry", e);
                    }
                }
                return !exhausted;
            }

            @Override
            public InputStream next() {
                if (hasNext()) {
                    currentEntry = null;
                    return stream;
                }

                return null;
            }
        };
    }
}
