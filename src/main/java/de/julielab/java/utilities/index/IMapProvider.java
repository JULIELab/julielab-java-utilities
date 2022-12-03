package de.julielab.java.utilities.index;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

public interface IMapProvider<K, V> {
    Map<K, V> getMap();

    void load(URI uri) throws IndexCreationException;

    void load(InputStream inputStream) throws IndexCreationException;

    void setValueIndex(int valueIndex);

    void setKeyIndex(int keyIndex);
}
