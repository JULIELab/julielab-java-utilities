package de.julielab.java.utilities.cache;

import org.mapdb.Serializer;

public abstract class CacheAccess<K, V> {
    public static final String STRING = "string";
    public static final String JAVA = "java";
    public static final String BYTEARRAY = "bytearray";
    public static final String DOUBLEARRAY = "doublearray";
    protected String cacheId;
    protected String cacheRegion;

    public CacheAccess(String cacheId, String cacheRegion) {
        this.cacheId = cacheId;
        this.cacheRegion = cacheRegion;
    }

    public static <T> Serializer<T> getSerializerByName(String name) {
        switch (name.toLowerCase()) {
            case STRING:
                return (Serializer<T>) Serializer.STRING;
            case JAVA:
                return Serializer.JAVA;
            case BYTEARRAY:
                return (Serializer<T>) Serializer.BYTE_ARRAY;
            case DOUBLEARRAY:
                return (Serializer<T>) Serializer.DOUBLE_ARRAY;
            default:
                throw new IllegalArgumentException("Unsupported cache serializer '" + name + "'.");
        }
    }

    public abstract V get(K key);

    public abstract boolean put(K key, V value);

    public abstract boolean isReadOnly();
}
