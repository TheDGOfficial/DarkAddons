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
    private static int previousScore;
    private static boolean hasSaidScoreAtBossEntry;

    ScoreFromScoreboard() {
        super();

        DarkAddons.registerTickTask("update_score_from_scoreboard", 20, true, () -> {
            if ((!Config.isSendMessageOn270Score() && !Config.isSendMessageOn300Score()) || !DarkAddons.isInDungeons()) {
                return;
            }

            for (final var line : ScoreboardUtil.fetchScoreboardLines(-1L == DungeonTimer.INSTANCE.getBossEntryTime() ? 10 : 9)) {
                final var withoutColor = Utils.removeControlCodes(line);
                if (withoutColor.startsWith("Cleared: ")) {
                    final var score = Utils.safeParseIntFast(StringUtils.removeEnd(StringUtils.substringAfter(withoutColor, "% ("), ")"));
                    ScoreFromScoreboard.onScoreUpdate(ScoreFromScoreboard.fixScoreboardScore(score), score);
                    break;
                }
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onWorldChange(@NotNull final WorldEvent.Unload event) {
        ScoreFromScoreboard.blazeDoneReceived = false;
        ScoreFromScoreboard.previousScore = 0;
        ScoreFromScoreboard.hasSaidScoreAtBossEntry = false;
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

    private static final void onScoreUpdate(final int score, final int rawScore) {
        final var sc = ScoreCalculation.INSTANCE;

        if (300 <= score && !sc.getHasSaid300()) {
            sc.setHasSaid300(true);
            Skytils.sendMessageQueue.add("/pc 300 score");
        } else if (270 <= score && !sc.getHasSaid270()) {
            sc.setHasSaid270(true);
            Skytils.sendMessageQueue.add("/pc 270 score");
        }

        final var diff = rawScore - ScoreFromScoreboard.previousScore;

        if (diff >= 28 || rawScore >= 300) {
            ScoreFromScoreboard.realScoreHook(Math.max(rawScore, sc.getTotalScore().get()), sc.getDeaths().get());
        }

        ScoreFromScoreboard.previousScore = rawScore;
    }

    private static final void realScoreHook(final int score, final int deaths) {
        if (Config.isSendMessageForScoreAtBossEntry() && (-1L != DungeonTimer.INSTANCE.getBossEntryTime() || score >= 300) && !ScoreFromScoreboard.hasSaidScoreAtBossEntry) {
            var affordableDeaths = 0;
            var extraScore = score - 300;

            var tempDeaths = deaths;
            while (extraScore > (tempDeaths < 1 ? 0 : 1)) {
                 if (tempDeaths < 1) {
                     --extraScore; // Assume spirit pet
                 } else {
                     extraScore -= 2;
                 }
                 ++tempDeaths;
                 ++affordableDeaths;
            }

            ScoreFromScoreboard.hasSaidScoreAtBossEntry = true;
            Skytils.sendMessageQueue.add("/pc Score At Boss Entry: " + score + " | " + (score >= 300 ? "Affordable Deaths For S+: " + affordableDeaths : "No S+"));
        }
    }
}
