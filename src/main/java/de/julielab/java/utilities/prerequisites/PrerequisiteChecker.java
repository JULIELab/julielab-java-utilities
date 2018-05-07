package de.julielab.java.utilities.prerequisites;

import java.util.*;
import java.util.function.Supplier;

import static java.util.stream.Collectors.joining;


public class PrerequisiteChecker {

    public static final String PREREQUISITE_CHECKS_ENABLED = "de.julielab.prerequisitechecksenabled";
    private static final PrerequisiteChecker DISABLED_CHECKER = new PrerequisiteChecker(false);
    private final boolean enabled;
    /**
     * Error messages for state checks.
     */
    private Set<String> errorMessages;
    private List<ParameterChecker> checkers;

    public PrerequisiteChecker() {
        enabled = Boolean.parseBoolean(System.getProperty(PREREQUISITE_CHECKS_ENABLED));
        errorMessages = new LinkedHashSet<>();
        checkers = new ArrayList<>();
    }

    /**
     * Only here to create {@link #DISABLED_CHECKER}
     *
     * @param enabled Ignored. Internally always set to <code>false</code>.
     */
    private PrerequisiteChecker(boolean enabled) {
        this.enabled = false;
    }

    public static PrerequisiteChecker checkThat() {
        return Boolean.parseBoolean(System.getProperty(PREREQUISITE_CHECKS_ENABLED)) ? new PrerequisiteChecker() : DISABLED_CHECKER;
    }

    void addErrorMessage(String message) {
        errorMessages.add(message);
    }

    /**
     * Sets the names for the items of the last {@link #notNull(Object...)} or {@link #notEmpty(Collection[])}.
     * There might be less arguments than items were previously given. In this case the remaining items will have no name.
     *
     * @param names The names to give to the last list of items to check
     * @return This object for chaining.
     */
    public PrerequisiteChecker withNames(String... names) {
        if (enabled) {
            if (checkers.isEmpty())
                throw new IllegalStateException("No items have been added to the prerequisite checker, cannot assign names.");
            checkers.get(checkers.size() - 1).withNames(names);
        }
        return this;
    }

    public PrerequisiteChecker notNull(Object... objects) {
        if (enabled)
            checkers.add(new NullChecker(this, objects));
        return this;
    }

    public PrerequisiteChecker notEmpty(Collection<?>... collections) {
        if (enabled)
            checkers.add(new EmptyChecker(this, collections));
        return this;
    }

    public PrerequisiteChecker supplyNotEmpty(Supplier<Collection<?>>... collections) {
        if (enabled)
            checkers.add(new EmptyChecker(this, collections));
        return this;
    }

    public PrerequisiteChecker supplyNotNull(Supplier<?>... objectSuppliers) {
        if (enabled)
            checkers.add(new NullChecker<>(this, objectSuppliers));
        return this;
    }

    /**
     * Checks for null items and, if found, generates an error message.
     */
    public void execute() {
        if (enabled) {
            for (ParameterChecker checker : checkers)
                checker.check();
            if (!errorMessages.isEmpty()) {
                throw new IllegalArgumentException("There were parameter check failures: " +
                        System.getProperty("line.separator") + errorMessages.stream().collect(joining(System.getProperty("line.separator"))));
            }
        }
    }


    private enum ObjectType {OBJECT, COLLECTION, SUPPLIER}


}
