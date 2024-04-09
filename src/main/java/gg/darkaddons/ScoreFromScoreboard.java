package gg.darkaddons;

import gg.skytils.skytilsmod.Skytils;
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;
import gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation;
import gg.skytils.skytilsmod.listeners.DungeonListener;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

final class ScoreFromScoreboard {
    private static boolean blazeDoneReceived;

    ScoreFromScoreboard() {
        super();

        DarkAddons.registerTickTask("update_score_from_scoreboard", 20, true, () -> {
            if (!Config.isSendMessageOn270Score() && !Config.isSendMessageOn300Score() || !DarkAddons.isInDungeons()) {
                return;
            }

            for (final var line : ScoreboardUtil.fetchScoreboardLines(-1L == DungeonTimer.INSTANCE.getBossEntryTime() ? 9 : 8)) {
                final var withoutColor = Utils.removeControlCodes(line);
                if (withoutColor.startsWith("Cleared: ")) {
                    final var score = Utils.safeParseIntFast(StringUtils.removeEnd(StringUtils.substringAfter(withoutColor, "% ("), ")"));
                    ScoreFromScoreboard.onScoreUpdate(ScoreFromScoreboard.fixScoreboardScore(score));
                }
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onWorldChange(@NotNull final WorldEvent.Unload event) {
        ScoreFromScoreboard.blazeDoneReceived = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onClientChatReceived(@NotNull final ClientChatReceivedEvent event) {
        if (MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            final var message = Utils.removeControlCodes(event.message.getUnformattedText());
            if (message.contains("Blaze Done")) {
                ScoreFromScoreboard.blazeDoneReceived = true;
            }
        }
    }

    private static final int fixScoreboardScore(final int scoreboardScore) {
        var fixedScore = scoreboardScore;

        if (-1L == DungeonTimer.INSTANCE.getBossEntryTime()) {
            fixedScore += 28;
        }

        if (ScoreFromScoreboard.blazeDoneReceived && DungeonListener.INSTANCE.getMissingPuzzles().contains("Higher Or Lower")) {
            fixedScore += 14;
        }

        return fixedScore;
    }

    private static final void onScoreUpdate(final int score) {
        final var sc = ScoreCalculation.INSTANCE;

        if (300 <= score && !sc.getHasSaid300()) {
            sc.setHasSaid300(true);
            Skytils.sendMessageQueue.add("/pc 300 score");
        } else if (270 <= score && !sc.getHasSaid270()) {
            sc.setHasSaid270(true);
            Skytils.sendMessageQueue.add("/pc 270 score");
        }
    }
}
