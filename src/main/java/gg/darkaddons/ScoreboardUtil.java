package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

final class ScoreboardUtil {
    @NotNull
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    @NotNull
    private static String[] scoreboardLines = ScoreboardUtil.EMPTY_STRING_ARRAY;

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private ScoreboardUtil() {
        super();

        throw Utils.staticClassException();
    }

    static final void init() {
        DarkAddons.registerTickTask("fetch_scoreboard_lines", 1, true, () -> ScoreboardUtil.scoreboardLines = ScoreboardUtil.fetchScoreboardLines());
    }

    @NotNull
    static final String[] getScoreboardLines() {
        return ScoreboardUtil.scoreboardLines;
    }

    @Nullable
    static final String cleanSB(@Nullable final String scoreboard) {
        if (null == scoreboard) {
            return null;
        }

        final var chars = Utils.removeControlCodes(scoreboard).toCharArray();

        final var builder = new StringBuilder(chars.length);
        for (final var ch : chars) {
            if (32 <= ch && 126 >= ch) {
                builder.append(ch);
            }
        }

        return builder.toString();
    }

    @NotNull
    private static final String[] fetchScoreboardLines() {
        final var mc = Minecraft.getMinecraft();
        final var world = mc.theWorld;

        if (null == world) {
            return ScoreboardUtil.EMPTY_STRING_ARRAY;
        }

        final var scoreboard = world.getScoreboard();

        if (null == scoreboard) {
            return ScoreboardUtil.EMPTY_STRING_ARRAY;
        }

        final var objective = scoreboard.getObjectiveInDisplaySlot(1);

        if (null == objective) {
            return ScoreboardUtil.EMPTY_STRING_ARRAY;
        }

        final var scores = scoreboard.getSortedScores(objective);
        final var maxSize = Math.min(15, scores.size()); // Limit 15

        if (0 == maxSize) {
            return ScoreboardUtil.EMPTY_STRING_ARRAY;
        }

        final var lines = new String[maxSize];

        var index = maxSize - 1; // Fill the array in reverse order
        var count = 0; // Track the number of valid elements

        for (final var score : scores) {
            if (0 > index) {
                break;
            }

            if (null != score) {
                final var playerName = score.getPlayerName();

                if (null != playerName && !(!playerName.isEmpty() && '#' == playerName.charAt(0))) {
                    lines[index--] = ScoreboardUtil.cleanSB(ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(playerName), playerName));
                    ++count; // Count only valid elements
                }
            }
        }

        // If we added fewer than maxSize elements, trim the array
        return count == maxSize ? lines : Arrays.copyOfRange(lines, index + 1, maxSize);
    }
}
