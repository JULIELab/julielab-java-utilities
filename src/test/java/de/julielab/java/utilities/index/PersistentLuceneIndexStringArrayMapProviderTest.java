package de.julielab.java.utilities.index;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PersistentLuceneIndexStringArrayMapProviderTest {
    @Test
    public void get() throws Exception {
        try (final PersistentLuceneIndexStringArrayMapProvider mapProvider = new PersistentLuceneIndexStringArrayMapProvider()) {
            final Path cachePath = Path.of("src", "test", "resources", "mypath");
            FileUtils.deleteQuietly(cachePath.toFile());
            mapProvider.load(URI.create("file:src/test/resources/stringArrayMapFile.txt"));
            final Map<String, String[]> map = mapProvider.getMap();
            assertThat(map).hasSize(4);
            assertThat(map.get("doesnotexist")).isNull();
            assertThat(map.get("key1")).containsExactly("value11");
            assertThat(map.get("key2")).containsExactly("value21", "value22");
            assertThat(map.get("key3")).containsExactly("value31", "value32", "value33");
            assertThat(map.get("key4")).containsExactly("value41", "value42", "value43", "value44");
        }
    }

    @Test
    public void getMultikey() throws Exception {
        try (final PersistentLuceneIndexStringArrayMapProvider mapProvider = new PersistentLuceneIndexStringArrayMapProvider()) {
            final Path cachePath = Path.of("src", "test", "resources", "mypath");
            mapProvider.setIndexDirectoryPath(cachePath);
            FileUtils.deleteQuietly(cachePath.toFile());
            mapProvider.load(URI.create("file:src/test/resources/stringArrayMapFileMultiKeyField.txt"));
            final Map<String, String[]> map = mapProvider.getMap();
            assertThat(map).hasSize(4);
            assertThat(map.get("key1")).containsExactly("value11");
            assertThat(map.get("key2")).containsExactly("value11");
            assertThat(map.get("key3")).containsExactly("value21", "value22");
            assertThat(map.get("key4")).containsExactly("value21", "value22");
        }
    }

    @Test
    public void getRepeatedkey() throws Exception {
        // The file contains the same key in multiple lines
        try (final PersistentLuceneIndexStringArrayMapProvider mapProvider = new PersistentLuceneIndexStringArrayMapProvider()) {
            final Path cachePath = Path.of("src", "test", "resources", "mypath");
            mapProvider.setIndexDirectoryPath(cachePath);
            FileUtils.deleteQuietly(cachePath.toFile());
            mapProvider.load(URI.create("file:src/test/resources/stringArrayMapFileRepeatedKeys.txt"));
            final Map<String, String[]> map = mapProvider.getMap();
            assertThat(map.size()).isEqualTo(7);
            assertThat(map.get("key1")).containsExactly("value11", "value31", "value41");
            assertThat(map.get("key2")).containsExactly("value11");
            assertThat(map.get("key3")).containsExactly("value21", "value22");
            assertThat(map.get("key4")).containsExactly("value21", "value22", "value51", "value61");
        }
    }

    @Test
    public void readIDMapping() throws Exception {
        // The file contains the same key in multiple lines
        try (final PersistentLuceneIndexStringArrayMapProvider mapProvider = new PersistentLuceneIndexStringArrayMapProvider()) {
            mapProvider.setKeyIndices(2);
            mapProvider.setValueIndices(0, 1);
            final Path cachePath = Path.of("src", "test", "resources", "mypath");
            mapProvider.setIndexDirectoryPath(cachePath);
            FileUtils.deleteQuietly(cachePath.toFile());
            mapProvider.load(URI.create("file:src/test/resources/idmapping_snippet.txt"));
            final Map<String, String[]> map = mapProvider.getMap();
            assertThat(map.get("2947774")).containsExactly("Q6GZX3", "002L_FRG3G");
        }
    }
}
