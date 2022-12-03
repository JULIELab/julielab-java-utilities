package de.julielab.java.utilities.index;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

public class LuceneIndexTest {

    @Test
    public void put() throws Exception {
        final Path cachePath = Path.of("src", "test", "resources", "mypath", "mysubdir");
        FileUtils.deleteQuietly(cachePath.toFile());
        final StringIndex stringIndex = new LuceneIndex(cachePath.toString());
        stringIndex.put("key5", new String[]{"myvalue1", "myvalue2"});
        if (stringIndex.requiresExplicitCommit())
            stringIndex.commit();
        stringIndex.open();
        assertThat(stringIndex.size()).isEqualTo(1);
        assertThat(stringIndex.getArray("key5")).containsExactly("myvalue1", "myvalue2");
        stringIndex.close();
    }

    @Test
    public void putSameKeyMultipleTimes() throws Exception {
        final Path cachePath = Path.of("src", "test", "resources", "mypath", "mysubdir");
        FileUtils.deleteQuietly(cachePath.toFile());
        final LuceneIndex stringIndex = new LuceneIndex(cachePath.toString());
        stringIndex.setRetrieveAllKeys(true);
        stringIndex.put("key5", new String[]{"myvalue1", "myvalue2"});
        stringIndex.put("key5", new String[]{"myvalue3"});
        if (stringIndex.requiresExplicitCommit())
            stringIndex.commit();
        stringIndex.open();
        assertThat(stringIndex.size()).isEqualTo(2);
        assertThat(stringIndex.getArray("key5")).containsExactly("myvalue1", "myvalue2", "myvalue3");
        stringIndex.close();
    }
}
