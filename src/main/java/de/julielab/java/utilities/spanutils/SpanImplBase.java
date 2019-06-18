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

    public void setOffsets(Range<Integer> offsets) {
        this.offsets = offsets;
    }

    public void setBegin(int begin) {
        offsets = Range.between(begin, Math.max(begin, getEnd()));
    }

    public void setEnd(int end) {
        offsets = Range.between(Math.min(getBegin(), end), end);
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
