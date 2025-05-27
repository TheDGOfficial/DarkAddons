package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

final class TPSDisplay extends SimpleGuiElement {
    private static final String[] LOADING_TEXTS = new String[]{"Loading.", "Loading..", "Loading..."};
    private static final int loadingTextsLength = TPSDisplay.LOADING_TEXTS.length;

    private static int currentLoadingTextIndex;

    private static int tps;
    private static int lastTps;

    TPSDisplay() {
        super("TPS Display", Config::isTpsDisplay, () -> true, () -> 0);

        DarkAddons.registerTickTask("tps_display_update_tps", 20, true, this::update);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onWorldChange(@NotNull final WorldEvent.Unload event) {
        TPSDisplay.currentLoadingTextIndex = 0;
    }

    @Override
    final void clear() {
        TPSDisplay.tps = 0;
        TPSDisplay.lastTps = 0;

        TPSDisplay.currentLoadingTextIndex = 0;

        super.clear();
    }

    @Override
    final void update() {
        if (!this.isEnabled()) {
            return;
        }

        final var isDemoRenderBypass = this.isDemoRenderBypass();

        TPSDisplay.tps = ServerTPSCalculator.getLastTPS();

        if (isDemoRenderBypass || -1 == TPSDisplay.tps || TPSDisplay.lastTps != TPSDisplay.tps || this.isEmpty()) {
            TPSDisplay.lastTps = TPSDisplay.tps;

            super.update();
        }
    }

    @NotNull
    private static final String getLoadingText() {
        return TPSDisplay.LOADING_TEXTS[TPSDisplay.currentLoadingTextIndex = (TPSDisplay.currentLoadingTextIndex + 1) % TPSDisplay.loadingTextsLength];
    }

    @NotNull
    private static final String getTpsWithColor(final int tpsToColor) {
        final var color = -1 == tpsToColor ? "6" : 18 <= tpsToColor ? "a" : 15 <= tpsToColor ? "e" : "c";

        return color + (-1 == tpsToColor ? TPSDisplay.getLoadingText() : tpsToColor);
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        lines.add("§bTPS: §" + TPSDisplay.getTpsWithColor(TPSDisplay.tps));
    }
}
