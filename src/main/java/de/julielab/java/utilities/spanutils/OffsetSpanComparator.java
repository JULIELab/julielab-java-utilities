package de.julielab.java.utilities.spanutils;

import org.apache.commons.lang3.Range;

import java.util.Comparator;

/**
 * A comparator for all classes that implement {@link Span}. Sort by begin
 * offset, then by end offset.
 *
 * @author faessler
 */
public class OffsetSpanComparator implements Comparator<Span> {
    @Override
    public int compare(Span s1, Span s2) {
        Range<Integer> r1 = s1.getOffsets();
        Range<Integer> r2 = s2.getOffsets();
        if (r1.getMinimum().intValue() == r2.getMinimum().intValue()) {
            return (r1.getMaximum().compareTo(r2.getMaximum()));
        } else {
            return r1.getMinimum() - r2.getMinimum();
        }
    }
}
