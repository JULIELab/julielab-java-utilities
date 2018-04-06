package de.julielab.java.utilities;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;

public class UriUtilities {
    public static BufferedInputStream getInputStreamFromUri(URI uri) throws IOException {
        return new BufferedInputStream(uri.getPath().endsWith(".gz") || uri.getPath().endsWith(".gzip") ?
                new GZIPInputStream(uri.toURL().openStream()) :
                uri.toURL().openStream());
    }

    public static BufferedReader getReaderFromUri(URI uri) throws IOException {
      return new BufferedReader(new InputStreamReader(getInputStreamFromUri(uri), StandardCharsets.UTF_8));
    }

}
