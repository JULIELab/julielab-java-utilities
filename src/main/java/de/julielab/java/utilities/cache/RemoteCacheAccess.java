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

    public RemoteCacheAccess(String cacheId, String cacheRegion, String keySerializer, String valueSerializer, String host, int port) {
        super(cacheId, cacheRegion);
        this.keySerializer = keySerializer;
        this.valueSerializer = valueSerializer;
        this.host = host;
        this.port = port;
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
        try (Socket s = getSocket()) {
            final ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            writeDefaultInformation(CacheServer.METHOD_GET, key, oos);
            final ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            return (V) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
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
        try (Socket s = getSocket()) {
            final ObjectOutputStream oos = new ObjectOutputStream(s.getOutputStream());
            writeDefaultInformation(CacheServer.METHOD_PUT, key, oos);
            oos.writeObject(value);
            final ObjectInputStream ois = new ObjectInputStream(s.getInputStream());
            final String response = ois.readUTF();
            if (response.equalsIgnoreCase(CacheServer.RESPONSE_FAILURE)) {
                Exception e = (Exception) ois.readObject();
                log.error("Could not put data into the remote cache:", e);
            }
            return response.equals(CacheServer.RESPONSE_OK);
        } catch (IOException e) {
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
}
