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
    public static BufferedReader getReaderFromInputStream(InputStream is) {
        return new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
    }

    public static List<String> getLinesFromInputStream(InputStream is) throws IOException {
        try (BufferedReader br = getReaderFromInputStream(is)) {
            return br.lines().collect(Collectors.toList());
        }
    }
}
