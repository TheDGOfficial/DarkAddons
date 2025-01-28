package gg.darkaddons;

import gg.skytils.skytilsmod.Skytils;

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
        if (Config.isSendMessageOnRagAxe() && packet instanceof final S29PacketSoundEffect soundPacket && "mob.wolf.howl".equals(soundPacket.getSoundName()) && Utils.compareFloatExact(1.492_063_5F, soundPacket.getPitch()) && Utils.isHoldingItemContaining(mc, "Ragnarock Axe")) {
            final var player = mc.thePlayer;
            if (null != player) {
                final var item = player.getHeldItem();
                if (null != item) {
                    final var compound = item.getTagCompound();
                    if (null != compound) {
                        final var display = compound.getCompoundTag("display");
                        if (null != display) {
                            final var taglist = display.getTagList("Lore", 8);
                            if (null != taglist) {
                                final var size = taglist.tagCount();
                                for (var i = 0; i < size; ++i) {
                                    final var line = Utils.removeControlCodes(taglist.getStringTagAt(i));
                                    if (line.startsWith("Strength: +")) {
                                        final var amount = StringUtils.substringBefore(StringUtils.substringAfter(line, "Strength: +"), " ");
                                        try {
                                            final var parsed = Double.parseDouble(amount);
                                            Skytils.sendMessageQueue.add("/pc Gained strength from rag axe: " + (int) Math.floor(parsed * 1.5));
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
        }
    }
}
