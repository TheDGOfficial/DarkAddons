package gg.darkaddons;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import java.util.WeakHashMap;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

final class ThreadPriorityTweaker {
    /**
     * Holds the original priorities for threads. Weak keys to not prevent thread instances from getting garbage collected.
     */
    @NotNull
    private static final WeakHashMap<Thread, Integer> originalPriorityMap = new WeakHashMap<>(Utils.calculateHashMapCapacity(100));

    /**
     * Run from separate thread to not lag the game since finding root thread group, then iterating over all threads,
     * and then setting their priorities according to their names is not entirely free, and we have to do this periodically
     * because new threads might spawn at a later time, after we set the priorities for the first time.
     */
    @NotNull
    private static final ScheduledExecutorService threadPriorityTweakerScheduler = Executors.newSingleThreadScheduledExecutor((@NotNull final Runnable r) -> Utils.newThread(r, "DarkAddons Thread Priority Tweaker Thread"));

    @Nullable
    private static ScheduledFuture<?> scheduledTweakPrioritiesTask;

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private ThreadPriorityTweaker() {
        super();

        throw Utils.staticClassException();
    }

    /**
     * Tweaks the priority of all currently live threads and registers a
     * task that runs every minute to periodically do this operation again,
     * in case new threads are spawned.
     * <p>
     * This method automatically cancels the previous scheduled task if it
     * is not already cancelled.
     */
    static final void scheduleTweakTask() {
        ThreadPriorityTweaker.cancelTweakTask();
        ThreadPriorityTweaker.scheduledTweakPrioritiesTask = ThreadPriorityTweaker.threadPriorityTweakerScheduler.scheduleWithFixedDelay(ThreadPriorityTweaker::tweakPriorities, 0L, 60L, TimeUnit.SECONDS);
    }

    /**
     * Cancels the periodic thread priority tweaking task so that it will not run again, unless you call the {@link ThreadPriorityTweaker#scheduleTweakTask()} method again.
     * <p>
     * Additionally, restores the old thread priorities.
     */
    static final void cancelTweakTaskAndRestorePriorities() {
        ThreadPriorityTweaker.cancelTweakTask();
        ThreadPriorityTweaker.threadPriorityTweakerScheduler.execute(ThreadPriorityTweaker::restorePriorities);
    }

    /**
     * Cancels the periodic thread priority tweaking task so that it will not run again, unless you call the {@link ThreadPriorityTweaker#scheduleTweakTask()} method again.
     */
    private static final void cancelTweakTask() {
        if (null != ThreadPriorityTweaker.scheduledTweakPrioritiesTask) {
            ThreadPriorityTweaker.scheduledTweakPrioritiesTask.cancel(false);
            ThreadPriorityTweaker.scheduledTweakPrioritiesTask = null;
        }
    }

    /**
     * Tweaks priorities of currently live threads. This action is done on a
     *  separate thread to not cause any lag in-game.
     */
    private static final void tweakPriorities() {
        for (final var thread : Utils.getAllThreads()) {
            if (null != thread) { // can be null if the thread is finished by the time we are iterating the list of living threads
                final var name = thread.getName();

                if (ThreadPriorityTweaker.tweakPriorityExact(name, thread)) {
                    continue;
                }

                ThreadPriorityTweaker.tweakPriorityContains(thread, name);
            }
        }
    }

    /**
     * Restores priorities of currently live threads.
     */
    private static final void restorePriorities() {
        //noinspection ForLoopWithMissingComponent
        for (final var iterator = ThreadPriorityTweaker.originalPriorityMap.entrySet().iterator(); iterator.hasNext();) {
            final var entry = iterator.next();

            final var thread = entry.getKey();

            final int originalPriority = entry.getValue();

            //noinspection CallToThreadSetPriority
            thread.setPriority(originalPriority);
            iterator.remove();
        }
    }

    private static final boolean tweakPriorityExact(@NotNull final String name, @NotNull final Thread thread) {
        return switch (name) {
            case "Client thread" -> {
                ThreadPriorityTweaker.tweakPriority(thread, Thread.MAX_PRIORITY); // 10
                yield true;
            }
            case "CullThread", "Reference Handler" -> {
                ThreadPriorityTweaker.tweakPriority(thread, Thread.MAX_PRIORITY - 3); // 7
                yield true;
            }
            case "Server thread", "Patcher Concurrency Thread 1", "Sound Thread" -> {
                ThreadPriorityTweaker.tweakPriority(thread, Thread.MAX_PRIORITY - 4); // 6
                yield true;
            }
            case "Timer hack thread", "Snooper Timer" -> {
                ThreadPriorityTweaker.tweakPriority(thread, Thread.MIN_PRIORITY); // 1
                yield true;
            }
            default -> false;
        };
    }

    private static final void tweakPriorityContains(@NotNull final Thread thread, @NotNull final String name) {
        if (name.startsWith("Chunk Batcher ")) {
            ThreadPriorityTweaker.tweakPriority(thread, Thread.MAX_PRIORITY - 1); // 9
        } else if (name.startsWith("Netty ")) {
            ThreadPriorityTweaker.tweakPriority(thread, Thread.MAX_PRIORITY - 2); // 8
        } else if (name.startsWith("Server Pinger #") || name.startsWith("Chat#")) {
            ThreadPriorityTweaker.tweakPriority(thread, Thread.NORM_PRIORITY + 1); // 6
        } else if (name.startsWith("DefaultDispatcher")) {
            ThreadPriorityTweaker.tweakPriority(thread, Thread.MIN_PRIORITY); // 1
        } else if (5 < thread.getPriority()) {
            ThreadPriorityTweaker.tweakPriority(thread, Thread.NORM_PRIORITY); // 5
        }
    }

    private static final void tweakPriority(@NotNull final Thread thread, final int priority) {
        final var oldPriority = thread.getPriority();

        if (oldPriority != priority) {
            ThreadPriorityTweaker.originalPriorityMap.putIfAbsent(thread, oldPriority);
            //noinspection CallToThreadSetPriority
            thread.setPriority(priority);
        }
    }

    static final int getThreadCount(final boolean daemonOnly) {
        var count = 0;
        for (final var thread : Utils.getAllThreads()) {
            if (null != thread && (!daemonOnly || thread.isDaemon())) {
                ++count;
            }
        }
        return count;
    }
}
