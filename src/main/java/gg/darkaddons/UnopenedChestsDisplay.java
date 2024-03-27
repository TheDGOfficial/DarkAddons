package gg.darkaddons;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

// TODO Currently does not update unopened chests or display the chest limit warning unless you stay on dungeon hub for
// at least a second so that it can fetch and update unopened chests. This is not ideal and should be changed.

// TODO Add Chests Expire At into the Display as well, for example "Chests expire at approx. 48h"
// Likely need to listen for chat messages for Defeated <dungeon boss> messages and if previous/last unopened chests
// was 0 (to make it so we show time left to expire for the earliest expiring chest), save System.currentTimeMillis()
// to a variable which is saved to disk at game close and loaded to that variable on game open, then show the time left
// to expire as time left to 48 hours (time limit) being passed since that epoch.
final class UnopenedChestsDisplay extends SimpleGuiElement {
    private static final int CROESUS_CHEST_LIMIT = 60;

    private static int unopenedChests;
    private static int lastUnopenedChests;

    UnopenedChestsDisplay() {
        super("Unopened Chests Display", Config::isUnopenedChestsDisplay, DarkAddons::isPlayerInDungeonHub, () -> 0);

        DarkAddons.registerTickTask("update_unopened_chests", 20, true, this::update);
    }

    private static final int fetchUnopenedChests() {
        for (final var entry : TablistUtil.getTabEntries()) {
            final var clear = Utils.removeControlCodes(entry).trim();
            final var prefix = "Unclaimed chests: ";

            if (clear.startsWith(prefix)) {
                return Utils.safeParseIntFast(StringUtils.remove(clear, prefix));
            }
        }

        Utils.printErr("[DarkAddons] Unable to fetch croesus unopened chests from tab! Tab list lines:");

        for (final var entry : TablistUtil.getTabEntries()) {
            Utils.printErr(entry);
        }

        DarkAddons.queueWarning("Unable to fetch croesus unopened chests from tab! Please enable the necessary widgets, or the feature will think you have 0 unopened chests. If this is a false positive, please report this! The tab list lines are printed to your logs, please include that information in your bug report as well.");
        return 0;
    }

    @Override
    final void clear() {
        UnopenedChestsDisplay.unopenedChests = 0;
        UnopenedChestsDisplay.lastUnopenedChests = 0;

        super.clear();
    }

    @Override
    final void update() {
        if (!this.isEnabled()) {
            return;
        }

        final var isDemoRenderBypass = this.isDemoRenderBypass();

        if (!isDemoRenderBypass && !DarkAddons.isPlayerInDungeonHub()) {
            this.clear();
            return;
        }

        UnopenedChestsDisplay.unopenedChests = isDemoRenderBypass ? 0 : UnopenedChestsDisplay.fetchUnopenedChests();

        if (isDemoRenderBypass || (UnopenedChestsDisplay.lastUnopenedChests != UnopenedChestsDisplay.unopenedChests || this.isEmpty())) {
            UnopenedChestsDisplay.lastUnopenedChests = UnopenedChestsDisplay.unopenedChests;

            super.update();
        }
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        final var unopened = UnopenedChestsDisplay.unopenedChests;
        final var limit = UnopenedChestsDisplay.CROESUS_CHEST_LIMIT;

        lines.add("§6Unopened chests: " + unopened + '/' + limit);

        if (limit <= unopened) {
            lines.add("§cReached limit, new runs will remove the oldest chests!");
        }
    }
}
