package de.julielab.java.utilities.index;

import java.io.InputStream;
import java.net.URI;
import java.util.Map;

public interface IStringArrayMapProvider {
    Map<String, String[]> getMap();

    void load(URI uri) throws IndexCreationException;

    void load(InputStream inputStream) throws IndexCreationException;

    void setValueIndices(int... valueIndices);

    void setKeyIndices(int... keyIndex);
}
