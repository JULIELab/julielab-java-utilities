package de.julielab.java.utilities;

import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class StringIteratorInputStreamTest {
    @Test
    public void testInputStream() throws Exception{
        final List<String> strings = List.of("eins", "zwei", "drei", "vier");
        final StringIteratorInputStream is = new StringIteratorInputStream(strings.iterator(), StandardCharsets.UTF_8);
        byte[] buf = new byte[2];
        int read;
        StringBuilder sb = new StringBuilder();
        while ((read = is.read(buf)) != -1) {
            sb.append(new String(buf, 0, read));
        }
        assertEquals("einszweidreivier", sb.toString());
    }
}