package de.julielab.java.utilities.index;

import com.google.gson.Gson;
import de.julielab.java.utilities.IOStreamUtilities;
import de.julielab.java.utilities.UriUtilities;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.net.URI;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * <p>Base class for addon terms (i.e. terms to be added to some key term, like synonyms or hypernyms) that uses a HashMap.</p>
 * <p>Subclasses of this class use other data structures to store and retrieve the addon terms. Useful for large numbers of such terms.</p>
 */
public class StringArrayMapProvider implements IStringArrayMapProvider {
    protected final Logger log;
    protected Map<String, String[]> map;
    private String multiValueDelimiterRegex = "[|,;]";
    private Set<String> eligibleKeys = Collections.emptySet();
    private int[] keyIndices = new int[]{0};
    private int[] valueIndices = new int[]{1};

    public StringArrayMapProvider(Logger log) {
        this.log = log;
        map = new HashMap<>();
    }

    public Set<String> getEligibleKeys() {
        return eligibleKeys;
    }

    public void setEligibleKeys(Set<String> eligibleKeys) {
        this.eligibleKeys = eligibleKeys;
    }

    public String getMultiValueDelimiterRegex() {
        return multiValueDelimiterRegex;
    }

    public void setMultiValueDelimiterRegex(String multiValueDelimiterRegex) {
        this.multiValueDelimiterRegex = multiValueDelimiterRegex;
    }

    protected void put(String term, String[] addonArray) {
        map.put(term, addonArray);
    }

    @Override
    public void load(URI uri) throws IndexCreationException {
        log.info("Loading key-multiple values mapping from " + uri);
        InputStream inputStream;
        try {
            inputStream = UriUtilities.getInputStreamFromUri(uri);
            load(inputStream);
        } catch (IndexCreationException e) {
            log.error("Could not create index from URI {}", uri, e);
            throw e;
        } catch (Exception e) {
            throw new IndexCreationException("Could not create index from " + uri + "", e);
        }
    }

    public int[] getValueIndices() {
        return valueIndices;
    }

    @Override
    public void setValueIndices(int... valueIndices) {
        this.valueIndices = valueIndices;
    }

    public int[] getKeyIndices() {
        return keyIndices;
    }

    @Override
    public void setKeyIndices(int... keyIndex) {
        this.keyIndices = keyIndex;
    }

    @Override
    public void load(InputStream inputStream) throws IndexCreationException {
        int addons = 0;
        int lineNr = 0;
        int maxIndex = Math.max(IntStream.of(keyIndices).max().getAsInt(), IntStream.of(valueIndices).max().getAsInt());
        String line = null;
        try (final BufferedReader br = IOStreamUtilities.getReaderFromInputStream(inputStream)) {
            final Iterator<String> lineIt = br.lines().iterator();
            while (lineIt.hasNext()) {
                ++lineNr;
                line = lineIt.next();
                if (line.trim().length() == 0 || line.startsWith("#"))
                    continue;
                String[] mapping = line.split("\t", maxIndex + 2);
                if (mapping.length <= maxIndex)
                    throw new IllegalArgumentException("Format problem with string array map line " + line + ": " + (maxIndex + 1) + " columns are expected but got " + mapping.length + ".");

                Stream<String> keys = Stream.empty();
                for (int keyIndex : keyIndices)
                    keys = splitFieldIntoInternedStrings(mapping, null, keyIndex);

                if (!eligibleKeys.isEmpty())
                    keys = keys.filter(eligibleKeys::contains);

                Stream<String> values = Stream.empty();
                for (int valueIndex : valueIndices)
                    values = splitFieldIntoInternedStrings(mapping, values, valueIndex);

                final String[] finalValues = values.toArray(String[]::new);
                addons += finalValues.length;
                keys.peek(key -> log.trace("key: {} -> values: {}", key, finalValues)).forEach(key -> put(key, finalValues));
                if (log.isDebugEnabled() && lineNr % 10000 == 0) {
                    log.debug("Processed {} lines", lineNr);
                }
            }
            log.info("Loaded {} values for {} keys.", addons, map.size());
        } catch (Exception e) {
            log.error("Exception at line {} of input file: {}", lineNr, line);
            throw new IndexCreationException(e);
        }
    }

    @NotNull
    private Stream<String> splitFieldIntoInternedStrings(String[] mapping, Stream<String> values, int valueIndex) {
        values = values != null ? values : Stream.empty();
        // we use internalization to reduce memory
        // requirements
        if (mapping[valueIndex].startsWith("[") && mapping[valueIndex].endsWith("]")) {
            // This looks like a JSON array
            Gson gson = new Gson();
            values = Stream.concat(values, Arrays.stream(gson.fromJson(mapping[valueIndex], String[].class)));
        } else {
            values = Stream.concat(values, Arrays.stream(mapping[valueIndex].split(multiValueDelimiterRegex)));
        }
        return values.map(String::trim).filter(Predicate.not(String::isEmpty)).map(String::intern);
    }


    @Override
    public Map<String, String[]> getMap() {
        return map;
    }

}
