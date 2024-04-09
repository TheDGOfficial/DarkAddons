package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.Minecraft;

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
            final var windowName = packetOpenWindow.getWindowTitle().getUnformattedText();
            if ("Chest".equals(windowName) || "Large Chest".equals(windowName)) {
                Minecraft.getMinecraft().getNetHandler().getNetworkManager().sendPacket(new C0DPacketCloseWindow(packetOpenWindow.getWindowId()));
                return false;
            }
        }
        return true;
    }
}
