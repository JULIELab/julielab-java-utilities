package de.julielab.java.utilities.cache;

import java.io.File;

public class CacheConfiguration {
    private final File localCacheDir;
    private final String remoteCacheHost;
    private final int remoteCachePort;
    private final boolean readOnly;
    private final CacheService.CacheType cacheType;

    public CacheConfiguration(CacheService.CacheType cacheType, File localCacheDir, String remoteCacheHost, int remoteCachePort, boolean readOnly) {
        this.cacheType = cacheType;
        this.localCacheDir = localCacheDir;
        this.remoteCacheHost = remoteCacheHost;
        this.remoteCachePort = remoteCachePort;
        this.readOnly = readOnly;
    }

    public CacheService.CacheType getCacheType() {
        return cacheType;
    }

    public File getLocalCacheDir() {
        return localCacheDir;
    }

    public String getRemoteCacheHost() {
        return remoteCacheHost;
    }

    public int getRemoteCachePort() {
        return remoteCachePort;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
