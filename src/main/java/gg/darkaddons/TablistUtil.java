package gg.darkaddons;

import gg.darkaddons.mixins.IMixinGuiPlayerTabOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

final class TablistUtil {
    private TablistUtil() {
        super();

        throw Utils.staticClassException();
    }

    @NotNull
    private static ArrayList<String> tabEntries = new ArrayList<>(0);

    static {
        DarkAddons.registerTickTask("darkaddons_fetch_tablist", 20, true, () -> {
            if (DarkAddons.isPlayerInDungeonHub()) {
                TablistUtil.tabEntries = TablistUtil.fetchTabEntries();
            }
        });
    }

    @NotNull
    static final ArrayList<String> getTabEntries() {
        return TablistUtil.tabEntries;
    }

    @NotNull
    private static final ArrayList<String> fetchTabEntries() {
        final var player = Minecraft.getMinecraft().thePlayer;

        if (null == player) {
            return new ArrayList<>(0);
        }

        final var list = IMixinGuiPlayerTabOverlay.getNetworkPlayerInfoOrdering().immutableSortedCopy(player.sendQueue.getPlayerInfoMap());
        final var entries = new ArrayList<String>(list.size());

        for (final var info : list) {
            final var displayName = info.getDisplayName();
            final var entry = null == displayName ? ScorePlayerTeam.formatPlayerName(info.getPlayerTeam(), info.getGameProfile().getName()) : displayName.getFormattedText();

            entries.add(entry);
        }

        return entries;
    }
}
