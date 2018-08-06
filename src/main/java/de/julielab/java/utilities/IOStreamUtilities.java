package de.julielab.java.utilities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class IOStreamUtilities {
    /**
     * Returns a buffered reader with UTF-8 encoding from the given input stream.
     * @param is The input stream to get an UTF-8 reader from.
     * @return A UTF-8 encoding reader.
     */
    public static BufferedReader getReaderFromInputStream(InputStream is) {
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    /**
     * Interprets <tt>is</tt> as a stream of UTF-8 encoded lines and returns all lines.
     * @param is The input stream.
     * @return All UTF-8 encoded lines from the input stream.
     * @throws IOException If reading fails.
     */
    public static List<String> getLinesFromInputStream(InputStream is) throws IOException {
        try (BufferedReader br = getReaderFromInputStream(is)) {
            return br.lines().collect(Collectors.toList());
        }
    }
}
