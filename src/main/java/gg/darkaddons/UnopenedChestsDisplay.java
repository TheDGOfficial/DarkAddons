package gg.darkaddons;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
    @NotNull
    private static final Logger LOGGER = LogManager.getLogger();

    private static final int CROESUS_CHEST_LIMIT = 60;

    private static int unopenedChests = -1;
    private static int lastUnopenedChests = -1;

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

        return -1;
    }

    @Override
    final void clear() {
        UnopenedChestsDisplay.unopenedChests = -1;
        UnopenedChestsDisplay.lastUnopenedChests = -1;

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

        if (isDemoRenderBypass || UnopenedChestsDisplay.lastUnopenedChests != UnopenedChestsDisplay.unopenedChests || this.isEmpty()) {
            UnopenedChestsDisplay.lastUnopenedChests = UnopenedChestsDisplay.unopenedChests;

            super.update();
        }
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        final var unopened = UnopenedChestsDisplay.unopenedChests;
        final var limit = UnopenedChestsDisplay.CROESUS_CHEST_LIMIT;

        lines.add("§6Unopened chests: " + (unopened == -1 ? "Loading..." : unopened + "/" + limit));

        if (limit <= unopened) {
            lines.add("§cReached limit, new runs will remove the oldest chests!");
        }
    }
}
