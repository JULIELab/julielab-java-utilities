package de.julielab.java.utilities;

import org.rauschig.jarchivelib.ArchiveEntry;
import org.rauschig.jarchivelib.ArchiveStream;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

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
        if ((entry = archiveStream.getNextEntry()) != null) {
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
}
