package de.julielab.java.utilities.cache;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class LocalCacheTest {
    @BeforeClass
    public static void setup() {
        File cacheDir = new File("src/test/resources/localcachetest");
        CacheService.initialize(new CacheConfiguration(CacheService.CacheType.LOCAL, cacheDir, null, 0, false));
        if (cacheDir.exists())
            FileUtils.deleteQuietly(cacheDir);
    }

    @Test
    public void testReferenceWriteThrough() {
        CacheAccess<String, List<String>> ca = CacheService.getInstance().getCacheAccess("testcache", "LocalCacheTest", CacheAccess.STRING, CacheAccess.JAVA);
        List<String> l = new ArrayList<>();
        l.add("eins");
        l.add("zwei");
        ca.put("testkey", l);
        ca.commit();
        l.set(0, "fuenf");
        // There is no update by reference
        l = ca.get("testkey");
        assertEquals("eins", l.get(0));
        // We need to put the value again to make the change happen (this is different from the guava in-´memory cache!)
        l.set(0, "fuenf");
        ca.put("testkey", l);
        l = ca.get("testkey");
        assertEquals("fuenf", l.get(0));
    }

    @Test
    public void testEvictMaxSizeOverflow() {
        CacheAccess<String, String> cacheAccess = CacheService.getInstance().getCacheAccess("testcache", "InMemTest", CacheAccess.STRING, CacheAccess.STRING, new CacheMapSettings(CacheMapSettings.MEM_CACHE_SIZE, 2L));
        LocalFileCacheAccess<String, String> ca = (LocalFileCacheAccess<String, String>) cacheAccess;
        for (int i = 0; i < 20; i++)
            ca.put("key" + i, "val" + i);


        ca.commit();
        ca.close();

        cacheAccess = CacheService.getInstance().getCacheAccess("testcache", "InMemTest", CacheAccess.STRING, CacheAccess.STRING, new CacheMapSettings(CacheMapSettings.MEM_CACHE_SIZE, 2L));
        ca = (LocalFileCacheAccess<String, String>) cacheAccess;
        for (int i = 0; i < 20; i++) {
            assertNotNull(ca.get("key" + i));
        }

    }

    @Test
    public void testMemOnly() {
        CacheAccess<String, String> cacheAccess = CacheService.getInstance().getCacheAccess("testcache", "MemOnlyTest", CacheAccess.STRING, CacheAccess.STRING, new CacheMapSettings(CacheMapSettings.MEM_CACHE_SIZE, 2L, CacheMapSettings.USE_PERSISTENT_CACHE, false));
        LocalFileCacheAccess<String, String> ca = (LocalFileCacheAccess<String, String>) cacheAccess;
        for (int i = 0; i < 20; i++)
            ca.put("key" + i, "val" + i);


        ca.commit();
        ca.close();

        cacheAccess = CacheService.getInstance().getCacheAccess("testcache", "MemOnlyTest", CacheAccess.STRING, CacheAccess.STRING, new CacheMapSettings(CacheMapSettings.MEM_CACHE_SIZE, 2L));
        ca = (LocalFileCacheAccess<String, String>) cacheAccess;
        // There was no persistent cache, all entried should be null.
        for (int i = 0; i < 20; i++) {
            assertNull(ca.get("key" + i));
        }

    }
}
