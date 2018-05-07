package de.julielab.java.utilities.prerequisites;

public abstract class ParameterChecker {
    private static final Object[] EMPTY_OBJECTS = new Object[0];
    private static final String[] EMPTY_NAMES = new String[0];

    protected Object[] items;
    protected String[] names;
    protected PrerequisiteChecker prerequisiteChecker;

    public ParameterChecker(PrerequisiteChecker prerequisiteChecker, Object... items) {
        this.prerequisiteChecker = prerequisiteChecker;
        this.items = items != null ? items : EMPTY_OBJECTS;
        this.names = EMPTY_NAMES;
    }

    public void withNames(String[] names) {
        this.names = names;
    }

    public abstract void check();

    protected String getName(int index) {
        if (index < names.length)
            return names[index];
        if (index < items.length)
            return null;
        throw new ArrayIndexOutOfBoundsException("Given index: " + index + ". There are " + items.length + " items with " + names.length + " names.");
    }
}
