package de.julielab.java.utilities.cache;


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

    public RemoteCacheAccess(String cacheId, String cacheRegion, String keySerializer, String valueSerializer, String host, int port) {
        super(cacheId, cacheRegion);
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.host = host;
        this.port = port;
    }

    public void establishConnection() {
        try {
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
        if (!connectionOpen)
            establishConnection();
        try {
            writeDefaultInformation(CacheServer.METHOD_GET, key, oos);
            return (V) ois.readObject();
        } catch (IOException e) {
            connectionOpen = false;
            throw new IllegalStateException(e);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException(e);
        }
    }

    private void writeDefaultInformation(String method, K key, ObjectOutputStream oos) throws IOException {
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
            writeDefaultInformation(CacheServer.METHOD_PUT, key, oos);
            oos.writeObject(value);
            final String response = ois.readUTF();
            if (response.equalsIgnoreCase(CacheServer.RESPONSE_FAILURE)) {
                Exception e = (Exception) ois.readObject();
                log.error("Could not put data into the remote cache:", e);
            }
            return response.equals(CacheServer.RESPONSE_OK);
        } catch (IOException e) {
            connectionOpen = false;
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
            // Sending a null key will cause all caches to be comitted. If we should need the commit
            // of individual caches in the future, that feature would need to be implemented because it is not
            // possible currently.
            writeDefaultInformation(CacheServer.METHOD_PUT, null, oos);
            oos.close();
            ois.close();
            connectionOpen = false;
        } catch (IOException e) {
            connectionOpen = false;
            e.printStackTrace();
        }
    }
}
