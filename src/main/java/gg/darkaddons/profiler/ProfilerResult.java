package gg.darkaddons.profiler;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a singular {@link Profiler} result.
 */
public interface ProfilerResult {
    /**
     * Returns the package name of this result.
     *
     * @return The package name of this result.
     */
    @NotNull
    String packageName();

    /**
     * Returns the class name of this result.
     *
     * @return The class name of this result.
     */
    @NotNull
    String className();

    /**
     * Returns the source file name of this result,
     * or null if it's not available.
     *
     * @return The source file name of this result,
     * or null if it's not available.
     */
    @Nullable
    String fileName();

    /**
     * Returns the method name of this result.
     *
     * @return The method name of this result.
     */
    @NotNull
    String methodName();

    /**
     * Returns the line number of this result, or -1 if unknown.
     *
     * @return The line number of this result, or -1 if unknown.
     */
    int lineNumber();

    /**
     * Returns the full name of this result, in the format of:
     * <pre>
     * {@code
     * packagename.ClassName.methodName(SourceFileOrClassName.java:linenumber)
     * }
     * </pre>
     * An example result would be
     * <pre>
     * {@code
     * gg.darkaddons.profiler.ProfilerResult.fullName(ProfilerResult.java:74)
     * }
     * </pre>
     *
     * The default implementation uses class name as the source file if it's not available,
     * i.e., the {@link ProfilerResult#fileName()} returns null.
     * <p>
     * The reason {@link ProfilerResult#fileName()} is not always {@link ProfilerResult#className()} plus
     * .java is because of other JVM languages. For example, the class name can be KotlinVersion, but the source
     * file will be KotlinVersion.kt instead of .java.
     *
     * @return The full name of this result.
     */
    @NotNull
    default String fullName() {
        final var className = this.className();

        final var fileName = this.fileName();
        final var sourceFile = null == fileName || "SourceFile".equals(fileName) ? className + ".java" : fileName;

        return this.packageName() + '.' + className + '.' + this.methodName() + '(' + sourceFile + ':' + this.lineNumber() + ')';
    }

    /**
     * Returns total time spent in the code path representing this result.
     *
     * @return The total time spent in the code path representing this result.
     */
    long timeSpent();

    /**
     * Returns the full stack trace of the code path representing this result.
     *
     * @return The full stack trace of the code path representing this result.
     */
    @NotNull
    StackTraceElement[] fullStack();

    /**
     * Returns the name of the thread this result was obtained from.
     *
     * @return The name of the thread this result was obtained from.
     */
    @NotNull
    String threadName();
}
