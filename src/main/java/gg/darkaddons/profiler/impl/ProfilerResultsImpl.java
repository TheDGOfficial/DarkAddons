package gg.darkaddons.profiler.impl;

import gg.darkaddons.PublicUtils;
import gg.darkaddons.profiler.Profiler;
import gg.darkaddons.profiler.ProfilerResult;
import gg.darkaddons.profiler.ProfilerResults;
import gg.darkaddons.profiler.impl.mappings.MethodMapping;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Stream;

final class ProfilerResultsImpl implements ProfilerResults {
    @NotNull
    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    private final Profiler profiler;
    @NotNull
    private final Collection<ProfilerResult> results;

    ProfilerResultsImpl(@NotNull final Profiler profilerIn,
                        @NotNull final Collection<ProfilerResult> resultsIn) {
        super();

        this.profiler = profilerIn;
        this.results = Collections.unmodifiableCollection(resultsIn);
    }

    @NotNull
    @Override
    public final Profiler profiler() {
        return this.profiler;
    }

    @NotNull
    private final Stream<ProfilerResult> resultStream() {
        return Objects.requireNonNull(this.results.parallelStream());
    }

    @NotNull
    private static final Collection<ProfilerResult> toCollection(@NotNull final Stream<ProfilerResult> resultStream) {
        return Arrays.asList(resultStream.toArray(ProfilerResult[]::new));
    }

    private static final long timeSpent(@Nullable final ProfilerResult profilerResult) {
        return Objects.requireNonNull(profilerResult).timeSpent();
    }

    @NotNull
    @Override
    public final ProfilerResults sort() {
        return new ProfilerResultsImpl(this.profiler, ProfilerResultsImpl.toCollection(this.resultStream().sorted(Comparator.comparingLong(ProfilerResultsImpl::timeSpent).reversed())));
    }

    @NotNull
    @Override
    public final ProfilerResults filter(@NotNull final String search) {
        return new ProfilerResultsImpl(this.profiler, ProfilerResultsImpl.toCollection(this.resultStream().filter((@NotNull final ProfilerResult profilerResult) -> Objects.requireNonNull(profilerResult).fullName().contains(search))));
    }

    @NotNull
    private static final ProfilerResult remapResult(@NotNull final ProfilerResult profilerResult) {
        final var methodName = profilerResult.methodName();
        final var methodMapping = MethodMapping.MethodMappingsHolder.lookup(methodName);

        final var remappedMethodName = null == methodMapping ? methodName : methodMapping.getDeobfName();
        if (remappedMethodName.startsWith("func_")) {
            ProfilerResultsImpl.LOGGER.warn("[mappings] can't find mapping for method " + remappedMethodName + " (in class " + profilerResult.packageName() + '.' + profilerResult.className() + ')');
        }

        final var remappedFullStack = profilerResult.fullStack();
        final var remappedFullStackLength = remappedFullStack.length;

        for (var i = 0; i < remappedFullStackLength; ++i) {
            final var elem = Objects.requireNonNull(remappedFullStack[i]);

            final var stackMethodName = elem.getMethodName();
            final var stackMethodMapping = MethodMapping.MethodMappingsHolder.lookup(stackMethodName);

            final var stackRemappedMethodName = null == stackMethodMapping ? stackMethodName : stackMethodMapping.getDeobfName();
            if (stackRemappedMethodName.startsWith("func_")) {
                ProfilerResultsImpl.LOGGER.warn("[mappings] can't find mapping for method " + stackRemappedMethodName + " (in class " + elem.getClassName() + ')');
            }

            remappedFullStack[i] = new StackTraceElement(elem.getClassName(), stackRemappedMethodName, elem.getFileName(), elem.getLineNumber());
        }

        return new ProfilerResultImpl(profilerResult.packageName(), profilerResult.className(), profilerResult.fileName(), remappedMethodName, profilerResult.lineNumber(), profilerResult.timeSpent(), remappedFullStack, profilerResult.threadName());
    }

    @NotNull
    @Override
    public final ProfilerResults remap() {
        return new ProfilerResultsImpl(this.profiler, ProfilerResultsImpl.toCollection(this.resultStream().map((@NotNull final ProfilerResult profilerResult) -> ProfilerResultsImpl.remapResult(Objects.requireNonNull(profilerResult)))));
    }

    @NotNull
    @Override
    public final ProfilerResults decompile() {
        throw new UnsupportedOperationException("not supported yet"); // TODO add support
    }

    @NotNull
    @Override
    public final Collection<ProfilerResult> results() {
        return this.results;
    }

    @Override
    public final String toString() {
        return "ProfilerResultsImpl{" +
            "profiler=" + this.profiler +
            ", results=" + this.results +
            '}';
    }
}
