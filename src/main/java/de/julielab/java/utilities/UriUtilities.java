package de.julielab.java.utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipException;

public class UriUtilities {

    private final static Logger log = LoggerFactory.getLogger(UriUtilities.class);

    public static BufferedInputStream getInputStreamFromUri(URI uri) throws IOException {
        try {
            return new BufferedInputStream(uri.getPath().endsWith(".gz") || uri.getPath().endsWith(".gzip") ?
                    new GZIPInputStream(uri.toURL().openStream()) :
                    uri.toURL().openStream());
        } catch (ZipException e) {
            log.error("URI {} target could not be uncompressed: {}", uri, e.getMessage());
            throw e;
        }
    }

    public static BufferedReader getReaderFromUri(URI uri) throws IOException {
      return new BufferedReader(new InputStreamReader(getInputStreamFromUri(uri), StandardCharsets.UTF_8));
    }

}
