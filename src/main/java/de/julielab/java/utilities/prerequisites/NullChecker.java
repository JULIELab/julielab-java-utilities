package de.julielab.java.utilities.prerequisites;

import java.util.function.Supplier;

public class NullChecker<T> extends ParameterChecker {


    public NullChecker(PrerequisiteChecker prerequisiteChecker, T... objects) {
        super(prerequisiteChecker, objects);
    }

    static void check(PrerequisiteChecker prerequisiteChecker, Object item, String name) {
        if (item == null)
            prerequisiteChecker.addErrorMessage("The object with name \"" + name + "\" is null.");

    }

    @Override
    public void check() {
        if (items.length == 0)
            check(prerequisiteChecker, null, getName(0));
        for (int i = 0; i < items.length; i++) {
            Object object = items[i];
            String name = getName(i);
            check(prerequisiteChecker, object, name);
            if (object instanceof Supplier)
                SupplierChecker.get(prerequisiteChecker, (Supplier<?>) object, name).ifPresent(o -> check(prerequisiteChecker, o, name));
        }
    }

}
