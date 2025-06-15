package gg.darkaddons;

import net.minecraft.client.Minecraft;

final class ChunkUtils {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private ChunkUtils() {
        super();

        throw Utils.staticClassException();
    }

    static final void reloadChunks() {
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }
}
