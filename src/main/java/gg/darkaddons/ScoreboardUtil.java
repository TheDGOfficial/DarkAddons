package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

final class ScoreboardUtil {
    @NotNull
    private static final String[] EMPTY_STRING_ARRAY = new String[0];

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
    static final String[] fetchScoreboardLines(final int limit) {
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
        final var maxSize = Math.min(limit, scores.size());

        if (maxSize == 0) {
            return ScoreboardUtil.EMPTY_STRING_ARRAY;
        }

        final var lines = new String[maxSize];

        var index = maxSize - 1; // Fill array in reverse order
        var count = 0; // Track number of valid elements

        for (final var score : scores) {
            if (index < 0) {
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
        return (count == maxSize) ? lines : Arrays.copyOfRange(lines, index + 1, maxSize);
    }
}
