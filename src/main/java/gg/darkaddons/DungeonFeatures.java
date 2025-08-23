package gg.darkaddons;

import net.minecraft.client.Minecraft;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import org.apache.commons.lang3.StringUtils;

public final class DungeonFeatures {
    DungeonFeatures() {
        super();
    }

    @Nullable
    private static String dungeonFloor;

    @Nullable
    private static Integer dungeonFloorNumber;

    private static boolean hasBossSpawned;

    @SubscribeEvent
    public final void onTick(@NotNull final TickEvent.ClientTickEvent event) {
        final var mc = Minecraft.getMinecraft();

        if (TickEvent.Phase.START != event.phase || null == mc.thePlayer || null == mc.theWorld) {
            return;
        }

        // If you join an F7 and then join M7 with the command without leaving the F7, the WorldEvent.Unload triggers while the scoreboard still says F7, and so you will be in a bugged state in M7 with the floor being detected as F7. To fix this rare bug, we keep re-assigning the dungeon floor till the dungeon starts in addition to the null check.
        if (DarkAddons.isInDungeons() && (null == DungeonFeatures.dungeonFloor || -1L == DungeonTimer.getDungeonStartTime())) {
            for (final var line : ScoreboardUtil.getScoreboardLines()) {
                if (line.contains("The Catacombs (")) {
                    DungeonFeatures.dungeonFloor = StringUtils.substringBetween(line, "(", ")");
                    DungeonFeatures.dungeonFloorNumber = "E".equals(DungeonFeatures.dungeonFloor) ? 0 : Utils.safeParseIntFast(DungeonFeatures.dungeonFloor.substring(1));
                    break;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onChat(@NotNull final ClientChatReceivedEvent event) {
        if (!DarkAddons.isInDungeons() || !MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            return;
        }

        final var unformatted = Utils.removeControlCodes(event.message.getUnformattedText());

        if (unformatted.startsWith("[BOSS]") && unformatted.contains(":")) {
            final var bossName = StringUtils.substringBefore(StringUtils.substringAfter(unformatted, "[BOSS] "), ":").trim();
            final Integer dungeonFloorNumber;

            if (!DungeonFeatures.hasBossSpawned && !"The Watcher".equals(bossName) && null != (dungeonFloorNumber = DungeonFeatures.dungeonFloorNumber) && Utils.checkBossName(dungeonFloorNumber, bossName)) {
                DungeonFeatures.hasBossSpawned = true;
            }
        }
    }

    @SubscribeEvent
    public final void onWorldChange(@NotNull final WorldEvent.Unload event) {
        DungeonFeatures.dungeonFloor = null;
        DungeonFeatures.dungeonFloorNumber = null;
        DungeonFeatures.hasBossSpawned = false;
    }

    @Nullable
    static final Integer getDungeonFloorNumber() {
        return DungeonFeatures.dungeonFloorNumber;
    }

    static final boolean getHasBossSpawned() {
        return DungeonFeatures.hasBossSpawned;
    }

    @Nullable
    static final String getDungeonFloor() {
        return DungeonFeatures.dungeonFloor;
    }
}
