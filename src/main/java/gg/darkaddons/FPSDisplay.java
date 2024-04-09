package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class FPSDisplay extends SimpleGuiElement {
    private static long fps;
    private static long lastFps;

    FPSDisplay() {
        super("FPS Display", Config::isFpsDisplay, () -> true, () -> 0);

        DarkAddons.registerTickTask("fps_display_update_fps", 20, true, this::update);
    }

    @Override
    final void clear() {
        FPSDisplay.fps = 0L;
        FPSDisplay.lastFps = 0L;

        super.clear();
    }

    @Override
    final void update() {
        if (!this.isEnabled()) {
            return;
        }

        final var isDemoRenderBypass = this.isDemoRenderBypass();

        FPSDisplay.fps = 1_000L / Math.max(1, Diagnostics.getLastGameLoopTime());

        if (isDemoRenderBypass || (FPSDisplay.lastFps != FPSDisplay.fps || this.isEmpty())) {
            FPSDisplay.lastFps = FPSDisplay.fps;

            super.update();
        }
    }

    @NotNull
    private static final String getFpsWithColor(final long fps) {
        final var color = 60 <= fps ? "a" : 30 <= fps ? "e" : "c";

        return color + fps;
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        lines.add("§bFPS: §" + FPSDisplay.getFpsWithColor(FPSDisplay.fps));
    }
}
