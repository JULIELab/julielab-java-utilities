package de.julielab.java.utilities.cache;

import org.mapdb.HTreeMap;
import org.mapdb.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CacheServer {
    public static final String METHOD_GET = "get";
    public static final String METHOD_PUT = "put";
    public static final String RESPONSE_OK = "OK";
    public static final String RESPONSE_FAILURE = "FAILURE";
    private final static Logger log = LoggerFactory.getLogger(CacheServer.class);
    private File cacheDir;
    private String host;
    private int port;
    private ExecutorService executorService;


    public CacheServer(File cacheDir, String host, int port, int numThreads) {
        this.cacheDir = cacheDir;
        this.host = host;
        this.port = port;
        CacheService.initialize(new CacheConfiguration(CacheService.CacheType.REMOTE, null, host, port, false));
        executorService = Executors.newFixedThreadPool(numThreads);
    }

    public static void main(String[] args) throws IOException {
        final File cacheDir = new File(args[0]);
        final String host = args[1];
        final int port = Integer.valueOf(args[2]);
        final int numThreads = Integer.valueOf(args[0]);
        log.info("Starting logger with cacheDir {}, host {}, port {} and the number of threads {}", cacheDir, host, port, numThreads);
        final CacheServer cacheServer = new CacheServer(cacheDir, host, port, numThreads);
        cacheServer.run();
    }

    private void run() throws IOException {
        final ServerSocket serverSocket = new ServerSocket(port, 1000, InetAddress.getByName(host));
        try {
            log.info("CacheServer ready for requests.");
            while (true) {
                log.debug("Waiting for request.");
                final Socket socket = serverSocket.accept();
                executorService.submit(new RequestServer(socket));
            }
        } finally {
            serverSocket.close();
        }
    }

    private class RequestServer extends Thread {
        private Socket socket;

        public RequestServer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            final CacheService cacheService = CacheService.getInstance();
            try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream()); ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
                try {
                    log.trace("Reading request data.");
                    final String method = ois.readUTF();
                    final String cacheName = ois.readUTF();
                    final String cacheRegion = ois.readUTF();
                    final String keySerializerName = ois.readUTF();
                    final String valueSerializerName = ois.readUTF();
                    final Object key = ois.readObject();

                    // The key being null is the commit-signal
                    if (key != null) {
                        Object value = null;
                        if (method.equalsIgnoreCase(METHOD_PUT))
                            value = ois.readObject();

                        Serializer<?> keySerializer = CacheAccess.getSerializerByName(keySerializerName);
                        Serializer<?> valueSerializer = CacheAccess.getSerializerByName(valueSerializerName);
                        final File cacheFile = new File(cacheDir.getAbsolutePath(), cacheName);
                        final HTreeMap cache = cacheService.getCache(cacheFile, cacheRegion, keySerializer, valueSerializer);

                        if (method.equalsIgnoreCase(METHOD_GET)) {
                            final Object o = cache.get(key);
                            if (o != null)
                                log.trace("Returning data for key '{}' from cache {}, {}.", key, cacheName, cacheRegion);
                            else
                                log.trace("No cached data available for key '{}' in cache {}, {}.", key, cacheName, cacheRegion);
                            oos.writeObject(o);
                        } else if (method.equalsIgnoreCase(METHOD_PUT)) {
                            log.trace("Putting data for key '{}' into the cache {}, {}.", key, cacheName, cacheRegion);
                            cache.put(key, value);
                            oos.writeUTF("OK");
                        }
                    } else {
                        // This is a commit request; we can currently only commit all caches at once.
                        CacheService.getInstance().commitAllCaches();
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    if (oos != null) {
                        try {
                            oos.writeUTF(RESPONSE_FAILURE);
                            oos.writeObject(e);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    cacheService.commitAllCaches();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
