package de.julielab.java.utilities.cache;

import java.util.HashMap;

public class CacheMapSettings extends HashMap<String, Object> {
    public static final String MAP_TYPE = "mapType";

    public static final String MEM_CACHE_SIZE = "memCacheSize";

    // HTree settings
    public static final String MAX_SIZE = "maxSize";
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
