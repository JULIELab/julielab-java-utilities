package de.julielab.java.utilities.cache;

import java.util.HashMap;

public class CacheMapSettings extends HashMap<String, Object> {
    public static final String USE_PERSISTENT_CACHE = "usePersistentCache";
    public static final String MAP_TYPE = "mapType";

    /**
     * This is not an actual DBMap setting but only used by the {@link CacheService} to create an in-memory cache
     * for fast access which uses the persistent cache on disc as overflow store.
     */
    public static final String MEM_CACHE_SIZE = "memCacheSize";

    // HTree settings
    public static final String MAX_SIZE = "maxSize";
    /**
     * Causes the size of the store on disc to keep within the given limit in bytes. Using this setting
     * will deactivate transactions because they are not supported by the store type required for staying within
     * given space bounds (see DBMaker.kt line 449, the 'make():DB' method). Thus, a crash of the JVM
     * will corrupt the cache. You can alternatively limit the size in terms of entries via {@link #MAX_SIZE}.
     */
    public static final String MAX_STORE_SIZE = "maxStoreSize";
    public static final String PERSIST_TYPE = "persistenceType";
    public static final String OVERFLOW_DB = "overflowDb";
    public static final String EXPIRE_EXECUTOR = "expireExecutor";
    public static final String EXPIRE_EXECUTOR_PERIOD = "expireExecutorPeriod";
    public static final String EXPIRE_AFTER_CREATE = "expireAfterCreate";
    public static final String EXPIRE_AFTER_GET = "expireAfterGet";
    public static final String EXPIRE_AFTER_UPDATE = "expireAfterUpdate";

    // BTree settings
    public static final String ENABLE_SIZE_COUNT = "enableSizeCount";
    public static final String MAX_NODE_SIZE = "maxNodeSize";

    public CacheMapSettings(Object... settings) {
        for (int i = 0; i < settings.length; i++) {
            if (i % 2 == 1) {
                String key = (String) settings[i - 1];
                Object value = settings[i];
                if (value instanceof Integer && (key.equals(MEM_CACHE_SIZE) || key.equals(MAX_SIZE) || key.equals(MAX_STORE_SIZE) || key.equals(EXPIRE_AFTER_CREATE) || key.equals(EXPIRE_AFTER_GET) || key.equals(EXPIRE_AFTER_UPDATE) || key.equals(EXPIRE_EXECUTOR_PERIOD))) {
                    int intVal = (int) value;
                    long longVal = intVal;
                    value = longVal;
                }
                put(key, value);
            }
        }
    }
}
