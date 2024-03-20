package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S0EPacketSpawnObject;

final class HideFallingBlocks {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private HideFallingBlocks() {
        super();

        throw Utils.staticClassException();
    }

    static final boolean handlePacket(@NotNull final Packet<?> packet) {
        if (Config.isHideFallingBlocks() && packet instanceof final S0EPacketSpawnObject packetSpawnObject && 70 == packetSpawnObject.getType()) {
            return false;
        }

        return true;
    }
}
