package de.julielab.java.utilities.spanutils;

import org.apache.commons.lang3.Range;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.TreeMap;

public class SpanOffsetMap<V> extends TreeMap<Span, V> {

	private final static SpanOffsetMap<?> EMPTY_OFFSET_MAP = new SpanOffsetMap<>();
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8336911838492274123L;

	public SpanOffsetMap() {
		super(new OffsetSpanComparator());
	}

	/**
	 * Limits a map to entries within a range.
	 * @param range The range containing the limits
	 * @return A map of all entries whose keys lie between the minimum of range, inclusive,
	 * and the maximum of range, also inclusive.
	 */
	public NavigableMap<Span, V> restrictTo(Span range) {
		Span begin = new SpanImplBase(Range.between(range.getBegin(), range.getBegin()));
		Span end = new SpanImplBase(Range.between(range.getEnd(), range.getEnd()));
		return this.subMap(begin, true, end, true);
	}
	
	public NavigableMap<Span, V> getOverlapping(Span range) {
		if (this.isEmpty())
			return emptyOffsetMap();
		// Idea: get the first element within or less of the range and the last within or the first out of the given range. Then remove those entries that are out range.
		Span begin = new SpanImplBase(Range.between(range.getBegin(), range.getBegin()));
		Span end = new SpanImplBase(Range.between(range.getEnd(), range.getEnd()));
		Entry<Span, V> floor = this.floorEntry(begin);
		Entry<Span, V> ceiling = this.ceilingEntry(end);
		if (floor == null)
			floor = this.firstEntry();
		if (ceiling == null)
			ceiling = this.lastEntry();
		NavigableMap<Span,V> subMap = new TreeMap<>(this.subMap(floor.getKey(), true, ceiling.getKey(), true));

		Span firstKey = subMap.firstKey();
		Span lastKey = subMap.lastKey();
		if (firstKey.getEnd() <= range.getBegin())
			subMap.remove(firstKey);
		// we have possible just removed the firstKey element which could have been the only element of the submap
		if (subMap.isEmpty())
			return emptyOffsetMap();
		if (lastKey.getBegin() >= range.getEnd())
			subMap.remove(lastKey);
		return subMap;
	}
	
	@SuppressWarnings("unchecked")
	public static <V> SpanOffsetMap<V> emptyOffsetMap(){
		return (SpanOffsetMap<V>) EMPTY_OFFSET_MAP;
	}

	@SuppressWarnings("unchecked")
	public V put(Span span) {
		return put(span, (V) span);
	}
	
}
