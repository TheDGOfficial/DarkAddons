package gg.darkaddons.profiler.impl;

import gg.darkaddons.profiler.ProfilerResult;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

final class ProfilerResultImpl implements ProfilerResult {
    @NotNull
    private final String packageName;
    @NotNull
    private final String className;
    @Nullable
    private final String fileName;
    @NotNull
    private final String methodName;
    private final int lineNumber;
    private final long timeSpent;
    @NotNull
    private final StackTraceElement[] fullStack;
    @NotNull
    private final String threadName;

    ProfilerResultImpl(@NotNull final String packageNameIn,
                       @NotNull final String classNameIn,
                       @Nullable final String fileNameIn,
                       @NotNull final String methodNameIn,
                       final int lineNumberIn,
                       final long timeSpentIn,
                       @NotNull final StackTraceElement[] fullStackIn,
                       @NotNull final String threadNameIn) {
        super();

        this.packageName = packageNameIn;
        this.className = classNameIn;
        this.fileName = fileNameIn;
        this.methodName = methodNameIn;
        this.lineNumber = lineNumberIn;
        this.timeSpent = timeSpentIn;
        this.fullStack = Objects.requireNonNull(fullStackIn.clone());
        this.threadName = threadNameIn;
    }

    @NotNull
    @Override
    public final String packageName() {
        return this.packageName;
    }

    @NotNull
    @Override
    public final String className() {
        return this.className;
    }

    @Nullable
    @Override
    public final String fileName() {
        return this.fileName;
    }

    @NotNull
    @Override
    public final String methodName() {
        return this.methodName;
    }

    @Override
    public final int lineNumber() {
        return this.lineNumber;
    }

    @Override
    public final long timeSpent() {
        return this.timeSpent;
    }

    @NotNull
    @Override
    public final StackTraceElement[] fullStack() {
        return Objects.requireNonNull(this.fullStack.clone());
    }

    @NotNull
    @Override
    public final String threadName() {
        return this.threadName;
    }

    @Override
    public final String toString() {
        return "ProfilerResultImpl{" +
            "packageName='" + this.packageName + '\'' +
            ", className='" + this.className + '\'' +
            ", fileName='" + this.fileName + '\'' +
            ", methodName='" + this.methodName + '\'' +
            ", lineNumber=" + this.lineNumber +
            ", timeSpent=" + this.timeSpent +
            ", fullStack=" + Arrays.toString(this.fullStack) +
            ", threadName='" + this.threadName + '\'' +
            '}';
    }
}
