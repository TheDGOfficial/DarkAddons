package gg.darkaddons;

import gg.darkaddons.mixins.IMixinS3BPacketScoreboardObjective;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3BPacketScoreboardObjective;
import org.jetbrains.annotations.NotNull;

final class ChromaScoreboard {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     *
     * @implNote The thrown {@link UnsupportedOperationException} will have no
     * message to not waste a {@link String} instance in the constant pool.
     */
    private ChromaScoreboard() {
        super();

        throw Utils.staticClassException();
    }

    static final void handlePacket(@NotNull final Packet<?> packet) {
        if (Config.isChromaToggle() && Config.isChromaSkyblock() && DarkAddons.isUsingSBA() && packet instanceof S3BPacketScoreboardObjective) {
            final var currentObjective = ScoreboardUtil.cleanSB(((S3BPacketScoreboardObjective) packet).func_149337_d());
            if (null != currentObjective && (currentObjective.contains("SKYBLOCK") || currentObjective.contains("SKIBLOCK"))) {
                ((IMixinS3BPacketScoreboardObjective) packet).setObjectiveValue("§z§lSKYBLOCK");
            }
        }
    }
}
