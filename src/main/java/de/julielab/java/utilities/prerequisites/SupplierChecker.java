package de.julielab.java.utilities.prerequisites;

import java.util.Optional;
import java.util.function.Supplier;

public class SupplierChecker {


    public static Optional<?> get(PrerequisiteChecker prerequisiteChecker, Supplier<?> supplier, String name) {
        try {
            Object o = supplier.get();
            if (o == null)
                prerequisiteChecker.addErrorMessage("The supplier with name \"" + name + "\" returned null.");
            return Optional.ofNullable(o);
        } catch (NullPointerException e) {
            prerequisiteChecker.addErrorMessage("The supplier with name \"" + name + "\" caused a NullPointerException.");
        }
        return Optional.empty();
    }

}
