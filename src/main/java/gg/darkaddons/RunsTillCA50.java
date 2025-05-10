package gg.darkaddons;

import net.minecraft.client.Minecraft;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.EOFException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import java.lang.reflect.InvocationTargetException;

import org.jetbrains.annotations.Nullable;

final class RunsTillCA50 {
    @NotNull
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private RunsTillCA50() {
        super();

        throw Utils.staticClassException();
    }

    /**
     * XP gained from M6 with 300 Score and Maxed XP boost perks.
     */
    private static final double M6_CATA_XP = 168_000.0D;

    private static final double M6_CLASS_XP = 120_000.0D;
    private static final double M6_SHARED_XP = 30_000.0D;

    /**
     * XP gained from M7 with 300 Score and Maxed XP boost perks. Is exactly the * 3 of the M6 values.
     */
    private static final double M7_CATA_XP = RunsTillCA50.M6_CATA_XP * 3.0D;

    private static final double M7_CLASS_XP = RunsTillCA50.M6_CLASS_XP * 3.0D;
    private static final double M7_SHARED_XP = RunsTillCA50.M6_SHARED_XP * 3.0D;

    /**
     * XP multiplier from Mayor Derpy.
     */
    private static final double DERPY_MULTIPLIER = 1.5D;

    /**
     * Represents the Max Catacombs and Class Levels before Overflow Levels start.
     *
     * See {@link RunsTillCA50#MAX_LEVEL_XP} for how much XP this max level requires.
     */
    private static final int MAX_LEVEL = 50;

    /**
     * XP required for Maxed Catacombs and Class Levels.
     */
    private static final double MAX_LEVEL_XP = 569_809_640.0D;

    /**
     * XP required leveling up overflow Catacombs and Class Levels.
     */
    private static final double XP_TO_LVL_UP_OVERFLOW_LEVEL = 200_000_000.0D;

    /**
     * Caches UUIDs of usernames up to 1 minute to avoid sending a request each time.
     */
    @NotNull
    private static final Cache<String, UUID> uuidCache = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.MINUTES).build();
    /**
     * Caches ranks, MVP+ colors and usernames of uuids up to 1 minute to reduce API requests.
     */
    @NotNull
    private static final Cache<UUID, String> rankCache = CacheBuilder.newBuilder().expireAfterAccess(1L, TimeUnit.MINUTES).build();
    /**
     * Holds last created data to access it without sending a request again if needed. Should be cleared afterward to not create memory leaks, though.
     */
    @Nullable
    static RunsTillCA50.PlayerDataHolder lastData;

    @SuppressWarnings("LambdaCanBeReplacedWithAnonymous")
    @NotNull
    static final ExecutorService apiFetcher = Executors.newSingleThreadExecutor((@NotNull final Runnable r) -> Utils.newThread(r, "DarkAddons API Fetcher Thread"));

    static final void init() {
        RunsTillCA50.apiFetcher.execute(RunsTillCA50.DungeonLeveling::init);
    }

    private static final void invalidPlayer() {
        DarkAddons.queueWarning("Couldn't get UUID from this username, check if it is correct!");
    }

    enum Mode {
        OPTIMAL, EARLY, SKULL;

        private Mode() {

        }

        @Nullable
        static final RunsTillCA50.Mode parseMode(@NotNull final String modeToParse) {
            for (final var mode : RunsTillCA50.Mode.values()) {
                if (mode.name().toLowerCase(Locale.ROOT).equals(modeToParse)) {
                    return mode;
                }
            }
            return null;
        }
    }

    static final void calculate(@NotNull final String username, final boolean m7, @NotNull final Runnable callback, final boolean derpy, @NotNull final RunsTillCA50.Mode mode) {
        final var cached = RunsTillCA50.uuidCache.getIfPresent(username);
        if (null == cached) {
            DarkAddons.queueWarning("Fetching UUID for " + username + "...");
            RunsTillCA50.apiFetcher.execute(() -> MojangUtil.getUUIDFromUsername(username, (@Nullable final UUID uuid) -> {
                if (null == uuid) {
                    RunsTillCA50.invalidPlayer();
                    return;
                }

                RunsTillCA50.uuidCache.put(username, uuid);
                RunsTillCA50.calculateUUID0(uuid, m7, callback, derpy, mode);
            }, (@Nullable final Throwable t) -> RunsTillCA50.invalidPlayer()));
        } else {
            RunsTillCA50.apiFetcher.execute(() -> RunsTillCA50.calculateUUID(cached, m7, callback, derpy, mode));
        }
    }

    static final void calculateUUID(@Nullable final UUID uuid, final boolean m7, @NotNull final Runnable callback, final boolean derpy, @NotNull final RunsTillCA50.Mode mode) {
        RunsTillCA50.apiFetcher.execute(() -> RunsTillCA50.calculateUUID0(uuid, m7, callback, derpy, mode));
    }

    static final class PlayerDataHolder {
        private final double cataXp;

        @NotNull
        private final Map<DungeonListener.DungeonClass, Double> classExperiencesMap;

        private final long currentCompletions;

        final double healerXp;
        final double mageXp;
        final double berserkerXp;
        final double archerXp;
        final double tankXp;

        private PlayerDataHolder(final double cataXpIn, @NotNull final Map<DungeonListener.DungeonClass, Double> classExperiencesMapIn, final long currentCompletionsIn, final double healerXpIn, final double mageXpIn, final double berserkerXpIn, final double archerXpIn, final double tankXpIn) {
            super();

            this.cataXp = cataXpIn;

            this.classExperiencesMap = classExperiencesMapIn;

            this.currentCompletions = currentCompletionsIn;

            this.healerXp = healerXpIn;
            this.mageXp = mageXpIn;
            this.berserkerXp = berserkerXpIn;
            this.archerXp = archerXpIn;
            this.tankXp = tankXpIn;
        }

        PlayerDataHolder(final double healerXpIn, final double mageXpIn, final double berserkerXpIn, final double archerXpIn, final double tankXpIn) {
            this(0.0D, null, 0L, healerXpIn, mageXpIn, berserkerXpIn, archerXpIn, tankXpIn);
        }

        @Override
        public final String toString() {
            return "PlayerDataHolder{" +
                "cataXp=" + this.cataXp +
                ", classExperiencesMap=" + this.classExperiencesMap +
                ", currentCompletions=" + this.currentCompletions +
                ", healerXp=" + this.healerXp +
                ", mageXp=" + this.mageXp +
                ", berserkerXp=" + this.berserkerXp +
                ", archerXp=" + this.archerXp +
                ", tankXp=" + this.tankXp +
                '}';
        }
    }

    @Nullable
    static final RunsTillCA50.PlayerDataHolder extractDataFor(@NotNull final UUID uuid,
                                                              final boolean m7,
                                                              final boolean printWarnings) {
        try {
            final var data = HypixelAPI.getDungeonStats(uuid);

            if (!data.isSuccessful()) {
                if (printWarnings) {
                    switch (data.getFailReason()) {
                        case NO_API_DATA:
                            DarkAddons.queueWarning("Can't find API data for this player, no Skyblock profiles?");
                            break;
                        case NO_DUNGEON_DATA:
                            DarkAddons.queueWarning("This player didn't enter The Catacombs or earn any Catacombs XP, can't proceed. If you believe this incorrect, check if the player's last played profile is the correct profile.");
                            break;
                    }
                }
                return null;
            }

            final var classExperiences = new EnumMap<DungeonListener.DungeonClass, Double>(DungeonListener.DungeonClass.class);

            Arrays.stream(DungeonListener.DungeonClass.values()).forEach(dungeonClass -> classExperiences.put(dungeonClass, data.getClassXpForClass(dungeonClass)));

            return RunsTillCA50.createDataHolder(uuid, data.getCataXp(), data.getMasterTierCompletionsForFloor(m7 ? 7 : 6), classExperiences);
        } catch (final Throwable t) {
            RunsTillCA50.handleError(t, printWarnings);
        }

        return null;
    }

    private static final boolean calculateUUID1(@Nullable final UUID uuid, final boolean m7, final boolean derpy, final double cataXp, @NotNull final Map<DungeonListener.DungeonClass, Double> classExperiences, @NotNull final RunsTillCA50.Mode mode, final long currentCompletions) {
        var formattedRankAndName = RunsTillCA50.rankCache.getIfPresent(uuid);

        if (null == formattedRankAndName) {
            final var formatted = HypixelAPI.getFormattedRankAndName(uuid);

            if (null == formatted) {
                DarkAddons.queueWarning("Error obtaining Hypixel player information; can't proceed.");
                return false;
            }

            formattedRankAndName = formatted;
            RunsTillCA50.rankCache.put(uuid, formattedRankAndName);
        }

        return RunsTillCA50.calculateDirect(Utils.toPrimitive(classExperiences.get(DungeonListener.DungeonClass.HEALER)), Utils.toPrimitive(classExperiences.get(DungeonListener.DungeonClass.MAGE)), Utils.toPrimitive(classExperiences.get(DungeonListener.DungeonClass.BERSERK)), Utils.toPrimitive(classExperiences.get(DungeonListener.DungeonClass.ARCHER)), Utils.toPrimitive(classExperiences.get(DungeonListener.DungeonClass.TANK)), cataXp, m7, currentCompletions, formattedRankAndName, derpy, mode);
    }

    private static final @NotNull RunsTillCA50.PlayerDataHolder createDataHolder(@NotNull final UUID uuid, final double cataXp, final long completions, @NotNull final Map<DungeonListener.DungeonClass, Double> classExperiences) {
        final var data = new RunsTillCA50.PlayerDataHolder(cataXp, classExperiences, completions, Utils.toPrimitive(classExperiences.get(DungeonListener.DungeonClass.HEALER)), Utils.toPrimitive(classExperiences.get(DungeonListener.DungeonClass.MAGE)), Utils.toPrimitive(classExperiences.get(DungeonListener.DungeonClass.BERSERK)), Utils.toPrimitive(classExperiences.get(DungeonListener.DungeonClass.ARCHER)), Utils.toPrimitive(classExperiences.get(DungeonListener.DungeonClass.TANK)));

        if (Minecraft.getMinecraft().thePlayer.getUniqueID().equals(uuid)) {
            RunsTillCA50.lastData = data;
        }

        return data;
    }

    private static final void calculateUUID0(@Nullable final UUID uuid, final boolean m7, @NotNull final Runnable callback, final boolean derpy, @NotNull final RunsTillCA50.Mode mode) {
        if (null == uuid) {
            RunsTillCA50.invalidPlayer();
            return;
        }

        DarkAddons.queueWarning("Fetching XP from API for the player...");

        final var playerData = RunsTillCA50.extractDataFor(uuid, m7, true);

        if (null == playerData) {
            // Warning should be printed by extractDataFor method since we pass printWarnings parameter true above.
            return;
        }

        if (RunsTillCA50.calculateUUID1(uuid, m7, derpy, playerData.cataXp, playerData.classExperiencesMap, mode, playerData.currentCompletions)) {
            callback.run();
        }
    }

    private static final void handleError(@NotNull final Throwable t,
                                          final boolean printWarnings) {
        if (t instanceof InvocationTargetException) {
            RunsTillCA50.handleError(t.getCause(), printWarnings);
            return;
        }

        if (t.getClass().getName().contains("Timeout")) {
            if (printWarnings) {
                DarkAddons.queueWarning("Request timed out while fetching data, please retry at a later time!");
            }
        } else if (t instanceof EOFException) {
            if (printWarnings) {
                DarkAddons.queueWarning("Unexpected end of line error while fetching data; please retry again at a later time!");
            }
        } else if (t instanceof NumberFormatException) {
            if (printWarnings) {
                DarkAddons.queueWarning("Parsing issue from the result gathered from API; Hypixel API format changed? Updating Skytils or " + DarkAddons.MOD_NAME + " might fix this error. The full error will be printed to the logs for debugging purposes after this message, although you probably can't fix this error yourself.");
            }
            RunsTillCA50.LOGGER.catching(t);
        } else {
            DarkAddons.modError(t);
        }
    }

    private static final boolean calculateDirect(final double healerXp, final double mageXp, final double bersXp, final double archerXp, final double tankXp, final double cataXp, final boolean m7, final long currentCompletions, @NotNull final String formattedRankAndName, final boolean derpy, @NotNull final RunsTillCA50.Mode mode) {
        final var xpMap = RunsTillCA50.generateXpMap(healerXp, mageXp, bersXp, archerXp, tankXp);
        final var originalXpMap = new EnumMap<>(xpMap);

        if (RunsTillCA50.isCA50(xpMap)) {
            DarkAddons.queueWarning("Player is already CA50, nothing to do.");
            return false;
        }

        RunsTillCA50.outputResults(RunsTillCA50.simulateAllRuns(xpMap, new EnumMap<>(xpMap), cataXp, m7, derpy, mode), originalXpMap, xpMap, m7, currentCompletions, formattedRankAndName, derpy, mode);
        return true;
    }

    static final double getTotalExperiencesNoOverflow(@SuppressWarnings("CollectionDeclaredAsConcreteClass") @NotNull final EnumMap<DungeonListener.DungeonClass, Double> xpMap) {
        var result = 0.0D;

        for (final double v : xpMap.values()) {
            result += Math.min(RunsTillCA50.MAX_LEVEL_XP, v);

            //noinspection IfCanBeAssertion
            if (Double.POSITIVE_INFINITY == result || Double.NEGATIVE_INFINITY == result) {
                throw new ArithmeticException("double overflow");
            }
        }

        return result;
    }

    @NotNull
    static final EnumMap<DungeonListener.DungeonClass, Double> generateXpMap(final double healerXp, final double mageXp, final double bersXp, final double archerXp, final double tankXp) {
        return RunsTillCA50.generateMap(healerXp, mageXp, bersXp, archerXp, tankXp);
    }

    @NotNull
    static final <T extends Number> EnumMap<DungeonListener.DungeonClass, T> generateMap(@NotNull final T healer, @NotNull final T mage, @NotNull final T bers, @NotNull final T archer, @NotNull final T tank) {
        final var map = new EnumMap<DungeonListener.DungeonClass, T>(DungeonListener.DungeonClass.class);

        map.put(DungeonListener.DungeonClass.HEALER, healer);
        map.put(DungeonListener.DungeonClass.MAGE, mage);
        map.put(DungeonListener.DungeonClass.BERSERK, bers);
        map.put(DungeonListener.DungeonClass.ARCHER, archer);
        map.put(DungeonListener.DungeonClass.TANK, tank);

        return map;
    }

    private static final void outputResultsHeader(@NotNull final String formattedRankAndName, @NotNull final RunsTillCA50.ProgramResult result, final boolean m7, final boolean derpy, @NotNull final RunsTillCA50.Mode mode, @NotNull final EnumMap<DungeonListener.DungeonClass, Double> originalXpMap, @NotNull final EnumMap<DungeonListener.DungeonClass, Double> xpMap) {
        DarkAddons.echoEmpty();
        RunsTillCA50.outputModeInfo(mode);
        DarkAddons.echoEmpty();
        DarkAddons.queueWarning("Class Average 50 Calculation Of " + formattedRankAndName);
        DarkAddons.echoEmpty();
        final var totalExperiences = RunsTillCA50.getTotalExperiencesNoOverflow(originalXpMap);
        final var totalFinishExperiences = RunsTillCA50.getTotalExperiencesNoOverflow(xpMap);
        DarkAddons.queueWarning(result.totalRuns + " Total " + (m7 ? "M7" : "M6") + " Runs." + (derpy ? " (With Derpy)" : "") + " | Overall Progress: %" + String.format(Locale.ROOT, "%.2f", totalExperiences / Math.max(1.0D, totalFinishExperiences) * 100.0D));
        DarkAddons.echoEmpty();
        DarkAddons.queueWarning("Class | Runs to CA50 | Level Before Swap Class | Level After CA50 | Overflow Runs Done");
        DarkAddons.echoEmpty();
    }

    private static final void outputModeInfo(@NotNull final RunsTillCA50.Mode mode) {
        final var modeName = mode.name().charAt(0) + mode.name().toLowerCase(Locale.ROOT).substring(1, mode.name().length());
        final var modeInfo = modeName + " mode: ";
        DarkAddons.queueWarning(modeInfo + switch (mode) {
            case OPTIMAL -> "This mode shows the runs needed with the best way to get Class Average 50.";
            case EARLY ->
                "This mode gets Level 50 in a class before switching to playing another class. You will get individual Level 50's earlier, but getting it all 50 will take a lot longer.";
            case SKULL ->
                "This mode only plays the class you have highest XP on till you get Class Average 50 just from shared XP for the other classes.";
        });
    }

    private static final void outputResults(@NotNull final RunsTillCA50.ProgramResult result, @NotNull final EnumMap<DungeonListener.DungeonClass, Double> originalXpMap, @NotNull final EnumMap<DungeonListener.DungeonClass, Double> xpMap, final boolean m7, final long currentCompletions, @NotNull final String formattedRankAndName, final boolean derpy, @NotNull final RunsTillCA50.Mode mode) {
        RunsTillCA50.outputResultsHeader(formattedRankAndName, result, m7, derpy, mode, originalXpMap, xpMap);

        for (final var dungeonClass : DungeonListener.DungeonClass.values()) {
            final var endXp = xpMap.get(dungeonClass);
            final var overflowRunsDone = (int) ((endXp - RunsTillCA50.MAX_LEVEL_XP) / RunsTillCA50.xpGained(true, m7, derpy));

            DarkAddons.queueWarning(dungeonClass.name().charAt(0) + dungeonClass.name().toLowerCase(Locale.ROOT).substring(1, dungeonClass.name().length()) + " | " + Objects.toString(result.runsAsClass.get(dungeonClass), "0") + " Runs." + " | " + String.format(Locale.ROOT, "%.2f", RunsTillCA50.Mode.EARLY == mode ? Math.max(RunsTillCA50.MAX_LEVEL, RunsTillCA50.xpToLevel(result.finishXp.get(dungeonClass))) : RunsTillCA50.xpToLevel(result.finishXp.get(dungeonClass))) + " | " + String.format(Locale.ROOT, "%.2f", RunsTillCA50.xpToLevel(endXp)) + " | " + overflowRunsDone + " Runs.");
        }

        RunsTillCA50.outputResultsFooter(result, m7, currentCompletions, derpy);
    }

    private static final void outputResultsFooter(@NotNull final RunsTillCA50.ProgramResult result, final boolean m7, final long currentCompletions, final boolean derpy) {
        DarkAddons.echoEmpty();
        DarkAddons.queueWarning("By the time you get CA50 with these runs, you will have Cata " + String.format(Locale.ROOT, "%.2f", RunsTillCA50.xpToLevel(result.finishCataXp)) + " and " + (currentCompletions + result.totalRuns) + ' ' + (m7 ? "M7" : "M6") + " completions.");
        DarkAddons.echoEmpty();
        DarkAddons.queueWarning("Note: It is assumed that you use a helmet with Hecatomb 10 for extra experience and that you have at least 25 Completions on the floor plus the Catacombs Expert Ring accessory and Essence Shop Class XP boost perks maxed for the max experience.");
        DarkAddons.echoEmpty();
        DarkAddons.queueWarning("Disclaimer: These runs does not take Daily XP bonus of %40 more XP" + (derpy ? "" : ", or 1.5x XP of Derpy") + " into account. It also assumes you get exactly 300 Score each run because any score above 300 will add some slight bonus XP to your runs. So Paul 317 Score is also more XP than 307 Score without Paul. All combined it will probably take you less runs than what the output says if you take advantage of Daily XP," + (derpy ? "" : " Derpy,") + " Paul and Extra Score.");
    }

    private static final double padStart(@NotNull final String number) {
        try {
            final var parsed = Double.parseDouble(number);

            return 10.0D > parsed ? Double.parseDouble("0." + number) : parsed;
        } catch (final NumberFormatException nfe) {
            DarkAddons.modError(nfe);
            return 0.0D;
        }
    }

    private static final double xpToLevel(double xp) {
        if (RunsTillCA50.MAX_LEVEL_XP <= xp) {
            xp -= RunsTillCA50.MAX_LEVEL_XP;

            var overflowLevel = 0;
            while (RunsTillCA50.XP_TO_LVL_UP_OVERFLOW_LEVEL <= xp) {
                xp -= RunsTillCA50.XP_TO_LVL_UP_OVERFLOW_LEVEL;
                ++overflowLevel;
            }

            try {
                @SuppressWarnings("StringConcatenationMissingWhitespace") final var toParse = RunsTillCA50.MAX_LEVEL + overflowLevel + "." + StringUtils.remove(Double.toString(RunsTillCA50.padStart(String.format(Locale.ROOT, "%.0f", xp * 100.0D / RunsTillCA50.XP_TO_LVL_UP_OVERFLOW_LEVEL))), '.');
                return Double.parseDouble(toParse);
            } catch (final NumberFormatException nfe) {
                DarkAddons.modError(nfe);
            }
        }

        return RunsTillCA50.noOverflowXpToLevel(xp);
    }

    private static final double noOverflowXpToLevel(final double xp) {
        var levelWithProgress = 0.0D;
        for (final var level : RunsTillCA50.DungeonLeveling.levels) {
            if (level.cumulativeXpRequiredToGet <= xp) {
                levelWithProgress = level.level;
            } else {
                // level - 1 can only not exist in cumulativeXpMap if level is 1 and the result becomes 0, as level 0 doesn't exist, so we default to progress towards level 1.
                final var xpEarnedTowardsNextLevel = xp - RunsTillCA50.DungeonLeveling.cumulativeXpMap.getOrDefault(level.level - 1, RunsTillCA50.DungeonLeveling.dungeonLevels.get(1));
                final double xpNeededForNextLevel = RunsTillCA50.DungeonLeveling.dungeonLevels.getOrDefault(level.level, RunsTillCA50.DungeonLeveling.dungeonLevels.get(1));

                try {
                    final var toParse = (int) levelWithProgress + "." + StringUtils.remove(Double.toString(RunsTillCA50.padStart(String.format(Locale.ROOT, "%.0f", xpEarnedTowardsNextLevel * 100.0D / xpNeededForNextLevel))), '.');
                    return Double.parseDouble(toParse);
                } catch (final NumberFormatException nfe) {
                    DarkAddons.modError(nfe);
                }
            }
        }
        return levelWithProgress;
    }

    private static final class DungeonLeveling {
        @NotNull
        private static final HashMap<Integer, Integer> dungeonLevels = new HashMap<>(Utils.calculateHashMapCapacity(50));
        @NotNull
        private static final HashMap<Integer, Integer> cumulativeXpMap = new HashMap<>(Utils.calculateHashMapCapacity(50));
        @NotNull
        private static final ArrayList<RunsTillCA50.DungeonLeveling.DungeonLevel> levels = new ArrayList<>(Utils.calculateHashMapCapacity(50));

        private static final class DungeonLevel {
            private final int level;

            private final int cumulativeXpRequiredToGet;

            private DungeonLevel(final int levelIn, final int cumulativeXpRequiredToGetIn) {
                super();

                this.level = levelIn;
                this.cumulativeXpRequiredToGet = cumulativeXpRequiredToGetIn;
            }

            @Override
            public final String toString() {
                return "DungeonLevel{" +
                    "level=" + this.level +
                    ", cumulativeXpRequiredToGet=" + this.cumulativeXpRequiredToGet +
                    '}';
            }
        }

        private static final void init() {
            RunsTillCA50.DungeonLeveling.initDungeonLevelsMap();
            RunsTillCA50.DungeonLeveling.initCumulativeXpMap();

            RunsTillCA50.DungeonLeveling.initLevels();
        }

        private static final void initDungeonLevelsMap() {
            RunsTillCA50.DungeonLeveling.level(1, 50);
            RunsTillCA50.DungeonLeveling.level(2, 75);
            RunsTillCA50.DungeonLeveling.level(3, 110);
            RunsTillCA50.DungeonLeveling.level(4, 160);
            RunsTillCA50.DungeonLeveling.level(5, 230);
            RunsTillCA50.DungeonLeveling.level(6, 330);
            RunsTillCA50.DungeonLeveling.level(7, 470);
            RunsTillCA50.DungeonLeveling.level(8, 670);
            RunsTillCA50.DungeonLeveling.level(9, 950);
            RunsTillCA50.DungeonLeveling.level(10, 1_340);
            RunsTillCA50.DungeonLeveling.level(11, 1_890);
            RunsTillCA50.DungeonLeveling.level(12, 2_665);
            RunsTillCA50.DungeonLeveling.level(13, 3_760);
            RunsTillCA50.DungeonLeveling.level(14, 5_260);
            RunsTillCA50.DungeonLeveling.level(15, 7_380);
            RunsTillCA50.DungeonLeveling.level(16, 10_300);
            RunsTillCA50.DungeonLeveling.level(17, 14_400);
            RunsTillCA50.DungeonLeveling.level(18, 20_000);
            RunsTillCA50.DungeonLeveling.level(19, 27_600);
            RunsTillCA50.DungeonLeveling.level(20, 38_000);
            RunsTillCA50.DungeonLeveling.level(21, 52_500);
            RunsTillCA50.DungeonLeveling.level(22, 71_500);
            RunsTillCA50.DungeonLeveling.level(23, 97_000);
            RunsTillCA50.DungeonLeveling.level(24, 132_000);
            RunsTillCA50.DungeonLeveling.level(25, 180_000);

            RunsTillCA50.DungeonLeveling.initDungeonLevelsMap0();
        }

        private static final void initDungeonLevelsMap0() {
            RunsTillCA50.DungeonLeveling.level(26, 243_000);
            RunsTillCA50.DungeonLeveling.level(27, 328_000);
            RunsTillCA50.DungeonLeveling.level(28, 445_000);
            RunsTillCA50.DungeonLeveling.level(29, 600_000);
            RunsTillCA50.DungeonLeveling.level(30, 800_000);
            RunsTillCA50.DungeonLeveling.level(31, 1_065_000);
            RunsTillCA50.DungeonLeveling.level(32, 1_410_000);
            RunsTillCA50.DungeonLeveling.level(33, 1_900_000);
            RunsTillCA50.DungeonLeveling.level(34, 2_500_000);
            RunsTillCA50.DungeonLeveling.level(35, 3_300_000);
            RunsTillCA50.DungeonLeveling.level(36, 4_300_000);
            RunsTillCA50.DungeonLeveling.level(37, 5_600_000);
            RunsTillCA50.DungeonLeveling.level(38, 7_200_000);
            RunsTillCA50.DungeonLeveling.level(39, 9_200_000);
            RunsTillCA50.DungeonLeveling.level(40, 12_000_000);
            RunsTillCA50.DungeonLeveling.level(41, 15_000_000);
            RunsTillCA50.DungeonLeveling.level(42, 19_000_000);
            RunsTillCA50.DungeonLeveling.level(43, 24_000_000);
            RunsTillCA50.DungeonLeveling.level(44, 30_000_000);
            RunsTillCA50.DungeonLeveling.level(45, 38_000_000);
            RunsTillCA50.DungeonLeveling.level(46, 48_000_000);
            RunsTillCA50.DungeonLeveling.level(47, 60_000_000);
            RunsTillCA50.DungeonLeveling.level(48, 75_000_000);
            RunsTillCA50.DungeonLeveling.level(49, 93_000_000);
            RunsTillCA50.DungeonLeveling.level(50, 116_250_000);
        }

        private static final void initCumulativeXpMap() {
            //noinspection StreamToLoop
            RunsTillCA50.DungeonLeveling.dungeonLevels.forEach((@NotNull final Integer key, @NotNull final Integer value) -> {
                int xp = value;

                for (final var innerLevel : RunsTillCA50.DungeonLeveling.dungeonLevels.entrySet()) {
                    if (innerLevel.getKey() < key) {
                        xp += innerLevel.getValue();
                    } else {
                        break;
                    }
                }

                RunsTillCA50.DungeonLeveling.cumulativeXpMap.put(key, xp);
            });
        }

        private static final void initLevels() {
            //noinspection StreamToLoop
            RunsTillCA50.DungeonLeveling.cumulativeXpMap.forEach((@NotNull final Integer key, @NotNull final Integer value) -> RunsTillCA50.DungeonLeveling.levels.add(new RunsTillCA50.DungeonLeveling.DungeonLevel(key, value)));
        }

        private static final void level(final int level, final int xpRequired) {
            RunsTillCA50.DungeonLeveling.dungeonLevels.put(level, xpRequired);
        }

        /**
         * Private constructor since this class only contains static members.
         * <p>
         * Always throws {@link UnsupportedOperationException} (for when
         * constructed via reflection).
         */
        private DungeonLeveling() {
            super();

            throw Utils.staticClassException();
        }
    }

    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    static final boolean isCA50(@NotNull final EnumMap<DungeonListener.DungeonClass, Double> xpMap) {
        for (final var entry : xpMap.entrySet()) {
            if (RunsTillCA50.MAX_LEVEL_XP > entry.getValue()) {
                return false;
            }
        }

        return true;
    }

    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    @NotNull
    private static final DungeonListener.DungeonClass findLowestXPClass(@NotNull final EnumMap<DungeonListener.DungeonClass, Double> xpMap) {
        return Collections.min(xpMap.entrySet(), Map.Entry.comparingByValue()).getKey();
    }

    static final class ProgramResult {
        final int totalRuns;

        @NotNull
        final EnumMap<DungeonListener.DungeonClass, Integer> runsAsClass;
        @NotNull
        private final EnumMap<DungeonListener.DungeonClass, Double> finishXp;

        private final double finishCataXp;

        private ProgramResult(final int totalRunsIn, @NotNull final EnumMap<DungeonListener.DungeonClass, Integer> runsAsClassIn, @NotNull final EnumMap<DungeonListener.DungeonClass, Double> finishXpIn, final double finishCataXpIn) {
            super();

            this.totalRuns = totalRunsIn;

            this.runsAsClass = runsAsClassIn;
            this.finishXp = finishXpIn;

            this.finishCataXp = finishCataXpIn;
        }

        @Override
        public final String toString() {
            return "ProgramResult{" +
                "totalRuns=" + this.totalRuns +
                ", runsAsClass=" + this.runsAsClass +
                ", finishXp=" + this.finishXp +
                ", finishCataXp=" + this.finishCataXp +
                '}';
        }
    }

    @NotNull
    static final RunsTillCA50.ProgramResult simulateAllRuns(@NotNull final EnumMap<DungeonListener.DungeonClass, Double> xpMap,
                                                            @NotNull final EnumMap<DungeonListener.DungeonClass, Double> xpMapWithoutSharedXp,
                                                            final double startingCataXp,
                                                            final boolean m7,
                                                            final boolean derpy,
                                                            @NotNull final RunsTillCA50.Mode mode) {
        var totalRuns = 0;

        final var runsAsClass = new EnumMap<DungeonListener.DungeonClass, Integer>(DungeonListener.DungeonClass.class);
        var cataXp = startingCataXp;

        while (!RunsTillCA50.isCA50(xpMap)) {
            ++totalRuns;
            cataXp += (m7 ? RunsTillCA50.M7_CATA_XP : RunsTillCA50.M6_CATA_XP) * (derpy ? RunsTillCA50.DERPY_MULTIPLIER : 1.0D);

            final var selectedClass = switch (mode) {
                case OPTIMAL -> RunsTillCA50.findLowestXPClass(xpMap);
                case EARLY ->
                    xpMap.entrySet().parallelStream().filter((@NotNull final Map.Entry<DungeonListener.DungeonClass, Double> entry) -> RunsTillCA50.MAX_LEVEL_XP > entry.getValue()).max(Comparator.comparingDouble(Map.Entry::getValue)).orElseThrow(() -> new IllegalStateException("could not find a class that is not maxed and has the highest XP in a set of classes that are not maxed")).getKey();
                case SKULL -> Collections.max(xpMap.entrySet(), Map.Entry.comparingByValue()).getKey();
            };

            runsAsClass.merge(selectedClass, 1, Integer::sum);

            RunsTillCA50.simulateSingularRun(xpMap, selectedClass, m7, derpy);

            final var xpGained = RunsTillCA50.xpGained(true, m7, derpy);
            xpMapWithoutSharedXp.merge(selectedClass, xpGained, Double::sum);
        }

        return new RunsTillCA50.ProgramResult(totalRuns, runsAsClass, xpMapWithoutSharedXp, cataXp);
    }

    private static final void simulateSingularRun(@SuppressWarnings("BoundedWildcard") @NotNull final EnumMap<DungeonListener.DungeonClass, Double> xpMap, @NotNull final DungeonListener.DungeonClass playingClass, final boolean m7, final boolean derpy) {
        for (final var dungeonClass : DungeonListener.DungeonClass.values()) {
            xpMap.merge(dungeonClass, RunsTillCA50.xpGained(dungeonClass == playingClass, m7, derpy), Double::sum);
        }
    }

    private static final double xpGained(final boolean playingClass, final boolean m7, final boolean derpy) {
        return (m7 ? playingClass ? RunsTillCA50.M7_CLASS_XP : RunsTillCA50.M7_SHARED_XP : playingClass ? RunsTillCA50.M6_CLASS_XP : RunsTillCA50.M6_SHARED_XP) * (derpy ? RunsTillCA50.DERPY_MULTIPLIER : 1.0D);
    }
}
