package de.julielab.java.utilities.prerequisites;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.joining;

/**
 * This is a class meant for chaining together calls like <code>PrerequisiteChecker.checkThat().notNull(ob1, ob2).notEmpty(coll).withNames("ob1", "ob2", "coll").execute()</code>.
 * In case that any of the checks fail on <code>execute()</code>, a message will be recorded for each failed check.
 * Then, an <code>IllegalArgumentException</code> is thrown explaining what went wrong.
 */
public class PrerequisiteChecker {

    public static final String PREREQUISITE_CHECKS_ENABLED = "de.julielab.prerequisitechecksenabled";
    private static final PrerequisiteChecker DISABLED_CHECKER = new PrerequisiteChecker(false);
    private final boolean enabled;
    /**
     * Error messages for state checks.
     */
    private Set<String> errorMessages;
    private List<ParameterChecker> checkers;

    private PrerequisiteChecker() {
        enabled = Boolean.parseBoolean(System.getProperty(PREREQUISITE_CHECKS_ENABLED));
        errorMessages = new LinkedHashSet<>();
        checkers = new ArrayList<>();
    }

    /**
     * Only here to create {@link #DISABLED_CHECKER}
     *
     * @param ignored Ignored. Internally always set to <code>false</code>.
     */
    private PrerequisiteChecker(boolean ignored) {
        this.enabled = false;
    }

    public static PrerequisiteChecker checkThat() {
        return Boolean.parseBoolean(System.getProperty(PREREQUISITE_CHECKS_ENABLED)) ? new PrerequisiteChecker() : DISABLED_CHECKER;
    }

    void addErrorMessage(String message) {
        errorMessages.add(message);
    }

    /**
     * Sets the names for all items previously added to the checker, possibly in multiple calls. The number of names may
     * lower or even higher than the number of given items. In the first case, the names will be assigned to the checked
     * items from left to right in a continuous manner while the remaining items will receive <tt>null</tt> as their name.
     * In the second case, all superfluous names will simply be ignored.
     *
     * @param names The names to give to the last list of items to check
     * @return This object for chaining.
     */
    public PrerequisiteChecker withNames(String... names) {
        if (enabled) {
            if (checkers.isEmpty())
                throw new IllegalStateException("No items have been added to the prerequisite checker, cannot assign names.");
            Deque<String> nameQueue = Arrays.stream(names).collect(Collectors.toCollection(ArrayDeque::new));
            checkers.forEach(c -> {
                c.withNames(nameQueue);
            });
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
