package gg.darkaddons;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class FPSLimitDisplay extends SimpleGuiElement {
    private static int fpsLimit;
    private static int lastFpsLimit;

    FPSLimitDisplay() {
        super("FPS Limit Warning", Config::isFpsLimitDisplay, () -> true, () -> 0);

        DarkAddons.registerTickTask("fps_limit_warning_update_fps_limit", 20, true, this::update);
    }

    @Override
    final void clear() {
        FPSLimitDisplay.fpsLimit = 0;
        FPSLimitDisplay.lastFpsLimit = 0;

        super.clear();
    }

    @Override
    final void update() {
        if (!this.isEnabled()) {
            return;
        }

        final var demoRenderBypass = this.isDemoRenderBypass();

        if (!demoRenderBypass && !Minecraft.getMinecraft().isFramerateLimitBelowMax()) {
            this.clear();
            return;
        }

        FPSLimitDisplay.fpsLimit = Minecraft.getMinecraft().getLimitFramerate();

        if (demoRenderBypass || FPSLimitDisplay.lastFpsLimit != FPSLimitDisplay.fpsLimit) {
            FPSLimitDisplay.lastFpsLimit = FPSLimitDisplay.fpsLimit;

            super.update();
        }
    }

    @Override
    void buildHudLines(final @NotNull Collection<String> lines) {
        lines.add("§cFPS Limited to " + FPSLimitDisplay.fpsLimit);
    }
}