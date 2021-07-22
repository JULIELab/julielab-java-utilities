package de.julielab.java.utilities.spanutils;

import org.apache.commons.lang3.Range;
import org.junit.jupiter.api.Test;

import java.util.Comparator;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class OffsetSetTest {

	@Test
	public void testLocate() {
		OffsetSet offsets = new OffsetSet();
		offsets.add(Range.between(0, 104));
		offsets.add(Range.between(105, 286));
		offsets.add(Range.between(287, 332));
		
		Range<Integer> result = offsets.locate(Range.between(105, 114));
		Range<Integer> expected = Range.between(105, 286);
		assertEquals(expected, result);
	}

	@Test
	public void testLocateFirst() {
		OffsetSet offsets = new OffsetSet();
		offsets.add(Range.between(0, 104));
		offsets.add(Range.between(105, 286));
		offsets.add(Range.between(287, 332));
		
		Range<Integer> result = offsets.locate(Range.between(0, 5));
		Range<Integer> expected = Range.between(0, 104);
		assertEquals(expected, result);
	}
	
	@Test
	public void testComparator() {
		OffsetSet offsets = new OffsetSet();
		Comparator<? super Range<Integer>> c = offsets.comparator();
		Range<Integer> firstRange = Range.between(287, 332);
		Range<Integer> secondRange = Range.between(287, 332);
		assertEquals(0, c.compare(firstRange,secondRange));
	}

}
