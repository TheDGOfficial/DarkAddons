package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class PingDisplay extends SimpleGuiElement {
    private static final String[] LOADING_TEXTS = {"Loading.", "Loading..", "Loading..."};
    private static final int loadingTextsLength = PingDisplay.LOADING_TEXTS.length;

    private static int currentLoadingTextIndex;

    private static int ping;
    private static int lastPing;

    PingDisplay() {
        super("Ping Display", Config::isPingDisplay, () -> true, () -> 0);

        DarkAddons.registerTickTask("ping_display_update_ping", 20, true, this::update);
    }

    @Override
    final void clear() {
        PingDisplay.ping = 0;
        PingDisplay.lastPing = 0;

        PingDisplay.currentLoadingTextIndex = 0;

        super.clear();
    }

    @Override
    final void update() {
        if (!this.isEnabled()) {
            return;
        }

        final var isDemoRenderBypass = this.isDemoRenderBypass();

        PingDisplay.ping = PingTracker.getLastPingMillis();

        if (isDemoRenderBypass || (-1 == PingDisplay.ping || PingDisplay.lastPing != PingDisplay.ping) || this.isEmpty()) {
            PingDisplay.lastPing = PingDisplay.ping;

            super.update();
        }
    }

    @NotNull
    private static final String getLoadingText() {
        return PingDisplay.LOADING_TEXTS[PingDisplay.currentLoadingTextIndex = (PingDisplay.currentLoadingTextIndex + 1) % PingDisplay.loadingTextsLength];
    }

    @NotNull
    private static final String getPingWithColor(final int pingToColor) {
        final var color = -1 == pingToColor ? "6" : 75 > pingToColor ? "a" : 150 > pingToColor ? "6" : 250 > pingToColor ? "e" : "c";

        return color + (-1 == pingToColor ? PingDisplay.getLoadingText() : pingToColor);
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        lines.add("§bPing: §" + PingDisplay.getPingWithColor(PingDisplay.ping));
    }
}
