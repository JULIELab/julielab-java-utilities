package de.julielab.java.utilities.spanutils;

import org.apache.commons.lang3.Range;

import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.SortedMap;
import java.util.TreeMap;

public class OffsetMap<V> extends TreeMap<Range<Integer>, V> {

	private final static OffsetMap<?> EMPTY_OFFSET_MAP = new OffsetMap() {
		public Object put(Range<Integer> key, Object value) {
			throw new UnsupportedOperationException();
		}

		public Object remove(Object key) {
			throw new UnsupportedOperationException();
		}

		public void putAll(Map m) {
			throw new UnsupportedOperationException();
		}

		public void clear() {
			throw new UnsupportedOperationException();
		}
	};

	/**
	 *
	 */
	private static final long serialVersionUID = 8336911838492274123L;

	public OffsetMap() {
		super(new OffsetRangeComparator());
	}

	public OffsetMap(SortedMap<Range<Integer>, ? extends V> other) {
		super(other);
	}

	public OffsetMap(Iterable<? extends Span> items) {
		this();
		items.forEach(this::put);
	}

	/**
	 * Limits a map to entries within a range.
	 * @param range The range containing the limits
	 * @return A map of all entries whose keys lie between the minimum of range, inclusive,
	 * and the maximum of range, also inclusive.
	 */
	public NavigableMap<Range<Integer>, V> restrictTo(Range<Integer> range) {
		Range<Integer> begin = Range.between(range.getMinimum(), range.getMinimum());
		Range<Integer> end = Range.between(range.getMaximum(), range.getMaximum());
		return this.subMap(begin, true, end, true);
	}

    /**
     * Limits a map to entries within a range.
     * @param range The range containing the limits
     * @return A map of all entries whose keys lie between the minimum of range, inclusive,
     * and the maximum of range, also inclusive.
     */
    public NavigableMap<Range<Integer>, V> restrictTo(Span range) {
        return restrictTo(range.getOffsets());
    }

	public NavigableMap<Range<Integer>, V> getOverlapping(Span range) {
		return getOverlapping(range.getOffsets());
	}

	public NavigableMap<Range<Integer>, V> getOverlapping(Range<Integer> range) {
		if (this.isEmpty())
			return emptyOffsetMap();
		// Idea: get the first element within or less of the range and the last within or the first out of the given range. Then remove those entries that are out range.
		Range<Integer> begin = Range.between(range.getMinimum(), range.getMinimum());
		Range<Integer> end = Range.between(range.getMaximum(), range.getMaximum());
		Entry<Range<Integer>, V> floor = this.floorEntry(begin);
		Entry<Range<Integer>, V> ceiling = this.ceilingEntry(end);
		if (floor == null)
			floor = this.firstEntry();
		if (ceiling == null)
			ceiling = this.lastEntry();
		NavigableMap<Range<Integer>,V> subMap = new TreeMap<>(this.subMap(floor.getKey(), true, ceiling.getKey(), true));

		Range<Integer> firstKey = subMap.firstKey();
		Range<Integer> lastKey = subMap.lastKey();
		if (firstKey.getMaximum() <= range.getMinimum())
			subMap.remove(firstKey);
		// we have possible just removed the firstKey element which could have been the only element of the submap
		if (subMap.isEmpty())
			return emptyOffsetMap();
		if (lastKey.getMinimum() >= range.getMaximum())
			subMap.remove(lastKey);
		return subMap;
	}
	
	@SuppressWarnings("unchecked")
	public static <V> OffsetMap<V> emptyOffsetMap(){
		return (OffsetMap<V>) EMPTY_OFFSET_MAP;
	}

	@SuppressWarnings("unchecked")
	public V put(Span span) {
		return put(span.getOffsets(), (V) span);
	}

	/**
	 * <p>Returns the first value in the map that intersects the given range and does not have a smaller intersection than following elements.</p>
	 * @param range The range to check for intersections.
	 * @return The first value with the largest intersection with the given range or null if there is no intersecting element.
	 */
    public V getFirstLargestIntersectionValue(Range<Integer> range) {
        final NavigableMap<Range<Integer>, V> overlapping = getOverlapping(range);
        Range<Integer> largestOverlap = null;
        int largestLength = 0;
        for (Range<Integer> key : overlapping.keySet()) {
            final Range<Integer> intersection = key.intersectionWith(range);
            int length = intersection.getMaximum() - intersection.getMinimum();
            if (largestOverlap == null || largestLength < length) {
                largestOverlap = key;
                largestLength = length;
            }
        }
        return largestOverlap != null ? overlapping.get(largestOverlap) : null;
    }

    public V getFirstLargestOverlappingValue(Range<Integer> range) {
        final NavigableMap<Range<Integer>, V> overlapping = getOverlapping(range);
        Range<Integer> largestOverlap = null;
        int largestLength = 0;
        for (Range<Integer> key : overlapping.keySet()) {
            int length = key.getMaximum() - key.getMinimum();
            if (largestOverlap == null || largestLength < length) {
                largestOverlap = key;
                largestLength = length;
            }
        }
        return largestOverlap != null ? overlapping.get(largestOverlap) : null;
    }
	
}
