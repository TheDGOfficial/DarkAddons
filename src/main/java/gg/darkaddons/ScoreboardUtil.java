package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.scoreboard.ScorePlayerTeam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        final var lines = new ArrayList<String>(scores.size());

        var i = 0;
        for (final var score : scores) {
            if (i >= limit) {
                break;
            }

            if (null != score) {
                final var playerName = score.getPlayerName();

                if (null != playerName && !(!playerName.isEmpty() && '#' == playerName.charAt(0))) {
                    lines.add(ScoreboardUtil.cleanSB(ScorePlayerTeam.formatPlayerName(scoreboard.getPlayersTeam(playerName), playerName)));
                }
            }

            ++i;
        }

        Collections.reverse(lines);

        return lines.toArray(ScoreboardUtil.EMPTY_STRING_ARRAY);
    }
}
