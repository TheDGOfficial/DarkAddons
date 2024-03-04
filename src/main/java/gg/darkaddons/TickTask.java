package gg.darkaddons;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.ConcurrentLinkedQueue;

final class TickTask {
    @NotNull
    private final String taskName;
    private final int ticks;

    private int remainingTicks;

    private final boolean repeats;

    @NotNull
    private final Runnable runnable;

    TickTask(@NotNull final String name, final int delayOrInterval, final boolean repeat, @NotNull final Runnable task) {
        super();

        this.taskName = name;
        this.ticks = delayOrInterval;
        this.remainingTicks = this.ticks;
        this.repeats = repeat;
        this.runnable = task;
    }

    final void register() {
        TickTask.TickTaskManager.tasks.add(this);
    }

    @NotNull
    static final Object newManager() {
        return new TickTask.TickTaskManager();
    }

    private static final class TickTaskManager {
        private TickTaskManager() {
            super();
        }

        @NotNull
        private static final ConcurrentLinkedQueue<TickTask> tasks = new ConcurrentLinkedQueue<>();

        private static final void handleTick(@NotNull final TickEvent.ClientTickEvent event) {
            if (TickEvent.Phase.START == event.phase && !TickTask.TickTaskManager.tasks.isEmpty()) {
                TickTask.TickTaskManager.tasks.removeIf((@NotNull final TickTask task) -> {
                    //noinspection DataFlowIssue,ValueOfIncrementOrDecrementUsed
                    if (0 >= task.remainingTicks--) {
                        final var shouldProfile = DarkAddons.shouldProfile();
                        if (shouldProfile) {
                            McProfilerHelper.startSection(task.taskName);
                        }
                        task.runnable.run();
                        if (shouldProfile) {
                            McProfilerHelper.endSection();
                        }
                        if (task.repeats) {
                            task.remainingTicks = task.ticks;
                        } else {
                            return true;
                        }
                    }
                    return false;
                });
            }
        }

        @SubscribeEvent(priority = EventPriority.LOWEST)
        public final void onTick(@NotNull final TickEvent.ClientTickEvent event) {
            if (DarkAddons.checkClientEvent()) {
                return;
            }

            if (DarkAddons.shouldProfile()) {
                DarkAddons.handleEvent("dark_addons_tick_tasks", event, TickTask.TickTaskManager::handleTick);
            } else {
                TickTask.TickTaskManager.handleTick(event);
            }
        }
    }

    @Override
    @NotNull
    public final String toString() {
        return "TickTask{" +
            "taskName='" + this.taskName + '\'' +
            ", ticks=" + this.ticks +
            ", remainingTicks=" + this.remainingTicks +
            ", repeats=" + this.repeats +
            ", runnable=" + this.runnable +
            '}';
    }
}
