package gg.darkaddons.profiler;

import gg.darkaddons.profiler.impl.ProfilerImpl;
import org.jetbrains.annotations.NotNull;

/**
 * Represents a Java Profiler implementation.
 */
public interface Profiler {
    /**
     * Starts profiling.
     * <p>
     * Depending on the profiler, results can either be available immediately after this call returns,
     * or after a while.
     * <p>
     * Depending on the profiler, this might slow down the application. Therefore {@link Profiler#endProfiling()}
     * should be called to stop profiling when appropriate.
     */
    void startProfiling();

    /**
     * Polls the results from the Profiler.
     * <p>
     * This should be the results from the current state of the Profiler, like a snapshot.
     * <p>
     * However, depending on the implementation, the profiler might choose to return an older set of results
     * if the last set of results is incomplete or has not enough data, for example.
     * <p>
     * If {@link Profiler#endProfiling()} is not called before calling this method, calling
     * it again might return a different set of results each time. It should, however, return the same set of results
     * if this method is called multiple times after stopping the profiler, since the profiler will not be able to
     * update results further while being stopped.
     * <p>
     * This method will return a new instance every time, but the underlying results might be the same,
     * depending on the implementation.
     *
     * @return The results from the profiler.
     */
    @NotNull
    ProfilerResults pollResults();

    /**
     * Ends profiling.
     * <p>
     * This will make {@link Profiler#pollResults()} method return the same underlying result every call,
     * effectively finalizing the results, till {@link Profiler#startProfiling()} is called again.
     */
    void endProfiling();

    /**
     * Returns a new instance of the default sampling Profiler implementation.
     *
     * @param samplingInterval The sampling interval for the sampling Profiler.
     *
     * @return A new instance of the default sampling Profiler implementation.
     */
    @NotNull
    static Profiler newSamplingProfiler(final long samplingInterval) {
        return ProfilerImpl.createProfilerImpl(samplingInterval);
    }
}
