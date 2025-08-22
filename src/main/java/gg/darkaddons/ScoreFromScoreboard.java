package gg.darkaddons;

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
    private static boolean sentTitleOn301Score;

    ScoreFromScoreboard() {
        super();

        DarkAddons.registerTickTask("update_score_from_scoreboard", 20, true, () -> {
            if (!Config.isSendMessageOn270Score() && !Config.isSendMessageOn300Score() || !DarkAddons.isInDungeons()) {
                return;
            }

            for (final var line : ScoreboardUtil.getScoreboardLines()) {
                final var withoutColor = Utils.removeControlCodes(line);
                if (withoutColor.startsWith("Cleared: ")) {
                    if (-1L != DungeonTimer.getDungeonStartTime()) {
                        final var score = Utils.safeParseIntFast(StringUtils.removeEnd(StringUtils.substringAfter(withoutColor, "% ("), ")"));
                        ScoreFromScoreboard.onScoreUpdate(ScoreFromScoreboard.fixScoreboardScore(score), score);
                    }
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
        ScoreFromScoreboard.sentTitleOn301Score = false;
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

        if (-1L == DungeonTimer.getBossEntryTime()) {
            fixedScore += 28;
        }

        if (ScoreFromScoreboard.blazeDoneReceived && DungeonListener.getIncompletePuzzles().contains("Higher Or Lower")) {
            fixedScore += 14;
        }

        return fixedScore;
    }

    private static final void onScoreUpdate(final int score, final int rawScore) {
        if (Config.isSendMessageOn300Score() && 300 <= score && !ScoreCalculation.getHasSaid300()) {
            ScoreCalculation.setHasSaid300(true);
            DarkAddons.queueUserSentMessageOrCommand("/pc 300 score");
        } else if (Config.isSendMessageOn270Score() && 270 <= score && !ScoreCalculation.getHasSaid270()) {
            ScoreCalculation.setHasSaid270(true);
            DarkAddons.queueUserSentMessageOrCommand("/pc 270 score");
        }

        final var calculatedScore = ScoreCalculation.getTotalScore().get();
        final var highestScore = Math.max(score, calculatedScore);
        final var deaths = ScoreCalculation.getDeaths().get();
        final var scoreReq = AdditionalM7Features.isInM7OrF7() ? 1 <= deaths ? 302 : 301 : 300;

        if (Config.isSendMessageOn300Score() && Config.isSendTitleOn301Score() && highestScore >= scoreReq && !ScoreFromScoreboard.sentTitleOn301Score) {
            ScoreFromScoreboard.sentTitleOn301Score = true;
            GuiManager.createTitle("§a✔ " + highestScore + " Score!", "§a§lYou can go in.", 60, 60, true, GuiManager.Sound.LEVEL_UP);
        }

        final var diff = rawScore - ScoreFromScoreboard.previousScore;

        if ((28 <= diff && -1L != DungeonTimer.getBossEntryTime()) || 300 <= rawScore) {
            ScoreFromScoreboard.realScoreHook(Math.max(rawScore, calculatedScore), deaths);
        }

        ScoreFromScoreboard.previousScore = rawScore;
    }

    private static final void realScoreHook(final int score, final int deaths) {
        if ((Config.isSendMessageOn300Score() && Config.isSendMessageForScoreAtBossEntry()) && (-1L != DungeonTimer.getBossEntryTime() || 300 <= score) && !ScoreFromScoreboard.hasSaidScoreAtBossEntry && -1L == DungeonTimer.getBossClearTime()) {
            var affordableDeaths = 0;
            var extraScore = score - 300;

            var tempDeaths = deaths;
            while (extraScore > (1 > tempDeaths ? 0 : 1)) {
                if (1 > tempDeaths) {
                    --extraScore; // Assume spirit pet
                } else {
                    extraScore -= 2;
                }
                ++tempDeaths;
                ++affordableDeaths;
            }

            ScoreFromScoreboard.hasSaidScoreAtBossEntry = true;
            DarkAddons.queueUserSentMessageOrCommand("/pc Score At Boss Entry: " + score + " | " + (300 <= score ? "Affordable Deaths For S+: " + affordableDeaths : "No S+"));
        }
    }
}
