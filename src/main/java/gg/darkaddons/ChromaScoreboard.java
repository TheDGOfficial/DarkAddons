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

    // This required for ProGuard to not optimize out the calls as the interface is only implemented in runtime.
    // A hacky workaround but works for now without having to disable optimizations.
    private static final class Dummy implements IMixinS3BPacketScoreboardObjective {
        @Override
        @NotNull
        public final String getObjectiveValue() {
            throw new UnsupportedOperationException(this.getClass().getName());
        }

        @Override
        public final void setObjectiveValue(@NotNull final String objectiveValue) {
            throw new UnsupportedOperationException(this.getClass().getName());
        }
    }

    static final void handlePacket(@NotNull final Packet<?> packet) {
        if (Config.isChromaToggle() && Config.isChromaSkyblock() && DarkAddons.isUsingSBA() && packet instanceof final S3BPacketScoreboardObjective packetScoreboardObjective) {
            final var packetScoreboardObjectiveAccessor = (IMixinS3BPacketScoreboardObjective) packetScoreboardObjective;
            final var currentObjective = ScoreboardUtil.cleanSB(packetScoreboardObjectiveAccessor.getObjectiveValue());

            if (null != currentObjective && (currentObjective.contains("SKYBLOCK") || currentObjective.contains("SKIBLOCK"))) {
                packetScoreboardObjectiveAccessor.setObjectiveValue("§z§lSKYBLOCK");
            }
        }
    }
}
