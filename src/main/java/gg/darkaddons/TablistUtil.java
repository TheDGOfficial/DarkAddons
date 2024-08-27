package gg.darkaddons;

import gg.darkaddons.mixins.IMixinGuiPlayerTabOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

final class TablistUtil {
    TablistUtil() {
        super();
    }

    @NotNull
    private static ArrayList<String> tabEntries = new ArrayList<>(0);

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void onTick(@NotNull final TickEvent.ClientTickEvent event) {
        if (TickEvent.Phase.START != event.phase || !DarkAddons.isPlayerInDungeonHub()) {
            return;
        }

        TablistUtil.tabEntries = TablistUtil.fetchTabEntries();
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
