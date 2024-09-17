package gg.darkaddons;

import gg.darkaddons.annotations.bytecode.Bridge;
import gg.darkaddons.annotations.bytecode.Synthetic;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;

/**
 * A wrapper of {@link AccessibleObject}s, allowing you to grant access
 * inside a try-with resources section, then the access will be revoked once
 * the block ends, in a try-finally block, even if you return or throw exceptions
 * inside the try-with resources block.
 *
 * @param <T> The type of the class extending {@link AccessibleObject}.
 */
public final class AccessibleObjectResource<T extends AccessibleObject> implements AutoCloseable {
    @NotNull
    private final T accessibleObject;

    /**
     * Creates a new wrapper resource for the given {@link Field}.
     *
     * @param field The {@link Field} to create the wrapper for.
     */
    @SuppressWarnings("unchecked")
    public AccessibleObjectResource(@NotNull final Field field) {
        super();

        this.accessibleObject = (T) field;
    }

    /**
     * Grants access to the {@link AccessibleObject} this object represents.
     * <p>
     * The access will be revoked when the {@link AccessibleObjectResource#close()}
     * method is called, including automatic calls from the compiler via try-with
     * resources syntax.
     */
    @Synthetic
    @Bridge
    @SuppressWarnings("PublicMethodNotExposedInInterface")
    public final void grantAccess() {
        this.modifyAccessibility(true);
    }

    @Synthetic
    @Bridge
    private final void restoreAccess() {
        this.modifyAccessibility(false);
    }

    @Synthetic
    @Bridge
    private final void modifyAccessibility(final boolean accessible) {
        this.accessibleObject.setAccessible(accessible);
    }

    @Synthetic
    @Bridge
    @Override
    public final void close() {
        this.restoreAccess();
    }

    @Override
    public final String toString() {
        return "AccessibleObjectResource{" +
                "accessibleObject=" + this.accessibleObject +
                '}';
    }
}
