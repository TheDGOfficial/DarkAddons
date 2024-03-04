package gg.darkaddons.profiler.impl;

import com.sun.management.GarbageCollectionNotificationInfo;
import gg.darkaddons.DarkAddons;
import gg.darkaddons.PublicUtils;
import gg.darkaddons.profiler.Profiler;
import gg.darkaddons.profiler.ProfilerResult;
import gg.darkaddons.profiler.ProfilerResults;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public final class ProfilerImpl implements Profiler {
    @NotNull
    private static final StackTraceElement[] EMPTY_STACK_TRACE_ELEMENT_ARRAY = new StackTraceElement[0];
    private static boolean lineNumbers = true;
    @Nullable
    private static String threadFilter;

    @NotNull
    private static final AtomicLong counter = new AtomicLong();

    @NotNull
    private final Collection<ProfilerResult> results;

    @NotNull
    private final Map<String, ProfilerResult> quickAccess;

    @NotNull
    private final AtomicBoolean profiling = new AtomicBoolean();

    private final long samplingInterval;

    private long totalRuntime;
    @Nullable
    private NotificationListener notificationListener;
    private long gcTime;
    private static boolean cpuBoundOnly;

    public static final void setCPUBoundOnly(final boolean newCpuBoundOnly) {
        ProfilerImpl.cpuBoundOnly = newCpuBoundOnly;
    }

    private static boolean uniquifyByThread;

    public static final void setUniquifyByThread(final boolean newUniquifyByThread) {
        ProfilerImpl.uniquifyByThread = newUniquifyByThread;
    }

    private static boolean uniquifyByStack;

    public static final void setUniquifyByStack(final boolean newUniquifyByStack) {
        ProfilerImpl.uniquifyByStack = newUniquifyByStack;
    }

    private static boolean uniquifyByThreadId;

    public static final void setUniquifyByThreadId(final boolean newUniquifyByThreadId) {
        ProfilerImpl.uniquifyByThreadId = newUniquifyByThreadId;
    }

    private ProfilerImpl(final long samplingIntervalIn) {
        super();

        this.samplingInterval = samplingIntervalIn;

        this.results = new ArrayList<>(100);
        this.quickAccess = new HashMap<>(PublicUtils.calculateHashMapCapacity(100));
    }

    public static final void setLineNumbers(final boolean newLineNumbers) {
        ProfilerImpl.lineNumbers = newLineNumbers;
    }

    public static final void setThreadFilter(@Nullable final String newThreadFilter) {
        ProfilerImpl.threadFilter = newThreadFilter;
    }

    public static final ProfilerImpl createProfilerImpl(final long samplingIntervalIn) {
        return new ProfilerImpl(samplingIntervalIn);
    }

    private final void profilingLoop() {
        final var threadMXBean = Objects.requireNonNull(Objects.requireNonNull(ManagementFactory.getThreadMXBean()));
        final var mcThreadId = DarkAddons.getMcThreadId();
        final var currentThread = Thread.currentThread();
        final var currentThreadId = PublicUtils.threadId(currentThread);
        final var interval = this.samplingInterval;
        while (!currentThread.isInterrupted() && this.profiling.get()) {
            try {
                // noinspection BusyWait
                Thread.sleep(interval);
            } catch (final InterruptedException ignored) {
                currentThread.interrupt();
            }

            this.totalRuntime += interval;

            // Fast-path optimization for Client thread, use the thread ID to only dump stack of Client thread instead of all the threads. We don't do this for other threads as the filter can match more than one thread and due to safe-points and JIT optimizations dumping all threads doesn't take much longer either, this optimization is only really visible when the sampling interval is 1 millisecond, where the profiler adds a bit of overhead without the optimization.
            final var threadInfos = "Client thread".equals(ProfilerImpl.threadFilter) ? new ThreadInfo[]{threadMXBean.getThreadInfo(mcThreadId, Integer.MAX_VALUE)} : threadMXBean.dumpAllThreads(false, false);

            for (final var threadInfo : Objects.requireNonNull(threadInfos)) {
                Objects.requireNonNull(threadInfo);

                // Do not profile the profiler thread as it will always report ProfilerImpl#profilingLoop taking %100 of the time (since we are getting the stack trace while inside this method just before feeding it to ProfilerImpl#addResult), it's not useful. If the profiler should be profiled, it should be done with a different instance of profiler running on another thread.
                if (currentThreadId != threadInfo.getThreadId() && (null == ProfilerImpl.threadFilter || Objects.requireNonNull(threadInfo.getThreadName()).contains(Objects.requireNonNull(ProfilerImpl.threadFilter))) && (!ProfilerImpl.cpuBoundOnly || Thread.State.RUNNABLE == threadInfo.getThreadState())) {
                    this.addResult(threadInfo.getThreadName(), threadInfo.getThreadId(), Objects.requireNonNull(threadInfo.getStackTrace()));
                }
            }
        }
    }

    @NotNull
    public static final String getPackageName(@NotNull final StackTraceElement stackTraceElement) {
        return StringUtils.substringBeforeLast(stackTraceElement.getClassName(), ".");
    }

    @NotNull
    public static final String getClassName(@NotNull final StackTraceElement stackTraceElement) {
        return StringUtils.substringAfterLast(stackTraceElement.getClassName(), ".");
    }

    private final void addResult(@NotNull final String threadName, final long threadId, @NotNull final StackTraceElement... stackTraceElements) {
        if (0 != stackTraceElements.length) {
            final var topOfStack = stackTraceElements[0];
            Objects.requireNonNull(topOfStack);

            final var packageName = ProfilerImpl.getPackageName(topOfStack);
            final var className = ProfilerImpl.getClassName(topOfStack);
            final var fileName = topOfStack.getFileName();
            final var methodName = topOfStack.getMethodName();
            final var lineNumber = ProfilerImpl.lineNumbers ? topOfStack.getLineNumber() : -1;

            var timeSpent = this.samplingInterval;

            final var identifier = ProfilerImpl.combineIdentifier(threadName, threadId, packageName, className, fileName, methodName, lineNumber, stackTraceElements);

            final var prevResult = this.findResult(identifier);
            if (null != prevResult) {
                timeSpent += prevResult.timeSpent();
                this.results.remove(prevResult);
                this.quickAccess.remove(identifier);
            }

            final ProfilerResult result = new ProfilerResultImpl(packageName, className, fileName, methodName, lineNumber, timeSpent, stackTraceElements, threadName);
            this.results.add(result);
            this.quickAccess.put(identifier, result);
        }
    }

    @NotNull
    private static final String stackIdentifier(@NotNull final StackTraceElement... stack) {
        final var builder = new StringBuilder(Math.max(16, stack.length * 5));
        final var lines = ProfilerImpl.lineNumbers;
        final var stackLength = stack.length;
        for (var i = 1; i < stackLength; ++i) { // Skip the first one as that's the method that is being run
            final var elem = stack[i];

            builder.append(elem.getClassName()).append(elem.getFileName()).append(elem.getMethodName()).append(lines ? elem.getLineNumber() : -1);
        }
        return builder.toString();
    }

    @NotNull
    private static final String combineIdentifier(@NotNull final String threadName, final long threadId, @NotNull final String packageName, @NotNull final String className, @Nullable final String fileName, @NotNull final String methodName, final int lineNumber, @NotNull final StackTraceElement... stack) {
        return (ProfilerImpl.uniquifyByThread ? threadName : "") + (ProfilerImpl.uniquifyByThreadId ? threadId : "") + packageName + className + fileName + methodName + lineNumber + (ProfilerImpl.uniquifyByStack ? ProfilerImpl.stackIdentifier(stack) : "");
    }

    @Nullable
    private final ProfilerResult findResult(@NotNull final String identifier) {
        return this.quickAccess.get(identifier);
    }

    @Override
    public final void startProfiling() {
        //noinspection IfCanBeAssertion
        if (!this.profiling.compareAndSet(false, true)) {
            throw new IllegalStateException("profiling is already started");
        }

        this.registerGCTracker();

        final var profilerThread = new Thread(this::profilingLoop, "Profiler Thread #" + ProfilerImpl.counter.incrementAndGet());
        profilerThread.start();
    }

    private final void registerGCTracker() {
        this.unregisterGCTracker();
        this.notificationListener = (@NotNull final Notification notification, @NotNull final Object handback) -> {
            final var gcni = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
            final var info = gcni.getGcInfo();

            this.gcTime += info.getDuration();
        };

        final var garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (final var garbageCollectorMXBean : garbageCollectorMXBeans) {
            ((NotificationBroadcaster) garbageCollectorMXBean).addNotificationListener(this.notificationListener, null, garbageCollectorMXBean);
        }
    }

    private final void unregisterGCTracker() {
        if (null != this.notificationListener) {
            final var garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
            for (final var garbageCollectorMXBean : garbageCollectorMXBeans) {
                try {
                    ((NotificationBroadcaster) garbageCollectorMXBean).removeNotificationListener(this.notificationListener);
                } catch (final ListenerNotFoundException ignored) {
                    // ignored
                }
            }
            this.notificationListener = null;
        }
    }

    @NotNull
    @Override
    public final ProfilerResults pollResults() {
        final var copy = new ArrayList<>(this.results);
        copy.add(new ProfilerResultImpl("jvm.operation", "GC", null, "timeSpentInGC", -2, this.gcTime, ProfilerImpl.EMPTY_STACK_TRACE_ELEMENT_ARRAY, "all threads"));

        return new ProfilerResultsImpl(this, copy);
    }

    @Override
    public final void endProfiling() {
        //noinspection IfCanBeAssertion
        if (!this.profiling.compareAndSet(true, false)) {
            throw new IllegalStateException("profiling is already stopped");
        }

        this.unregisterGCTracker();
    }

    public final long getTotalRuntime() {
        return this.totalRuntime;
    }

    @Override
    public final String toString() {
        return "ProfilerImpl{" +
            "results=" + this.results +
            ", quickAccess=" + this.quickAccess +
            ", profiling=" + this.profiling +
            ", samplingInterval=" + this.samplingInterval +
            ", totalRuntime=" + this.totalRuntime +
            ", notificationListener=" + this.notificationListener +
            ", gcTime=" + this.gcTime +
            '}';
    }
}
