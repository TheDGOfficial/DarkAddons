package gg.darkaddons;

import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S3EPacketTeams;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import org.jetbrains.annotations.NotNull;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

public final class DungeonTimer {
    DungeonTimer() {
        super();
    }

    @NotNull
    private static final Matcher dungeonClearedPatternMatcher = Pattern.compile("Cleared: (?<percentage>\\d+)% \\(\\d+\\)").matcher("");

    private static long dungeonStartTime = -1L;
    private static long bossEntryTime = -1L;
    private static long bossClearTime = -1L;
    private static long phase1ClearTime = -1L;
    private static long phase2ClearTime = -1L;
    private static long phase3ClearTime = -1L;
    private static long phase4ClearTime = -1L;
    private static long terraClearTime = -1L;

    static final void onPacketReceived(@NotNull final Packet<?> p) {
        if (!DarkAddons.isInSkyblock() || !(p instanceof final S3EPacketTeams packet)) {
            return;
        }

        if (2 != packet.getAction()) {
            return;
        }

        final var line = Utils.removeControlCodes(packet.getPrefix() + String.join(" ", packet.getPlayers()) + packet.getSuffix());

        if (line.startsWith("Cleared: ")) {
            final var matcher = DungeonTimer.dungeonClearedPatternMatcher.reset(line);
            if (matcher.find()) {
                if (-1L == DungeonTimer.dungeonStartTime) {
                    DungeonTimer.dungeonStartTime = System.currentTimeMillis();
                }
                return;
            }
        }

        if (line.startsWith("Time Elapsed:")) {
            if (-1L == DungeonTimer.dungeonStartTime) {
                DungeonTimer.dungeonStartTime = System.currentTimeMillis();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onChat(@NotNull final ClientChatReceivedEvent event) {
        if (!DarkAddons.isInDungeons() || !MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            return;
        }

        final var message = event.message.getFormattedText();
        final var unformatted = Utils.removeControlCodes(event.message.getUnformattedText());

        final var currentTime = System.currentTimeMillis();
        final var dungeonFloorNumber = DungeonFeatures.getDungeonFloorNumber();

        if (-1L == DungeonTimer.bossEntryTime && unformatted.startsWith("[BOSS] ") && unformatted.contains(":")) {
            final var bossName = StringUtils.substringBefore(StringUtils.substringAfter(unformatted, "[BOSS] "), ":").trim();
            if (!"The Watcher".equals(bossName) && null != dungeonFloorNumber && Utils.checkBossName(dungeonFloorNumber, bossName)) {
                DungeonTimer.bossEntryTime = currentTime;
            }
        } else if (-1L != DungeonTimer.bossEntryTime && -1L == DungeonTimer.bossClearTime && message.contains("§r§c☠ §r§eDefeated §r")) {
            DungeonTimer.bossClearTime = currentTime;
        } else if (null != dungeonFloorNumber && 7 == dungeonFloorNumber && message.startsWith("§r§4[BOSS] ")) {
            if (message.endsWith("§r§cPathetic Maxor, just like expected.§r") && -1L == DungeonTimer.phase1ClearTime) {
                DungeonTimer.phase1ClearTime = currentTime;
            } else if (message.endsWith("§r§cWho dares trespass into my domain?§r") && -1L == DungeonTimer.phase2ClearTime) {
                DungeonTimer.phase2ClearTime = currentTime;
            } else if (message.endsWith("§r§cYou went further than any human before, congratulations.§r") && -1L == DungeonTimer.phase3ClearTime) {
                DungeonTimer.phase3ClearTime = currentTime;
            } else if (message.endsWith("§r§cAll this, for nothing...§r")) {
                DungeonTimer.phase4ClearTime = currentTime;
            }
        } else if (null != dungeonFloorNumber && 6 == dungeonFloorNumber && message.startsWith("§r§c[BOSS] Sadan") && message.endsWith("§r§f: ENOUGH!§r") && -1L == DungeonTimer.terraClearTime) {
            DungeonTimer.terraClearTime = currentTime;
        }
    }

    @SubscribeEvent
    public final void onWorldChange(@NotNull final WorldEvent.Unload event) {
        DungeonTimer.dungeonStartTime = -1L;
        DungeonTimer.bossEntryTime = -1L;
        DungeonTimer.bossClearTime = -1L;
        DungeonTimer.phase1ClearTime = -1L;
        DungeonTimer.phase2ClearTime = -1L;
        DungeonTimer.phase3ClearTime = -1L;
        DungeonTimer.phase4ClearTime = -1L;
        DungeonTimer.terraClearTime = -1L;
    }

    static final long getDungeonStartTime() {
        return DungeonTimer.dungeonStartTime;
    }

    public static final long getBossEntryTime() {
        return DungeonTimer.bossEntryTime;
    }

    public static final long getBossClearTime() {
        return DungeonTimer.bossClearTime;
    }

    public static final long getPhase1ClearTime() {
        return DungeonTimer.phase1ClearTime;
    }

    static final long getPhase2ClearTime() {
        return DungeonTimer.phase2ClearTime;
    }

    static final long getPhase3ClearTime() {
        return DungeonTimer.phase3ClearTime;
    }

    static final long getPhase4ClearTime() {
        return DungeonTimer.phase4ClearTime;
    }

    static final long getTerraClearTime() {
        return DungeonTimer.terraClearTime;
    }
}
