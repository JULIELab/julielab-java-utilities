package de.julielab.java.utilities.cache;

import org.mapdb.BTreeMap;
import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.HTreeMap;
import org.mapdb.serializer.GroupSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;

import static de.julielab.java.utilities.cache.CacheMapSettings.*;
import static de.julielab.java.utilities.cache.CacheService.CacheType.LOCAL;

public class CacheService {
    /**
     * <p>Java system property name to enable or disable caching. No value is interpreted as 'true'.</p>
     * <p>This property can be used to disable caching. When set to 'false', all methods returning an
     * instance of {@link CacheAccess} will return the implementation {@link NoOpCacheAccess}. This
     * implementation caches nothing and always returns null for {@link CacheAccess#get(Object)}.</p>
     * <p>If caching is disabled, the call to {@link #initialize(CacheConfiguration)} is no longer
     * necessary.</p>
     */
    public static final String CACHING_ENABLED_PROP = "de.julielab.java.utilities.cache.enabled";
    private final static Logger log = LoggerFactory.getLogger(CacheService.class);
    private static CacheService service;
    private Map<String, DB> dbs = new HashMap<>();
    private Set<String> readOnly = new HashSet<>();
    private CacheConfiguration configuration;
    private List<CacheAccess<?, ?>> cacheAccesses = new ArrayList<>();

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
        return getCacheAccess(cacheId, cacheRegion, keySerializerName, valueSerializerName, 100);
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
    public <K, V> CacheAccess<K, V> getCacheAccess(String cacheId, String cacheRegion, String keySerializerName, String valueSerializerName, long memCacheSize) {
        CacheAccess<K, V> ret;
        String propertyValue = System.getProperty(CACHING_ENABLED_PROP);
        if (propertyValue != null && !Boolean.parseBoolean(propertyValue))
            return new NoOpCacheAccess<>(cacheId, cacheRegion);
        switch (configuration.getCacheType()) {
            case LOCAL:
                ret = new LocalFileCacheAccess<>(cacheId, cacheRegion, keySerializerName, valueSerializerName, configuration.getLocalCacheDir(), memCacheSize);
                break;
            case REMOTE:
                ret = new RemoteCacheAccess<>(cacheId, cacheRegion, keySerializerName, valueSerializerName, configuration.getRemoteCacheHost(), configuration.getRemoteCachePort(), memCacheSize);
                break;
            default:
                throw new IllegalArgumentException("Unknown cache type '" + configuration.getCacheType() + "' in the configuration.");
        }
        cacheAccesses.add(ret);
        return ret;
    }

    public <K, V> CacheAccess<K, V> getCacheAccess(String cacheId, String cacheRegion, String keySerializerName, String valueSerializerName, CacheMapSettings mapSettings) {
        CacheAccess<K, V> ret;
        String propertyValue = System.getProperty(CACHING_ENABLED_PROP);
        if (propertyValue != null && !Boolean.parseBoolean(propertyValue))
            return new NoOpCacheAccess<>(cacheId, cacheRegion);
        switch (configuration.getCacheType()) {
            case LOCAL:
                ret = new LocalFileCacheAccess<>(cacheId, cacheRegion, keySerializerName, valueSerializerName, configuration.getLocalCacheDir(), mapSettings);
                break;
            case REMOTE:
                ret = new RemoteCacheAccess<>(cacheId, cacheRegion, keySerializerName, valueSerializerName, configuration.getRemoteCacheHost(), configuration.getRemoteCachePort(), (long) mapSettings.get(MEM_CACHE_SIZE));
                break;
            default:
                throw new IllegalArgumentException("Unknown cache type '" + configuration.getCacheType() + "' in the configuration.");
        }
        cacheAccesses.add(ret);
        return ret;
    }

    boolean isDbReadOnly(File file) {
        try {
            return readOnly.contains(file.getCanonicalPath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    synchronized void commitCache(File dbFile) {
        if (!isDbReadOnly(dbFile)) {
            try {
                dbs.get(dbFile.getCanonicalPath()).commit();
            } catch (IOException e) {
                log.error("Could not commit db at {}.", dbFile, e);
            }
        } else
            log.debug("Cannot commit cache {} because it is read-only.", dbFile);
    }


    <K, V> BTreeMap<K, V> getBTreeCache(File dbFile, String regionName, GroupSerializer<K> keySerializer, GroupSerializer<V> valueSerializer, Map<String, Object> mapSettings) {
        final DB db = mapSettings.get(PERSIST_TYPE) == CachePersistenceType.MEM ? getMemdb(dbFile.getName()) : getFiledb(dbFile);
        final DB.TreeMapMaker<K, V> dbmaker = db.treeMap(regionName).keySerializer(keySerializer).valueSerializer(valueSerializer);
        for (String setting : mapSettings.keySet()) {
            switch (setting) {
                case ENABLE_SIZE_COUNT:
                    if ((boolean) mapSettings.get(ENABLE_SIZE_COUNT)) dbmaker.counterEnable();
                    break;
                case MAX_NODE_SIZE:
                    dbmaker.maxNodeSize((Integer) mapSettings.get(MAX_NODE_SIZE));
                    break;
            }
        }
        if (isDbReadOnly(dbFile))
            return dbmaker.open();
        return dbmaker.
                createOrOpen();
    }

    <K, V> HTreeMap<K, V> getHTreeCache(File dbFile, String regionName, GroupSerializer<K> keySerializer, GroupSerializer<V> valueSerializer, Map<String, Object> mapSettings) {
        final DB db = mapSettings.get(PERSIST_TYPE) == CachePersistenceType.MEM ? getMemdb(dbFile.getName()) : getFiledb(dbFile);
        final DB.HashMapMaker<K, V> dbmaker = db.hashMap(regionName).keySerializer(keySerializer).valueSerializer(valueSerializer);
        for (String setting : mapSettings.keySet()) {
            switch (setting) {
                case MAX_SIZE:
                    if (mapSettings.get(EXPIRE_AFTER_CREATE) == null && mapSettings.get(EXPIRE_AFTER_GET) == null && mapSettings.get(EXPIRE_AFTER_UPDATE) == null)
                        log.warn("A maximum cache size is given but no trigger (after create, get or update) that would enqueue elements for eviction has been specified. Eviction will not happen.");
                    dbmaker.expireMaxSize((Long) mapSettings.get(MAX_SIZE));
                    break;
                case OVERFLOW_DB:
                    if (mapSettings.get(EXPIRE_AFTER_CREATE) == null && mapSettings.get(EXPIRE_AFTER_GET) == null && mapSettings.get(EXPIRE_AFTER_UPDATE) == null)
                        log.warn("An expiration overflow map is given but no trigger (after create, get or update) that would enqueue elements for eviction has been specified. Overflow will not happen.");
                    dbmaker.expireOverflow((Map<K, V>) mapSettings.get(OVERFLOW_DB));
                    break;
                case EXPIRE_EXECUTOR:
                    dbmaker.expireExecutor((ScheduledExecutorService) mapSettings.get(EXPIRE_EXECUTOR));
                    break;
                case EXPIRE_EXECUTOR_PERIOD:
                    dbmaker.expireExecutorPeriod((Long) mapSettings.get(EXPIRE_EXECUTOR_PERIOD));
                    break;
                case EXPIRE_AFTER_CREATE:
                    if (Boolean.parseBoolean(String.valueOf(mapSettings.get(EXPIRE_AFTER_CREATE))))
                        dbmaker.expireAfterCreate();
                    else
                        dbmaker.expireAfterCreate((Long) mapSettings.get(EXPIRE_AFTER_CREATE));
                    break;
                case EXPIRE_AFTER_GET:
                    if (Boolean.parseBoolean(String.valueOf(mapSettings.get(EXPIRE_AFTER_GET))))
                        dbmaker.expireAfterGet();
                    else
                        dbmaker.expireAfterGet((Long) mapSettings.get(EXPIRE_AFTER_GET));
                    break;
                case EXPIRE_AFTER_UPDATE:
                    if (Boolean.parseBoolean(String.valueOf(mapSettings.get(EXPIRE_AFTER_UPDATE))))
                        dbmaker.expireAfterUpdate();
                    else
                        dbmaker.expireAfterUpdate((Long) mapSettings.get(EXPIRE_AFTER_UPDATE));
                    break;
            }
        }
        if (isDbReadOnly(dbFile))
            return dbmaker.open();
        return dbmaker.
                createOrOpen();
    }

    <K, V> HTreeMap<K, V> getHTreeCache(File dbFile, String regionName, GroupSerializer<K> keySerializer, GroupSerializer<V> valueSerializer) {
        return getHTreeCache(dbFile, regionName, keySerializer, valueSerializer, Collections.emptyMap());
    }

    /**
     * @param dbFile
     * @param regionName
     * @param keySerializer
     * @param valueSerializer
     * @param <K>
     * @param <V>
     * @return
     * @deprecated Use {@link #getHTreeCache(File, String, GroupSerializer, GroupSerializer)}
     */
    <K, V> HTreeMap<K, V> getCache(File dbFile, String regionName, GroupSerializer<K> keySerializer, GroupSerializer<V> valueSerializer) {
        return getHTreeCache(dbFile, regionName, keySerializer, valueSerializer);
    }

    public void commitAllCaches() {
        // We issue commit commands to all the cache accesses that
        cacheAccesses.stream().filter(ca -> !ca.isClosed()).forEach(CacheAccess::commit);
    }

    private DB getFiledb(File cacheDir) {
        try {
            DB db = dbs.get(cacheDir.getCanonicalPath());
            if (db == null || db.isClosed() || db.getStore().isClosed()) {
                DBMaker.Maker dbmaker;
                synchronized (this) {
                    if (!dbs.containsKey(cacheDir.getCanonicalPath()) || db.isClosed() || db.getStore().isClosed()) {
                        dbmaker = DBMaker
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
                    } else {
                        db = dbs.get(cacheDir.getCanonicalPath());
                    }
                }
            }
            return db;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private DB getMemdb(String name) {
        DB db = dbs.get(name);
        if (db == null || db.isClosed()) {
            DBMaker.Maker dbmaker;
            synchronized (this) {
                if (!dbs.containsKey(name) || db.isClosed()) {
                    dbmaker = DBMaker
                            .memoryDB()
                            .closeOnJvmShutdown();
                    db = dbmaker.make();
                    dbs.put(name, db);
                } else {
                    db = dbs.get(name);
                }
            }
        }
        return db;
    }

    public enum CacheType {LOCAL, REMOTE}

    public enum CacheMapDataType {HTREE, BTREE}

    public enum CachePersistenceType {MEM, DISC}
}
