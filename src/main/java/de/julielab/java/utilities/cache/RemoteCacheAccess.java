package de.julielab.java.utilities.cache;


import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class RemoteCacheAccess<K, V> extends CacheAccess<K, V> {
    private final static Logger log = LoggerFactory.getLogger(RemoteCacheAccess.class);
    private final String keySerializer;
    private final String valueSerializer;
    private final String host;
    private final int port;
    private ObjectOutputStream oos;
    private ObjectInputStream ois;
    private boolean connectionOpen = false;
    private Cache<K, V> memCache;

    public RemoteCacheAccess(String cacheId, String cacheRegion, String keySerializer, String valueSerializer, String host, int port) {
        this(cacheId, cacheRegion, keySerializer, valueSerializer, host, port, 100);
    }

    public RemoteCacheAccess(String cacheId, String cacheRegion, String keySerializer, String valueSerializer, String host, int port, int memCacheSize) {
        super(cacheId, cacheRegion);
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.host = host;
        this.port = port;
        memCache = CacheBuilder.newBuilder().maximumSize(memCacheSize).build();
    }

    public void establishConnection() {
        try {
            log.debug("Establishing new connection to cache server at {}:{} for cache ID {} and region {}", host, port, cacheId, cacheRegion);
            Socket s = getSocket();
            oos = new ObjectOutputStream(s.getOutputStream());
            ois = new ObjectInputStream(s.getInputStream());
            connectionOpen = true;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }

    }

    private Socket getSocket() {
        try {
            return new Socket(InetAddress.getByName(host), port);
        } catch (IOException e) {
            log.error("Could not create a socket to {}:{}", host, port, e);
            throw new IllegalStateException(e);
        }
    }

    @Override
    public V get(K key) {
        V value = memCache.getIfPresent(key);
        if (value == null) {
            if (!connectionOpen)
                establishConnection();
            try {
                writeDefaultInformation(CacheServer.METHOD_GET, key, oos);
                value = (V) ois.readObject();
                if (value != null)
                    memCache.put(key, value);
            } catch (IOException e) {
                log.trace("Closing connection to {}:{}, cache ID {} and region {} due to exception in get().", host, port, cacheId, cacheRegion, e);
                try {
                    oos.close();
                } catch (IOException ex) {
                    //
                }
                try {
                    ois.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                connectionOpen = false;
                throw new IllegalStateException(e);
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException(e);
            }
        }
        return value;
    }

    private void writeDefaultInformation(String method, K key, ObjectOutputStream oos) throws IOException {
        if (key == null)
            throw new IllegalArgumentException("The cache key is null.");

        oos.writeUTF(method);
        oos.writeUTF(cacheId);
        oos.writeUTF(cacheRegion);
        oos.writeUTF(keySerializer);
        oos.writeUTF(valueSerializer);
        oos.writeObject(key);
    }

    @Override
    public boolean put(K key, V value) {
        if (!connectionOpen)
            establishConnection();
        try {
            if (value != null)
                memCache.put(key, value);
            writeDefaultInformation(CacheServer.METHOD_PUT, key, oos);
            oos.writeObject(value);
            oos.flush();
            final String response = ois.readUTF();
            if (response.equalsIgnoreCase(CacheServer.RESPONSE_FAILURE)) {
                Exception e = (Exception) ois.readObject();
                log.error("Could not put data into the remote cache:", e);
            }
            return response.equals(CacheServer.RESPONSE_OK);
        } catch (IOException e) {
            log.trace("Closing connection to {}:{}, cache ID {} and region {} due to exception in put().", host, port, cacheId, cacheRegion, e);
            connectionOpen = false;
            try {
                oos.close();
            } catch (IOException ex) {
                //
            }
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void commit() {
        if (!connectionOpen)
            establishConnection();
        try {
            // Sending a null key will cause all caches to be committed. If we should need the commit
            // of individual caches in the future, that feature would need to be implemented because it is not
            // possible currently.
            writeDefaultInformation(CacheServer.METHOD_PUT, null, oos);
            oos.close();
            ois.close();
            connectionOpen = false;
        } catch (IOException e) {
            log.trace("Closing connection to {}:{}, cache ID {} and region {} due to exception in commit().", host, port, cacheId, cacheRegion, e);
            try {
                oos.close();
            } catch (IOException ex) {
                //
            }
            try {
                ois.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            connectionOpen = false;
            e.printStackTrace();
        }
    }
}
