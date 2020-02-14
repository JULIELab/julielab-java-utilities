package de.julielab.java.utilities.cache;

import org.mapdb.serializer.GroupSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;

import static de.julielab.java.utilities.cache.CacheMapSettings.*;

public class LocalFileCacheAccess<K, V> extends CacheAccess<K, V> {
    private final static Logger log = LoggerFactory.getLogger(LocalFileCacheAccess.class);
    private final CacheService cacheService;
    private final File cacheFile;
    private final GroupSerializer<K> keySerializer;
    private final GroupSerializer<V> valueSerializer;
    private final File cacheDir;
    private final File memCacheName;
    private Map<K, V> cache;

    public LocalFileCacheAccess(String cacheId, String cacheRegion, String keySerializer, String valueSerializer, File cacheDir) {
        this(cacheId, cacheRegion, keySerializer, valueSerializer, cacheDir, 100);
    }

    public LocalFileCacheAccess(String cacheId, String cacheRegion, String keySerializer, String valueSerializer, File cacheDir, CacheMapSettings mapSettings) {
        super(cacheId, cacheRegion);
        this.keySerializer = getSerializerByName(keySerializer);
        this.valueSerializer = getSerializerByName(valueSerializer);
        this.cacheDir = cacheDir;
        cacheService = CacheService.getInstance();
        cacheFile = new File(getCacheDir(), cacheId);

        if (mapSettings.get(MAP_TYPE) == CacheService.CacheMapDataType.HTREE)
            cache = cacheService.getHTreeCache(cacheFile, cacheRegion, this.keySerializer, this.valueSerializer, mapSettings);
        else
            cache = cacheService.getBTreeCache(cacheFile, cacheRegion, this.keySerializer, this.valueSerializer, mapSettings);

        memCacheName = new File(cacheId + ".mem");
        if ((long) mapSettings.get(MEM_CACHE_SIZE) > 0) {
            cache = cacheService.getHTreeCache(memCacheName, cacheId + ".mem", this.keySerializer, this.valueSerializer, new CacheMapSettings(MAX_SIZE, mapSettings.get(MEM_CACHE_SIZE), OVERFLOW_DB, cache));
        }
    }

    public LocalFileCacheAccess(String cacheId, String cacheRegion, String keySerializer, String valueSerializer, File cacheDir, long memCacheSize) {
        this(cacheId, cacheRegion, keySerializer, valueSerializer, cacheDir, new CacheMapSettings(MEM_CACHE_SIZE, memCacheSize));
    }

    private File getCacheDir() {
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        return cacheDir;
    }

    @Override
    public V get(K key) {
        return cache.get(key);
    }

    @Override
    public void commit() {
        cacheService.commitCache(cacheFile);
    }

    @Override
    public boolean put(K key, V value) {
        if (!cacheService.isDbReadOnly(cacheFile)) {
            cache.put(key, value);
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
