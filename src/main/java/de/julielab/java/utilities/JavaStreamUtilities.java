package de.julielab.java.utilities;

import java.util.function.Function;
import java.util.function.Predicate;

public class JavaStreamUtilities {
    /**
     * A predicate that tests true for all values that equal the first value it saw.
     *
     * @param valueFunction A function to retrieve the value.
     * @param <T>           The input type.
     * @param <E>           The value type for comparison.
     * @return The predicate.
     */
    public static <T, E> Predicate equalsFirstSeenValue(Function<T, E> valueFunction) {
        return new Predicate<T>() {
            private E firstValue = null;

            @Override
            public boolean test(T entry) {
                E value = valueFunction.apply(entry);
                if (firstValue == null) {
                    firstValue = value;
                    return true;
                }
                return value.equals(firstValue);
            }
        };
    }
}
