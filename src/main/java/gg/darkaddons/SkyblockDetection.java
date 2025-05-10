package gg.darkaddons;

import net.minecraft.client.Minecraft;

import java.util.Locale;

final class SkyblockDetection {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private SkyblockDetection() {
        super();

        throw Utils.staticClassException();
    }

    static final boolean isInSkyblock() {
        return SBInfo.isInSkyblock();
    }

    static final boolean isInAlphaNetwork() {
        final var currentServer = Minecraft.getMinecraft().getCurrentServerData();
        if (null != currentServer) {
            final var ip = currentServer.serverIP;
            if (null != ip) {
                return ip.toLowerCase(Locale.ROOT).contains("alpha");
            }
        }
        return false;
    }
}
