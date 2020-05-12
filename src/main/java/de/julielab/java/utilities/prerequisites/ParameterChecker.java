package de.julielab.java.utilities.prerequisites;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;

/**
 * Abstract class for checks of items that will most probably method or configuration parameters. Extending
 * classes check for <tt>null</tt>, if a collection is empty or possibly others.
 */
public abstract class ParameterChecker {
    private static final Object[] NULL_ITEM = new Object[]{null};

    protected Object[] items;
    protected List<String> names;
    protected PrerequisiteChecker prerequisiteChecker;

    public ParameterChecker(PrerequisiteChecker prerequisiteChecker, Object... items) {
        this.prerequisiteChecker = prerequisiteChecker;
        this.items = items != null ? items : NULL_ITEM;
        this.names = Collections.emptyList();
    }

    public void withNames(Queue<String> newNames) {
        if (this.names.isEmpty() && !newNames.isEmpty())
            this.names = new ArrayList<>();
        while (this.names.size() < items.length)
            this.names.add(newNames.poll());
    }

    public abstract void check();

    protected String getName(int index) {
        if (index < names.size())
            return names.get(index);
        if (index < items.length)
            return null;
        throw new ArrayIndexOutOfBoundsException("Given index: " + index + ". There are " + items.length + " items with " + names.size() + " names.");
    }
}
