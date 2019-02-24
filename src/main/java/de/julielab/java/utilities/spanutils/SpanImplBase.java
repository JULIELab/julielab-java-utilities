package de.julielab.java.utilities.spanutils;

import org.apache.commons.lang3.Range;

/**
 * This helper class represents a span of offsets, for example for the coverage of a specific streak of characters in a text.
 * It is mainly supposed to serve as an extension point for classes that carry more information associated with the span, such as text annotations.
 */
public class SpanImplBase implements Span {

	protected Range<Integer> offsets;
	
	public SpanImplBase(Range<Integer> offsets) {
		this.offsets = offsets;
	}

	@Override
	public Range<Integer> getOffsets() {
		return offsets;
	}

	@Override
	public int getBegin() {
		return offsets.getMinimum();
	}

	@Override
	public int getEnd() {
		return offsets.getMaximum();
	}
}
