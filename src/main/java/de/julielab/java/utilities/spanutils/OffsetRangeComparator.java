package de.julielab.java.utilities.spanutils;

import org.apache.commons.lang3.Range;

import java.util.Comparator;

/**
 * A comparator directly for instances of {@link Range}. Sort by begin offset, then by end offset.
 * 
 * @author faessler
 *
 */
public class OffsetRangeComparator implements Comparator<Range<Integer>> {
	@Override
	public int compare(Range<Integer> gm1, Range<Integer> gm2) {
		if (gm1.getMinimum().intValue() == gm2.getMinimum().intValue()) {
			return (gm1.getMaximum().compareTo(gm2.getMaximum()));
		} else {
			return gm1.getMinimum() - gm2.getMinimum();
		}
	}
}
