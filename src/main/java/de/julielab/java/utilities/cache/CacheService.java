package de.julielab.java.utilities.cache;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.julielab.java.utilities.cache.CacheService.CacheType.LOCAL;

public class CacheService {
    private final static Logger log = LoggerFactory.getLogger(CacheService.class);
    /**
     * <p>Java system property name to enable or disable caching. No value is interpreted as 'true'.</p>
     * <p>This property can be used to disable caching. When set to 'false', all methods returning an
     * instance of {@link CacheAccess} will return the implementation {@link NoOpCacheAccess}. This
     * implementation caches nothing and always returns null for {@link CacheAccess#get(Object)}.</p>
     * <p>If caching is disabled, the call to {@link #initialize(CacheConfiguration)} is no longer
     * necessary.</p>
     */
    public static final String CACHING_ENABLED_PROP = "de.julielab.java.utilities.cache.enabled";
    private static CacheService service;
    private Map<String, DB> dbs = new HashMap<>();
    private Set<String> readOnly = new HashSet<>();
    private CacheConfiguration configuration;

    private CacheService(CacheConfiguration configuration) {
        this.configuration = configuration;
    }

    public synchronized static CacheService getInstance() {
        String propertyValue = System.getProperty(CACHING_ENABLED_PROP);
        boolean cachingEnabled = propertyValue == null || Boolean.parseBoolean(propertyValue);
        if (cachingEnabled && service == null)
            throw new IllegalStateException("Call 'initialize' before acquiring an instance of the CacheService.");
        else if (!cachingEnabled) {
            service = new CacheService(null);
            return service;
        }
        return service;
    }

    public synchronized static void initialize(CacheConfiguration configuration) {
        if (service == null)
            service = new CacheService(configuration);

    }

    /**
     * <p>This is the method to acquire an actual cache object.</p>
     * <p>Calling this method results in the creation of or the opening of a concrete cache file. The file
     * will be created in the directory given by {@link CacheConfiguration#getLocalCacheDir()} or the
     * respective parameter when starting the cache server in case of a remote cache.</p>
     *
     * @param cacheId             An arbitrary name that names the resulting cache file.
     * @param cacheRegion         An arbitrary name of a region in within the given cacheId.
     * @param keySerializerName   One of {@link CacheAccess#STRING}, {@link CacheAccess#JAVA}, {@link CacheAccess#BYTEARRAY} or {@link CacheAccess#DOUBLEARRAY}.
     * @param valueSerializerName One of {@link CacheAccess#STRING}, {@link CacheAccess#JAVA}, {@link CacheAccess#BYTEARRAY} or {@link CacheAccess#DOUBLEARRAY}.
     * @param <K>                 The cache key type.
     * @param <V>                 The cache value type.
     * @return An object granting access to the requested cache.
     */
    public <K, V> CacheAccess<K, V> getCacheAccess(String cacheId, String cacheRegion, String keySerializerName, String valueSerializerName) {
        return getCacheAccess(cacheId, cacheRegion, keySerializerName, valueSerializerName, 0);
    }

    /**
     * <p>This is the method to acquire an actual cache object.</p>
     * <p>Calling this method results in the creation of or the opening of a concrete cache file. The file
     * will be created in the directory given by {@link CacheConfiguration#getLocalCacheDir()} or the
     * respective parameter when starting the cache server in case of a remote cache.</p>
     *
     * @param cacheId             An arbitrary name that names the resulting cache file.
     * @param cacheRegion         An arbitrary name of a region in within the given cacheId.
     * @param keySerializerName   One of {@link CacheAccess#STRING}, {@link CacheAccess#JAVA}, {@link CacheAccess#BYTEARRAY} or {@link CacheAccess#DOUBLEARRAY}.
     * @param valueSerializerName One of {@link CacheAccess#STRING}, {@link CacheAccess#JAVA}, {@link CacheAccess#BYTEARRAY} or {@link CacheAccess#DOUBLEARRAY}.
     * @param memCacheSize        The size of the in-memory cache buffer.
     * @param <K>                 The cache key type.
     * @param <V>                 The cache value type.
     * @return An object granting access to the requested cache.
     */
    public <K, V> CacheAccess<K, V> getCacheAccess(String cacheId, String cacheRegion, String keySerializerName, String valueSerializerName, int memCacheSize) {
        String propertyValue = System.getProperty(CACHING_ENABLED_PROP);
        if (propertyValue != null && !Boolean.parseBoolean(propertyValue))
            return new NoOpCacheAccess<>(cacheId, cacheRegion);
        switch (configuration.getCacheType()) {
            case LOCAL:
                return new LocalFileCacheAccess<>(cacheId, cacheRegion, keySerializerName, valueSerializerName, configuration.getLocalCacheDir(), memCacheSize);
            case REMOTE:
                return new RemoteCacheAccess<>(cacheId, cacheRegion, keySerializerName, valueSerializerName, configuration.getRemoteCacheHost(), configuration.getRemoteCachePort(), memCacheSize);
            default:
                throw new IllegalArgumentException("Unknown cache type '" + configuration.getCacheType() + "' in the configuration.");
        }
    }

    boolean isDbReadOnly(File file) {
        try {
            return readOnly.contains(file.getCanonicalPath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    synchronized <K, V> HTreeMap<K, V> getCache(File dbFile, String regionName, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        final DB filedb = getFiledb(dbFile);
        final DB.HashMapMaker<K, V> dbmaker = filedb.hashMap(regionName).keySerializer(keySerializer).valueSerializer(valueSerializer);
        if (isDbReadOnly(dbFile))
            return dbmaker.open();
        return dbmaker.
                createOrOpen();
    }

    synchronized void commitCache(File dbFile) {
        if (!isDbReadOnly(dbFile))
            getFiledb(dbFile).commit();
        else
            log.debug("Cannot commit cache {} because it is read-only.", dbFile);
    }

    public void commitAllCaches() {
        dbs.values().forEach(db -> db.commit());
    }

    private synchronized DB getFiledb(File cacheDir) {
        try {
            DB db = dbs.get(cacheDir.getCanonicalPath());
            if (db == null) {
                final DBMaker.Maker dbmaker = DBMaker
                        .fileDB(cacheDir.getAbsolutePath())
                        .fileMmapEnable()
                        .transactionEnable()
                        .closeOnJvmShutdown();
                if (configuration.getCacheType() == LOCAL && configuration.isReadOnly() && cacheDir.exists()) {
                    dbmaker.readOnly();
                    readOnly.add(cacheDir.getCanonicalPath());
                }
                db = dbmaker.make();
                dbs.put(cacheDir.getCanonicalPath(), db);
            }
            return db;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public enum CacheType {LOCAL, REMOTE}
}
