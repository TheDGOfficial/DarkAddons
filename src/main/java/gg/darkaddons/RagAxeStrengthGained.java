package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S29PacketSoundEffect;

import org.apache.commons.lang3.StringUtils;

final class RagAxeStrengthGained {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private RagAxeStrengthGained() {
        super();

        throw Utils.staticClassException();
    }

    static final void handlePacket(@NotNull final Packet<?> packet) {
        final var mc = Minecraft.getMinecraft();
        if (Config.isSendMessageOnRagAxe() && (!Config.isSendMessageOnRagAxeOnlyInDungeons() || DarkAddons.isInDungeons()) && packet instanceof final S29PacketSoundEffect soundPacket && "mob.wolf.howl".equals(soundPacket.getSoundName()) && Utils.compareFloatExact(1.492_063_5F, soundPacket.getPitch()) && ItemUtils.isHoldingItemContaining(mc, "Ragnarock Axe")) {
            final var player = mc.thePlayer;
            if (null != player) {
                final var item = player.getHeldItem();
                if (null != item) {
                    for (final var line : ItemUtils.getItemLore(item)) {
                        final var cleanLine = Utils.removeControlCodes(line);
                        if (cleanLine.startsWith("Strength: +")) {
                            final var amount = StringUtils.substringBefore(StringUtils.substringAfter(cleanLine, "Strength: +"), " ");
                            try {
                                final var parsed = Double.parseDouble(amount);
                                DarkAddons.queueUserSentMessageOrCommand("/pc Gained strength from rag axe: " + (int) Math.floor(parsed * 1.5));
                            } catch (final NumberFormatException nfe) {
                                DarkAddons.modError(nfe);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }
}
