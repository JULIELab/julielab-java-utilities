package de.julielab.java.utilities.spanutils;

import java.util.Collection;
import java.util.TreeSet;

public class SpanOffsetSet extends TreeSet<Span> {

	/**
	 *
	 */
	private static final long serialVersionUID = -90885720823317587L;

	public SpanOffsetSet() {
		super(new OffsetSpanComparator());
	}

	public SpanOffsetSet(Collection<Span> collection) {
		this();
		this.addAll(collection);
	}

	public Span locate(Span offsets) {
		Span range = this.floor(offsets);
		if (null == range) {
			return this.first();
		}
		return (range.getOffsets().isOverlappedBy(offsets.getOffsets()))? range: this.higher(range);
	}

}
