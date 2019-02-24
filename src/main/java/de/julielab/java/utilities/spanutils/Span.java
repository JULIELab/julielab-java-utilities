package de.julielab.java.utilities.spanutils;

import org.apache.commons.lang3.Range;

public interface Span {
    Range<Integer> getOffsets();

    int getBegin();

    int getEnd();
}
