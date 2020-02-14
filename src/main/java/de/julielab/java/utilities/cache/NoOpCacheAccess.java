package de.julielab.java.utilities.cache;

public class NoOpCacheAccess<K, V> extends CacheAccess<K, V> {
    public NoOpCacheAccess(String cacheId, String cacheRegion) {
        super(cacheId, cacheRegion);
    }

    @Override
    public V get(K key) {
        return null;
    }

    @Override
    public boolean put(K key, V value) {
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public boolean isClosed() {
        return false;
    }

    @Override
    public void commit() {
        // no-op
    }
}
