package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

import java.util.concurrent.atomic.AtomicInteger;

import java.util.function.IntConsumer;

final class ServerTPSCalculator {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private ServerTPSCalculator() {
        super();

        throw Utils.staticClassException();
    }

    private static final AtomicInteger tickCount = new AtomicInteger();

    private static final ScheduledExecutorService calculatorThread = Executors.newSingleThreadScheduledExecutor((@NotNull final Runnable r) -> Utils.newThread(r, "DarkAddons Server TPS Calculator Thread"));

    private static volatile boolean initialized;

    private static volatile int lastTPS;

    private static volatile boolean enableTPSCalculationTemporarily;

    private static volatile IntConsumer hook;
    private static volatile Runnable serverTickHook;

    /**
     * Gets TPS from the last second.
     * Returns -1 if not measured yet.
     */
    static final int getLastTPS() {
        return ServerTPSCalculator.initialized ? ServerTPSCalculator.lastTPS : -1;
    }

    static {
        ServerTPSCalculator.calculatorThread.scheduleWithFixedDelay(() -> {
            if (ServerTPSCalculator.shouldCalculate()) {
                final var ticksThisSecond = ServerTPSCalculator.tickCount.getAndSet(0);
                ServerTPSCalculator.lastTPS = Math.min(20, ticksThisSecond);

                if (!ServerTPSCalculator.initialized && 0 < ticksThisSecond) {
                    ServerTPSCalculator.initialized = true;
                }

                if (ServerTPSCalculator.initialized) {
                    final var hook = ServerTPSCalculator.hook;
                    if (null != hook) {
                        final var value = ServerTPSCalculator.lastTPS;

                        // Run the hook inside client thread for thread safety.
                        DarkAddons.runOnceInNextTick("tps_update_hook", () -> {
                            hook.accept(value);
                        });
                    }
                }
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    /**
     * On world change, which is most of the time a server change on Hypixel,
     * the TPS will be 0 till we start getting the S32PacketConfirmTransaction packets.
     * so we have a flag to see if we got any confirm transaction packets in the current world,
     * and if we do, we can assume the tps is correct, otherwise we will show the TPS as loading.
     */
    static final void onWorldUnload() {
        if (ServerTPSCalculator.shouldCalculate()) {
            ServerTPSCalculator.initialized = false;
            ServerTPSCalculator.lastTPS = 0;
            ServerTPSCalculator.tickCount.set(0);
        }
    }

    /**
     * Called by main mod class whenever a packet is received on the client.
     * The filtering of whether the packet should be sent to mod code is done
     * by the main class already, i.e., the checks for if packet's direction is
     * EnumPacketDirection.CLIENTBOUND and is not sent in a local channel
     * (e.g., the integrated server in single player worlds).
     */
    static final void handlePacket(@NotNull final Packet<?> packet) {
        if (ServerTPSCalculator.shouldCalculate() && packet instanceof final S32PacketConfirmTransaction pct) {
            if (1 > pct.getActionNumber()) {
                ServerTPSCalculator.tickCount.incrementAndGet();

                final var hook = ServerTPSCalculator.serverTickHook;
                if (null != hook) {
                    // Run the hook inside client thread for thread safety.
                    DarkAddons.runOnceInNextTick("server_tick_hook", hook);
                }
            }
        }
    }

    static final void startCalculatingTPS(@NotNull final IntConsumer hook) {
        ServerTPSCalculator.enableTPSCalculationTemporarily = true;
        ServerTPSCalculator.hook = hook;
    }

    static final void stopCalculatingTPS() {
        ServerTPSCalculator.enableTPSCalculationTemporarily = false;
        ServerTPSCalculator.hook = null;
    }

    static final void startListeningTicks(@NotNull final Runnable serverTickHook) {
        ServerTPSCalculator.enableTPSCalculationTemporarily = true;
        ServerTPSCalculator.serverTickHook = serverTickHook;
    }

    static final void stopListeningTicks() {
        ServerTPSCalculator.enableTPSCalculationTemporarily = false;
        ServerTPSCalculator.serverTickHook = null;
    }

    private static final boolean shouldCalculate() {
        return Config.isTpsDisplay() || ServerTPSCalculator.enableTPSCalculationTemporarily;
    }
}
