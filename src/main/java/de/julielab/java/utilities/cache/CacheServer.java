package de.julielab.java.utilities.cache;

import org.mapdb.HTreeMap;
import org.mapdb.serializer.GroupSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class CacheServer {
    public static final String METHOD_GET = "get";
    public static final String METHOD_PUT = "put";
    public static final String RESPONSE_OK = "OK";
    public static final String RESPONSE_FAILURE = "FAILURE";
    private final static Logger log = LoggerFactory.getLogger(CacheServer.class);
    private final File cacheDir;
    private final String host;
    private final int port;
    private final ExecutorService executorService;
    private Thread backgroundThread;


    public CacheServer(File cacheDir, String host, int port) {
        this.cacheDir = cacheDir;
        this.host = host;
        this.port = port;
        CacheService.initialize(new CacheConfiguration(CacheService.CacheType.REMOTE, null, host, port, false));
        executorService = Executors.newCachedThreadPool();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public static void main(String[] args) throws IOException {
        final File cacheDir = new File(args[0]);
        final String host = args[1];
        final int port = Integer.valueOf(args[2]);
        log.info("Starting logger with cacheDir {}, host {} and port {}", cacheDir, host, port);
        final CacheServer cacheServer = new CacheServer(cacheDir, host, port);
        cacheServer.run();
    }

    public void run() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port, 1000, InetAddress.getByName(host))) {
            log.info("CacheServer ready for requests.");
            while (true) {
                final Socket socket = serverSocket.accept();
                log.debug("Handling new incoming connection");
                executorService.submit(new RequestServer(socket));
            }
        }
    }

    public void runInBackground() {
        if (backgroundThread == null) {
            backgroundThread = new Thread() {
                @Override
                public void interrupt() {
                    super.interrupt();
                    log.trace("Terminating background cache server thread.");
                }

                @Override
                public void run() {
                    try {
                        CacheServer.this.run();
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            };

            log.debug("Starting background thread for caching server");
            backgroundThread.start();
        } else {
            throw new IllegalStateException("Background thread for caching server is already running.");
        }
    }

    public void shutdown() {
        log.info("Shutting down cache server.");
        CacheService.shutdown();
        if (backgroundThread != null)
            backgroundThread.interrupt();
        executorService.shutdown();
    }

    private class RequestServer extends Thread {
        private final Socket socket;

        public RequestServer(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            final CacheService cacheService = CacheService.getInstance();
            log.trace("Establishing connection to requesting client");
            try (ObjectInputStream ois = new ObjectInputStream(socket.getInputStream()); ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream())) {
                while (true) {
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

                            GroupSerializer<?> keySerializer = CacheAccess.getSerializerByName(keySerializerName);
                            GroupSerializer<?> valueSerializer = CacheAccess.getSerializerByName(valueSerializerName);
                            final File cacheFile = new File(cacheDir.getAbsolutePath(), cacheName);
                            final HTreeMap cache = cacheService.getCache(cacheFile, cacheRegion, keySerializer, valueSerializer);

                            if (method.equalsIgnoreCase(METHOD_GET)) {
                                final Object o = cache.get(key);
                                if (o != null)
                                    log.trace("Returning data for key '{}' from cache {}, {}.", key, cacheName, cacheRegion);
                                else
                                    log.trace("No cached data available for key '{}' in cache {}, {}.", key, cacheName, cacheRegion);
                                oos.writeObject(o);
                                oos.flush();
                            } else if (method.equalsIgnoreCase(METHOD_PUT)) {
                                if (log.isTraceEnabled()) {
                                    String valueString = value == null ? null : value.toString();
                                    if (valueString != null)
                                        valueString = valueString.substring(0, Math.min(valueString.length(), 79));
                                    log.trace("Putting data '{}' for key '{}' into the cache {}, {}.", valueString, key, cacheName, cacheRegion);
                                }
                                cache.put(key, value);
                                log.trace("Sending OK response");
                                oos.writeUTF("OK");
                                oos.flush();
                            }
                        } else {
                            // This is a commit request; we can currently only commit all caches at once.
                            CacheService.getInstance().commitAllCaches();
                        }
                    } catch (SocketException e) {
                        if (e.getMessage().contains("Broken pipe")) {
                            log.debug("Client disconnected (Broken pipe).");
                        } else {
                            log.error("Connection error", e);
                        }
                    } catch (Throwable e) {
                        log.error("Exception occurred. Sending an error message to the client and terminating the connection.", e);
                        try {
                            oos.writeUTF(RESPONSE_FAILURE);
                            oos.writeObject(e);
                            oos.flush();
                        } catch (IOException e1) {
                            // OK, so we couldn't even send the error ignore it
                        }
                        oos.close();
                        ois.close();
                        break;
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
