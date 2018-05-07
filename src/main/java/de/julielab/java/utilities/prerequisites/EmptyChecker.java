package de.julielab.java.utilities.prerequisites;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Supplier;

public class EmptyChecker extends ParameterChecker {

    public EmptyChecker(PrerequisiteChecker prerequisiteChecker, Collection... objects) {
        super(prerequisiteChecker, objects);
    }

    public EmptyChecker(PrerequisiteChecker prerequisiteChecker, Supplier<Collection<?>>[] suppliers) {
        super(prerequisiteChecker, suppliers);
    }

    static void check(PrerequisiteChecker prerequisiteChecker, Collection collection, String name) {
        NullChecker.check(prerequisiteChecker, collection, name);
        if (collection.isEmpty())
            prerequisiteChecker.addErrorMessage("The collection with name \"" + name + "\" is empty.");

    }

    @Override
    public void check() {
        for (int i = 0; i < items.length; i++) {
            Object object = items[i];
            String name = getName(i);
            NullChecker.check(prerequisiteChecker, object, name);
            if (object instanceof Supplier) {
                SupplierChecker.get(prerequisiteChecker, (Supplier<?>) object, name).ifPresent(o -> check(prerequisiteChecker, (Collection) o, name));
            } else if (!(object instanceof Collection)) {
                prerequisiteChecker.addErrorMessage("The item with name \"" + name + "\" is not a collection. But the supplier was given to the EmptyChecker.");
            } else {
                check(prerequisiteChecker, (Collection) object, name);
            }
        }
    }
}
