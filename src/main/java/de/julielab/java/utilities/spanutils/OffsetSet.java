package de.julielab.java.utilities.spanutils;

import org.apache.commons.lang3.Range;

import java.util.Collection;
import java.util.TreeSet;

public class OffsetSet extends TreeSet<Range<Integer>> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -90885720823317587L;

	public OffsetSet() {
		super(new OffsetRangeComparator());
	}

	public OffsetSet(Collection<Range<Integer>> collection) {
		this();
		this.addAll(collection);
	}

	public Range<Integer> locate(Range<Integer> offsets) {
		Range<Integer> range = this.floor(offsets);
		if (null == range) {
			return this.first();
		}
		return (range.isOverlappedBy(offsets))? range: this.higher(range);
	}

	public Range<Integer> locate(Span offsets) {
        return locate(offsets.getOffsets());
	}

}
