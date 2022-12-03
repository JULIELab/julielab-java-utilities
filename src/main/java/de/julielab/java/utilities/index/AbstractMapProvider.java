package de.julielab.java.utilities.index;

import de.julielab.java.utilities.UriUtilities;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * <p>Base class for resources that map one term to another. Uses a HashMap.^</p>
 * <p>This class is abstract because it is generic. To work with other data types than strings, the {@link #getKey(String)} and {@link #getValue(String)}
 * methods are overridden by subclasses to deliver the correct data types from the string input.</p>
 * <p>Subclasses deal with maps where the keys and/or values are not strings but numbers. Other subclasses deal with
 * String but use a persistent data structure to deal with very large maps.</p>
 *
 * @param <K>
 * @param <V>
 */
public abstract class AbstractMapProvider<K, V> implements IMapProvider<K, V> {
    protected final Logger log;
    protected boolean reverse = false;
    protected Map<K, V> map;
    private int keyIndex = 0;
    private int valueIndex = 1;
    private Set<K> eligibleKeys = Collections.emptySet();

    public AbstractMapProvider(Logger log) {
        this.log = log;
        map = new HashMap<>();
    }

    public Set<K> getEligibleKeys() {
        return eligibleKeys;
    }

    public void setEligibleKeys(Set<K> eligibleKeys) {
        this.eligibleKeys = eligibleKeys;
    }

    public int getKeyIndex() {
        return keyIndex;
    }

    public void setKeyIndex(int keyIndex) {
        this.keyIndex = keyIndex;
    }

    public int getValueIndex() {
        return valueIndex;
    }

    public void setValueIndex(int valueIndex) {
        this.valueIndex = valueIndex;
    }

    public void load(URI uri) throws IndexCreationException {

        InputStream is;
        try {
            is = UriUtilities.getInputStreamFromUri(uri);
            load(is);
        } catch (IndexCreationException e) {
            log.error("Could not create index from URI {}", uri, e);
            throw e;
        } catch (Exception e) {
            throw new IndexCreationException("Resource " + uri + " not found");
        }

    }

    public void load(InputStream is) throws IndexCreationException {
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            String line;
            String splitExpression = "\t";
            int numEntries = 0;
            while ((line = br.readLine()) != null) {
                if (line.trim().length() == 0 || line.startsWith("#"))
                    continue;
                ++numEntries;
                int maxIndex = Math.max(keyIndex, valueIndex);
                String[] split = line.split(splitExpression, maxIndex + 2);
                if (split.length < maxIndex) {
                    splitExpression = "\\s+";
                    split = line.split(splitExpression);
                }
                if (split.length < maxIndex)
                    throw new IllegalArgumentException("Format error in map file: Expected format is file with tab-separated columns with at least " + maxIndex + " fields but the input line '" + line
                            + "' has " + split.length + " columns.");
                if (reverse) {
                    final K key = getKey(split[valueIndex]);
                    if (eligibleKeys.isEmpty() || eligibleKeys.contains(key))
                        put(key, getValue(split[keyIndex]));
                } else {
                    final K key = getKey(split[keyIndex]);
                    if (eligibleKeys.isEmpty() || eligibleKeys.contains(key)) {
                        put(key, getValue(split[valueIndex]));
                    }
                }
            }
            log.info("Finished reading resource from InputStream and got {} entries.", numEntries);
        } catch (IOException e) {
            throw new IndexCreationException(e);
        } finally {
            try {
                if (null != br)
                    br.close();
            } catch (IOException e) {
                throw new IndexCreationException(e);
            }
        }
    }

    protected abstract void put(K key, V value);

    protected abstract V getValue(String valueString);

    protected abstract K getKey(String keyString);

    /**
     * Returns the loaded map. All strings - keys and values - are internalized.
     */
    @Override
    public Map<K, V> getMap() {
        return map;
    }

}
