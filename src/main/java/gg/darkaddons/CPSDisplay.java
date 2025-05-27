package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;

final class CPSDisplay extends SimpleGuiElement {
    private static int leftClickCps;
    private static int lastLeftClickCps;

    private static int rightClickCps;
    private static int lastRightClickCps;

    CPSDisplay() {
        super("CPS Display", Config::isCpsDisplay, () -> true, () -> 0, 4);

        DarkAddons.registerTickTask("cps_display_update_cps", 20, true, this::update);
    }

    @Override
    final void clear() {
        CPSDisplay.leftClickCps = 0;
        CPSDisplay.lastLeftClickCps = 0;

        CPSDisplay.rightClickCps = 0;
        CPSDisplay.lastRightClickCps = 0;

        super.clear();
    }

    @Override
    final void update() {
        if (!this.isEnabled()) {
            return;
        }

        final var isDemoRenderBypass = this.isDemoRenderBypass();

        CPSDisplay.leftClickCps = CPSCalculator.getLastLeftClickCPS();
        CPSDisplay.rightClickCps = CPSCalculator.getLastRightClickCPS();

        if (isDemoRenderBypass || CPSDisplay.lastLeftClickCps != CPSDisplay.leftClickCps || CPSDisplay.lastRightClickCps != CPSDisplay.rightClickCps || this.isEmpty()) {
            CPSDisplay.lastLeftClickCps = CPSDisplay.leftClickCps;
            CPSDisplay.lastRightClickCps = CPSDisplay.rightClickCps;

            super.update();
        }
    }

    @NotNull
    private static final String getCpsWithColor(final int cpsToColor) {
        final var color = 15 <= cpsToColor ? "a" : 10 <= cpsToColor ? "2" : 5 <= cpsToColor ? "e" : 1 <= cpsToColor ? "6" : "f";

        return color + cpsToColor;
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        lines.add("§bTotal CPS: §" + CPSDisplay.getCpsWithColor(CPSDisplay.leftClickCps + CPSDisplay.rightClickCps));
        lines.add("");
        lines.add("§bLeft Click CPS: §" + CPSDisplay.getCpsWithColor(CPSDisplay.leftClickCps));
        lines.add("§bRight Click CPS: §" + CPSDisplay.getCpsWithColor(CPSDisplay.rightClickCps));
    }
}
