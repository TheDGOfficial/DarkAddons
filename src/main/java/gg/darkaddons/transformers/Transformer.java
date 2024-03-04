package gg.darkaddons.transformers;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.tree.ClassNode;

/**
 * Represents a {@link ClassNode} transformer.
 */
public interface Transformer {
    /**
     * Returns an array containing the list of fully qualified class names that can be transformed with this transformer.
     *
     * @return An array containing the list of fully qualified class names that can be transformed with this transformer.
     */
    String @NotNull [] getApplicableClasses();

    /**
     * Transforms a class matching one of the values in {@link Transformer#getApplicableClasses()}.
     *
     * @param matchedClass The index of the matched value in {@link Transformer#getApplicableClasses()}.
     * @param classNode The {@link ClassNode} to transform.
     */
    void transform(final int matchedClass, @NotNull final ClassNode classNode);
}
