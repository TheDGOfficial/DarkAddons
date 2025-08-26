package gg.darkaddons.profiler;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Represents a {@link Profiler} result.
 * <p>
 * That is, being a snapshot of a state
 * in the {@link Profiler}.
 * <p>
 * The results might be empty if there are no results
 * available from the {@link Profiler}.
 */
public interface ProfilerResults {
    /**
     * Returns the {@link Profiler} that created this result.
     *
     * @return The {@link Profiler} that created this result.
     */
    @NotNull
    Profiler profiler();

    /**
     * Sorts the results in this result by most time spent first
     * to the least time spent last, and returns the new sorted result.
     *
     * @return The new sorted result.
     */
    @NotNull
    ProfilerResults sort();

    /**
     * Filters the result to only remain the results that contain the given search.
     * <p>
     * The returned result might be empty if no results match the given filter.
     *
     * @param search The search string that the results must contain to not be filtered out.
     * @return The new filtered results.
     */
    @NotNull
    ProfilerResults filter(@NotNull final String search);

    /**
     * Remaps the results with the default obfuscation mappings and returns the new results.
     *
     * @return The new remapped results.
     */
    @NotNull
    ProfilerResults remap();

    /**
     * Decompiles the results with the default decompiler and returns the new results.
     *
     * @return The new decompiled results.
     */
    @NotNull
    ProfilerResults decompile();

    /**
     * Returns the underlying individual results.
     *
     * @return The individual results.
     */
    @NotNull
    Collection<ProfilerResult> results();
}
