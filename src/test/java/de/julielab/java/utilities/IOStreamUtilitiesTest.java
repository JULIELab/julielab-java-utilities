package de.julielab.java.utilities;


import org.junit.jupiter.api.Test;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class IOStreamUtilitiesTest {
    @Test
    public void testGetStringFromInputStream() throws Exception {
        final InputStream resource = FileUtilities.findResource("/uriTestContent.txt");
        final String content = IOStreamUtilities.getStringFromInputStream(resource);
        assertEquals("Congratulations, you found me!\n", content);
    }
}
