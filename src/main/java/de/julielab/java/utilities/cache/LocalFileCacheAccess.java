package de.julielab.java.utilities.cache;

import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class LocalFileCacheAccess<K, V> extends CacheAccess<K, V> {
    private final static Logger log = LoggerFactory.getLogger(LocalFileCacheAccess.class);
    private final CacheService cacheService;
    private final File cacheFile;
    private final Serializer<K> keySerializer;
    private final Serializer<V> valueSerializer;
    private final File cacheDir;

    public LocalFileCacheAccess(String cacheId, String cacheRegion, String keySerializer, String valueSerializer, File cacheDir) {
        super(cacheId, cacheRegion);
        this.keySerializer = getSerializerByName(keySerializer);
        this.valueSerializer = getSerializerByName(valueSerializer);
        this.cacheDir = cacheDir;
        cacheService = CacheService.getInstance();
        cacheFile = new File(getCacheDir(), cacheId);
    }

    private File getCacheDir() {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    @Override
    public V get(K key) {
        return cacheService.getCache(cacheFile, cacheRegion, keySerializer, valueSerializer).get(key);
    }

    @Override
    public void commit() {
        cacheService.commitCache(cacheFile);
    }

    @Override
    public boolean put(K key, V value) {
        if (!cacheService.isDbReadOnly(cacheFile)) {
            cacheService.getCache(cacheFile, cacheRegion, keySerializer, valueSerializer).put(key, value);
            //  cacheService.commitCache(cacheFile);
            return true;
        } else {
            log.debug("Could not write value to cache {} because it is read-only.", cacheFile);
        }
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return cacheService.isDbReadOnly(cacheFile);
    }

}
