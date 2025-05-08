package gg.darkaddons;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;

public final class SoundManager {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private SoundManager() {
        super();

        throw Utils.staticClassException();
    }

    public static boolean shouldBypassVolumeLimit;

    static final void playSound(@NotNull final String sound, final float pitch, final float volume, final boolean bypassVolumeLimit) {
        final var player = Minecraft.getMinecraft().thePlayer;
        SoundManager.shouldBypassVolumeLimit = bypassVolumeLimit;
        player.playSound(sound, volume, pitch);
        SoundManager.shouldBypassVolumeLimit = false;
    }

    static final void playSound(@NotNull final String sound, final float pitch, final float volume, final boolean bypassVolumeLimit, final int delay) {
        DarkAddons.registerTickTask("play_queued_sound", delay, false, () -> SoundManager.playSound(sound, pitch, volume, bypassVolumeLimit));
    }
}