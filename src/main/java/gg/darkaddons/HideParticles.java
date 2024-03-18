package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S2APacketParticles;

import net.minecraft.util.EnumParticleTypes;

final class HideParticles {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     *
     * @implNote The thrown {@link UnsupportedOperationException} will have no
     * message to not waste a {@link String} instance in the constant pool.
     */
    private HideParticles() {
        super();

        throw Utils.staticClassException();
    }

    static final boolean handlePacket(@NotNull final Packet<?> packet) {
        if (Config.isHideParticles() && AdditionalM7Features.phase5Started && packet instanceof final S2APacketParticles particlePacket) {
            final var type = particlePacket.getParticleType();

            return EnumParticleTypes.ENCHANTMENT_TABLE == type || EnumParticleTypes.FLAME == type || EnumParticleTypes.FIREWORKS_SPARK == type;
        }

        return true;
    }
}
