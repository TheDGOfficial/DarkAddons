package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S32PacketConfirmTransaction;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ScheduledExecutorService;

import java.util.concurrent.atomic.AtomicInteger;

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

    static volatile boolean initialized;
 
    static volatile int lastTPS;

    static {
        ServerTPSCalculator.calculatorThread.scheduleWithFixedDelay(() -> {
            if (Config.isTpsDisplay()) {
                final var ticksThisSecond = ServerTPSCalculator.tickCount.getAndSet(0);
                ServerTPSCalculator.lastTPS = Math.min(20, ticksThisSecond);

                if (!ServerTPSCalculator.initialized && ticksThisSecond > 0) {
                    ServerTPSCalculator.initialized = true;
                }
            }
        }, 1L, 1L, TimeUnit.SECONDS);
    }

    /**
     * On world change, which is most of the time a server change on Hypixel,
     * the TPS will be 0 till we start getting the S32PacketConfirmTransaction packets.
     * so we have a flag to see if we got any confirm transaction packets in the current world,
     * and if we do we can assume the tps is correct, otherwise we will show the TPS as loading.
     */
    static final void onWorldUnload() {
        if (Config.isTpsDisplay()) {
            ServerTPSCalculator.initialized = false;
            ServerTPSCalculator.lastTPS = 0;
            ServerTPSCalculator.tickCount.set(0);
        }
    }

    /**
     * Called by main mod class whenever a packet is received on the client.
     * The filtering of whether the packet should be sent to mod code is done
     * by the main class already, i.e the checks for if packet's direction is
     * EnumPacketDirection.CLIENTBOUND and is not sent in a local channel
     * (e.g the integrated server in single player worlds).
     */
    static final void handlePacket(@NotNull final Packet<?> packet) {
        if (Config.isTpsDisplay() && packet instanceof final S32PacketConfirmTransaction pct) {
            if (pct.getActionNumber() < 1) {
                ServerTPSCalculator.tickCount.incrementAndGet();
            }
        }
    }
}
