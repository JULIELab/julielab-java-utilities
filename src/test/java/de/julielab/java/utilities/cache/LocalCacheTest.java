package de.julielab.java.utilities.cache;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
        l = ca.get("testkey");
        System.out.println(l);
    }
}
