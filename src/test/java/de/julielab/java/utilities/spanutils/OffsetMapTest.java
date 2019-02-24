
package de.julielab.java.utilities.spanutils;

import org.apache.commons.lang3.Range;
import org.junit.Test;

import java.util.NavigableMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class OffsetMapTest {

	@Test
	public void testGetOverlapping() {
		OffsetMap<String> map = new OffsetMap<>();
		map.put(Range.between(5, 10), "first");
		map.put(Range.between(11, 15), "middle");
		map.put(Range.between(16, 20), "last");

		assertEquals("first", map.getOverlapping(Range.between(5, 10)).values().stream().findFirst().get());
		assertEquals("middle", map.getOverlapping(Range.between(10, 14)).values().stream().findFirst().get());
		assertEquals("middle", map.getOverlapping(Range.between(11, 15)).values().stream().findFirst().get());
		assertEquals("last", map.getOverlapping(Range.between(17, 25)).values().stream().findFirst().get());

		assertFalse(map.getOverlapping(Range.between(0, 4)).values().stream().findFirst().isPresent());
		assertFalse(map.getOverlapping(Range.between(21, 25)).values().stream().findFirst().isPresent());
	}

	@Test
	public void testSubMap() {
		OffsetMap<String> map = new OffsetMap<>();
		map.put(Range.between(300, 400), "test");
		map.put(Range.between(300, 400), "test2");
		NavigableMap<Range<Integer>, String> subMap = map.subMap(Range.between(300, 400), true, Range.between(300, 400),
				true);
		assertEquals(1, subMap.size());
	}
}
