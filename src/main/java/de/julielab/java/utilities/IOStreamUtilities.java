package de.julielab.java.utilities;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;

public class IOStreamUtilities {
    /**
     * Returns a buffered reader with UTF-8 encoding from the given input stream.
     *
     * @param is The input stream to get an UTF-8 reader from.
     * @return A UTF-8 encoding reader.
     */
    public static BufferedReader getReaderFromInputStream(InputStream is) {
        return new BufferedReader(new InputStreamReader(is, UTF_8));
    }

    /**
     * Interprets <tt>is</tt> as a stream of UTF-8 encoded lines and returns all lines.
     *
     * @param is The input stream.
     * @return All UTF-8 encoded lines from the input stream.
     * @throws IOException If reading fails.
     */
    public static List<String> getLinesFromInputStream(InputStream is) throws IOException {
        try (BufferedReader br = getReaderFromInputStream(is)) {
            return br.lines().collect(Collectors.toList());
        }
    }


    /**
     * <p>
     * Converts an <tt>InputStream</tt> into a single <tt>String</tt> using a <tt>ByteArrayOutputStream</tt>
     * as a buffer.
     * </p>
     * <p>Inspired by https://stackoverflow.com/a/48775964/1314955.</p>
     *
     * @param is The <tt>InputStream</tt> to convert into a <tt>String</tt>.
     * @return The string contents of <tt>is</tt>.
     * @throws IOException If reading fails.
     */
    public static String getStringFromInputStream(InputStream is) throws IOException {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }

            return result.toString(UTF_8);
        } finally {
            is.close();
        }
    }
}
