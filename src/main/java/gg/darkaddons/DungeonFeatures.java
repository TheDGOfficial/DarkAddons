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

        if (DarkAddons.isInDungeons()) {
            if (null == DungeonFeatures.dungeonFloor) {
                for (final var line : ScoreboardUtil.fetchScoreboardLines(11)) {
                    if (line.contains("The Catacombs (")) {
                        DungeonFeatures.dungeonFloor = StringUtils.substringBetween(line, "(", ")");
                        DungeonFeatures.dungeonFloorNumber = Utils.safeParseIntFast(DungeonFeatures.dungeonFloor.substring(1, DungeonFeatures.dungeonFloor.length()));
                        break;
                    }
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
