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
}
