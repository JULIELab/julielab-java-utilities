package de.julielab.java.utilities.index;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class PersistentLuceneStringMapProvider extends PersistentStringIndexMapProvider {
    private final static Logger log = LoggerFactory.getLogger(PersistentLuceneStringMapProvider.class);

    public PersistentLuceneStringMapProvider() {
        super(log);
    }

    @Override
    protected LuceneIndex initializeIndex(String cachePath) {
        return new LuceneIndex(cachePath);
    }

    @Override
    public void close() throws IOException {
        index.close();
    }

    public LuceneIndex getIndex() {
        return (LuceneIndex) index;
    }
}
