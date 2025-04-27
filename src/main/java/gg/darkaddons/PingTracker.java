package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C16PacketClientStatus;
import net.minecraft.network.play.server.S37PacketStatistics;

import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicLong;

final class PingTracker {
    private static final AtomicLong lastSentPacket = new AtomicLong(-1L);
    private static final AtomicLong lastPingNanos = new AtomicLong(-1L);

    private static final AtomicLong cleanupPending = new AtomicLong(-1L);

    private static final ScheduledExecutorService trackerThread = Executors.newSingleThreadScheduledExecutor((@NotNull final Runnable r) -> Utils.newThread(r, "DarkAddons Ping Tracker Thread"));

    static {
        PingTracker.trackerThread.scheduleWithFixedDelay(() -> {
            if (Config.isPingDisplay() && -1L == PingTracker.lastSentPacket.get()) {
                final var player = Minecraft.getMinecraft().thePlayer;
                if (null != player) {
                    player.sendQueue.getNetworkManager().sendPacket(new C16PacketClientStatus(C16PacketClientStatus.EnumState.REQUEST_STATS));
                }
            }
        }, 1L, 1L, TimeUnit.SECONDS);

        PingTracker.trackerThread.scheduleWithFixedDelay(() -> {
            if (Config.isPingDisplay()) {
                final var last = PingTracker.lastSentPacket.get();
                if (-1L != last) {
                    final var pending = PingTracker.cleanupPending.get();
                    if (pending == last) {
                        PingTracker.reset();
                        return;
                    }
                    PingTracker.cleanupPending.set(last);
                } else {
                    PingTracker.cleanupPending.set(-1L);
                }
            }
        }, 5L, 5L, TimeUnit.SECONDS);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onClientDisconnect(@NotNull final FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        if (Config.isPingDisplay()) {
            PingTracker.reset();
        }
    }

    static final void reset() {
        PingTracker.lastSentPacket.set(-1L);
        PingTracker.lastPingNanos.set(-1L);
    }

    static final void onPacketSent(@NotNull final Packet<?> packet) {
        if (Config.isPingDisplay() && packet instanceof final C16PacketClientStatus stats && C16PacketClientStatus.EnumState.REQUEST_STATS == stats.getStatus()) {
            PingTracker.lastSentPacket.set(System.nanoTime());
        }
    }

    static final void onPacketReceived(@NotNull final Packet<?> packet) {
        if (Config.isPingDisplay() && packet instanceof final S37PacketStatistics stats) {
            final var sent = PingTracker.lastSentPacket.getAndSet(-1L);
            if (-1L != sent) {
                PingTracker.lastPingNanos.set(Math.abs(System.nanoTime() - sent));
            }
        }
    }

    /**
     * Returns the last measured ping in milliseconds.
     * Returns -1 if not measured yet.
     */
    static final int getLastPingMillis() {
        final var nanos = PingTracker.lastPingNanos.get();
        return -1L == nanos ? -1 : (int) (nanos / TimeUnit.MILLISECONDS.toNanos(1L));
    }
}
