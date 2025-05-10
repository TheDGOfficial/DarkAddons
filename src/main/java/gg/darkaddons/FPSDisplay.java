package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class FPSDisplay extends SimpleGuiElement {
    private static int fps;
    private static int lastFps;

    FPSDisplay() {
        super("FPS Display", Config::isFpsDisplay, () -> true, () -> 0);

        DarkAddons.registerTickTask("fps_display_update_fps", 20, true, this::update);
    }

    @Override
    final void clear() {
        FPSDisplay.fps = 0;
        FPSDisplay.lastFps = 0;

        super.clear();
    }

    @Override
    final void update() {
        if (!this.isEnabled()) {
            return;
        }

        final var isDemoRenderBypass = this.isDemoRenderBypass();

        FPSDisplay.fps = Diagnostics.getFPSWithNanosecondPrecision();

        if (isDemoRenderBypass || FPSDisplay.lastFps != FPSDisplay.fps || this.isEmpty()) {
            FPSDisplay.lastFps = FPSDisplay.fps;

            super.update();
        }
    }

    @NotNull
    private static final String getFpsWithColor(final int fpsToColor) {
        final var color = 60 <= fpsToColor ? "a" : 30 <= fpsToColor ? "e" : "c";

        return color + fpsToColor;
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        lines.add("§bFPS: §" + FPSDisplay.getFpsWithColor(FPSDisplay.fps));
    }
}
