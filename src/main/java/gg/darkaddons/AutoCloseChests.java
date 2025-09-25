package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;

import net.minecraft.util.ChatComponentTranslation;

import net.minecraft.network.Packet;

import net.minecraft.network.play.server.S2DPacketOpenWindow;
import net.minecraft.network.play.client.C0DPacketCloseWindow;

final class AutoCloseChests {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private AutoCloseChests() {
        super();

        throw Utils.staticClassException();
    }

    static final boolean handlePacket(@NotNull final Packet<?> packet) {
        if (Config.isAutoCloseChests() && DarkAddons.isInDungeons() && packet instanceof final S2DPacketOpenWindow packetOpenWindow) {
            final var title = packetOpenWindow.getWindowTitle();
            final var slots = packetOpenWindow.getSlotCount();

            // Only look at standard chest sizes: 9x3 (27) or 9x6 (54)
            if ((27 == slots || 54 == slots) && title instanceof final ChatComponentTranslation translation) {
                final String key = translation.getKey();

                if ("container.chest".equals(key) || "container.chestDouble".equals(key)) {
                    Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(new C0DPacketCloseWindow(packetOpenWindow.getWindowId()));
                    return false;
                }
            }
        }
        return true;
    }
}
