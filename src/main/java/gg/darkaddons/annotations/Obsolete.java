package gg.darkaddons.annotations;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An extension annotation to the {@link Deprecated} annotation,
 * that provides more information.
 * <p>
 * Currently only for documentation purposes in addition to the {@link Deprecated}
 * tag. No IDE support or even compiler warnings will be generated from the usages
 * of methods annotated by this annotation (yet).
 */
// TODO make an annotation processor to warn usages of Obsolete items
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.ANNOTATION_TYPE, ElementType.FIELD, ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.PACKAGE})
public @interface Obsolete {
    /**
     * Returns the reason this element was made obsolete.
     *
     * @return The reason this element was made obsolete.
     */
    @NotNull
    String reason();

    /**
     * Backport of Java 9 {@link Deprecated} since method.
     *
     * @return The version this element was made obsolete.
     */
    @NotNull
    String since();

    /**
     * Backport of Java 9 {@link Deprecated} forRemoval method.
     *
     * @return True if this obsolete element is planned to be removed in the future.
     */
    boolean forRemoval();

    /**
     * Like since, but instead represents the version the element will be
     * removed, if {@link Obsolete#forRemoval()} is true.
     *
     * @return The version the element is scheduled to be removed at.
     */
    @NotNull
    String willBeRemovedAt();

    /**
     * Returns the suggested replacement for this obsolete element.
     *
     * @return The suggested replacement for this obsolete element.
     */
    @NotNull
    String replaceWith();
}
