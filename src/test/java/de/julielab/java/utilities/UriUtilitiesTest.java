package de.julielab.java.utilities;

import org.junit.Test;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;

public class UriUtilitiesTest {

    @Test
    public void testGetInputStreamFromUri() throws URISyntaxException, IOException {
        URL here = new File(".").toURI().toURL();
        URI theFile = new URL(here, "src/test/resources/uriTestContent.txt").toURI();
        try (BufferedInputStream is = UriUtilities.getInputStreamFromUri(theFile)) {
            byte[] bytes = new byte[1000];
            int numread = is.read(bytes);
            assertEquals("Congratulations, you found me!" + System.getProperty("line.separator"), new String(bytes, 0, numread, StandardCharsets.UTF_8));
        }

    }
}
