package de.julielab.java.utilities.cache;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static de.julielab.java.utilities.cache.CacheService.CacheType.LOCAL;

public class CacheService {
    private final static Logger log = LoggerFactory.getLogger(CacheService.class);
    private static CacheService service;
    private final String thisJvmName;
    private Map<String, DB> dbs = new HashMap<>();
    private Set<String> readOnly = new HashSet<>();
    private CacheConfiguration configuration;

    private CacheService(CacheConfiguration configuration) {
        this.configuration = configuration;
        thisJvmName = ManagementFactory.getRuntimeMXBean().getName();
    }

    public static CacheService getInstance() {
        if (service == null)
            throw new IllegalStateException("Call 'initialize' before acquiring an instance of the CacheService.");
        return service;
    }

    public static void initialize(CacheConfiguration configuration) {
        if (service == null)
            service = new CacheService(configuration);

    }

    public <K, V> CacheAccess<K, V> getCacheAccess(String cacheId, String cacheRegion, String keySerializerName, String valueSerializerName) {
        switch (configuration.getCacheType()) {
            case LOCAL:
                return new LocalFileCacheAccess<>(cacheId, cacheRegion, keySerializerName, valueSerializerName, configuration.getLocalCacheDir());
            case REMOTE:
                return new RemoteCacheAccess<>(cacheId, cacheRegion, keySerializerName, valueSerializerName, configuration.getRemoteCacheHost(), configuration.getRemoteCachePort());
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

    <K, V> HTreeMap<K, V> getCache(File dbFile, String regionName, Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        final DB filedb = getFiledb(dbFile);
        final DB.HashMapMaker<K, V> dbmaker = filedb.hashMap(regionName).keySerializer(keySerializer).valueSerializer(valueSerializer);
        if (isDbReadOnly(dbFile))
            return dbmaker.open();
        return dbmaker.
                createOrOpen();
    }

    void commitCache(File dbFile) {
        if (!isDbReadOnly(dbFile))
            getFiledb(dbFile).commit();
        else
            log.debug("Cannot commit cache {} because it is read-only.", dbFile);
    }

    private DB getFiledb(File cacheDir) {
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

    enum CacheType {LOCAL, REMOTE}
}
