package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

import java.util.concurrent.atomic.AtomicInteger;

final class CPSCalculator {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private CPSCalculator() {
        super();

        throw Utils.staticClassException();
    }

    private static final AtomicInteger leftClickCount = new AtomicInteger();
    private static final AtomicInteger rightClickCount = new AtomicInteger();

    private static final ScheduledExecutorService calculatorThread = Executors.newSingleThreadScheduledExecutor((@NotNull final Runnable r) -> Utils.newThread(r, "DarkAddons CPS Calculator Thread"));

    private static volatile int lastLeftClickCPS;

    static final int getLastLeftClickCPS() {
        return CPSCalculator.lastLeftClickCPS;
    }

    private static volatile int lastRightClickCPS;

    static final int getLastRightClickCPS() {
        return CPSCalculator.lastRightClickCPS;
    }

    static {
        CPSCalculator.calculatorThread.scheduleWithFixedDelay(() -> {
            if (Config.isCpsDisplay()) {
                CPSCalculator.lastLeftClickCPS = CPSCalculator.leftClickCount.getAndSet(0);
                CPSCalculator.lastRightClickCPS = CPSCalculator.rightClickCount.getAndSet(0);
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    static final void onLeftClick() {
        if (Config.isCpsDisplay()) {
            leftClickCount.incrementAndGet();
        }
    }

    static final void onRightClick() {
        if (Config.isCpsDisplay()) {
            rightClickCount.incrementAndGet();
        }
    }
}
