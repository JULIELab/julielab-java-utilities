package de.julielab.java.utilities.cache;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Random;

import static org.assertj.core.api.Assertions.assertThat;

public class RemoteCacheTest {
    private final static Logger log = LoggerFactory.getLogger(RemoteCacheTest.class);
    private static CacheServer cacheServer;

    @BeforeClass
    public static void setup() throws Exception {
        String host = "localhost";
        Random random = new Random();
        int port = random.nextInt(1000) + 9000;
        File cacheDir = new File("src/test/resources/remotecachetest");
        if (cacheDir.exists())
            FileUtils.deleteQuietly(cacheDir);
        int numTries = 0;
        while (cacheServer == null && numTries < 3) {
            cacheServer = new CacheServer(cacheDir, host, port, 4);
            port = random.nextInt(1000) + 9000;
            ++numTries;
        }
        log.debug("Using port {} for remote cache test,", port);
        cacheServer.runInBackground();
    }

    @AfterClass
    public static void shutdown() {
        cacheServer.shutdown();
    }

    @Test
    public void test() {
        CacheAccess<String, String> ca = CacheService.getInstance().getCacheAccess("testcache", "RemoteCacheTest", CacheAccess.STRING, CacheAccess.STRING);
        CacheAccess<String, String> ca2 = CacheService.getInstance().getCacheAccess("testcache", "RemoteCacheTest2", CacheAccess.STRING, CacheAccess.STRING);
        ca.put("key1", "value1");
        assertThat(ca.get("key1")).isEqualTo("value1");

        ca2.put("key21", "value21");
        ca2.put("key22", "value22");

        ca.put("key2", "value2");
        assertThat(ca.get("key2")).isEqualTo("value2");

        ca.put("key3", "value3");
        assertThat(ca.get("key3")).isEqualTo("value3");

        assertThat(ca2.get("key21")).isEqualTo("value21");
        assertThat(ca2.get("key22")).isEqualTo("value22");
    }
}
