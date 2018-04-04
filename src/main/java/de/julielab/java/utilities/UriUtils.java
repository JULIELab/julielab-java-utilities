package de.julielab.java.utilities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URI;
import java.util.zip.GZIPInputStream;

public class UriUtils {
    public static BufferedInputStream getInputStreamFromUri(URI uri) throws IOException {
        return new BufferedInputStream(uri.getPath().endsWith(".gz") || uri.getPath().endsWith(".gzip") ?
                new GZIPInputStream(uri.toURL().openStream()) :
                uri.toURL().openStream());
    }

}
