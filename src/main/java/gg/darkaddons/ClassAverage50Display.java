package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Locale;

final class ClassAverage50Display extends GuiElement {
    @NotNull
    private static final ArrayList<String> linesToRender = new ArrayList<>(11);
    private static int linesToRenderSize;

    private static int width;
    private static int height;

    @Nullable
    private static ClassAverage50Display.DungeonFloor lastDoneFloor;

    @Nullable
    private static DungeonListener.DungeonClass lastPlayedClass;

    private static double healerExperience;

    private static double mageExperience;

    private static double berserkerExperience;

    private static double archerExperience;

    private static double tankExperience;

    private static final double parseDouble(@NotNull final String toParse) {
        try {
            return Double.parseDouble(toParse);
        } catch (final NumberFormatException ignored) {
            return 0.0D;
        }
    }

    private static final void parseMessage(@NotNull String message) {
        message = Utils.removeControlCodes(message).trim();
        var updateNeeded = false;

        if (message.startsWith("☠ Defeated Sadan")) {
            ClassAverage50Display.lastDoneFloor = ClassAverage50Display.DungeonFloor.M6;

            updateNeeded = true;
        } else if (message.startsWith("☠ Defeated Maxor")) {
            ClassAverage50Display.lastDoneFloor = ClassAverage50Display.DungeonFloor.M7;

            updateNeeded = true;
        } else if (!message.isEmpty() && '+' == message.charAt(0) && message.contains(" Experience")) {
            message = StringUtils.remove(StringUtils.remove(StringUtils.remove(StringUtils.remove(StringUtils.remove(message, ','), '+'), ' '), "Experience"), "(TeamBonus)");

            if (message.contains("Healer")) {
                message = StringUtils.remove(message, "Healer");
                ClassAverage50Display.healerExperience += ClassAverage50Display.parseDouble(message);

                updateNeeded = true;
            } else if (message.contains("Mage")) {
                message = StringUtils.remove(message, "Mage");
                ClassAverage50Display.mageExperience += ClassAverage50Display.parseDouble(message);

                updateNeeded = true;
            } else if (message.contains("Berserk")) {
                message = StringUtils.remove(message, "Berserk");
                ClassAverage50Display.berserkerExperience += ClassAverage50Display.parseDouble(message);

                updateNeeded = true;
            } else if (message.contains("Archer")) {
                message = StringUtils.remove(message, "Archer");
                ClassAverage50Display.archerExperience += ClassAverage50Display.parseDouble(message);

                updateNeeded = true;
            } else if (message.contains("Tank")) {
                message = StringUtils.remove(message, "Tank");
                ClassAverage50Display.tankExperience += ClassAverage50Display.parseDouble(message);

                updateNeeded = true;
            }
        }

        if (updateNeeded) {
            ClassAverage50Display.fastSyncClassXP();
        }
    }

    static final void doCheckMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("class_average_L_display_check_message");

        if (Config.isClassAverage50Display() && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            ClassAverage50Display.parseMessage(event.message.getUnformattedText());
        }

        McProfilerHelper.endSection();
    }

    @NotNull
    private static final String findLongestLine() {
        var longestLine = "";
        var longestLength = 0;

        for (final var line : ClassAverage50Display.linesToRender) {
            final var len = line.length();

            if (len > longestLength) {
                longestLine = line;
                longestLength = len;
            }
        }

        return longestLine;
    }

    private enum DungeonFloor {
        M7, M6;

        private DungeonFloor() {

        }
    }

    @NotNull
    private static final ClassAverage50Display.DungeonFloor defaultIfNull(@Nullable final ClassAverage50Display.DungeonFloor floor, @NotNull final ClassAverage50Display.DungeonFloor def) {
        return null == floor ? def : floor;
    }

    private static final int defaultIfNull(@Nullable final Integer i, final int def) {
        return null == i ? def : i;
    }

    @NotNull
    private static final ClassAverage50Display.DungeonFloor getDungeonFloor() {
        final var floorSelection = Config.getClassAverage50DisplayFloor();

        return switch (floorSelection) {
            case 0 -> ClassAverage50Display.DungeonFloor.M7;
            case 1 -> ClassAverage50Display.DungeonFloor.M6;
            case 2 ->
                ClassAverage50Display.defaultIfNull(ClassAverage50Display.lastDoneFloor, ClassAverage50Display.DungeonFloor.M7);
            default ->
                throw new UnsupportedOperationException("unsupported floor selection in config: " + floorSelection);
        };
    }

    private static final void syncToDisk() {
        TinyConfig.setDouble("healerExperience", ClassAverage50Display.healerExperience);
        TinyConfig.setDouble("mageExperience", ClassAverage50Display.mageExperience);
        TinyConfig.setDouble("berserkerExperience", ClassAverage50Display.berserkerExperience);
        TinyConfig.setDouble("archerExperience", ClassAverage50Display.archerExperience);
        TinyConfig.setDouble("tankExperience", ClassAverage50Display.tankExperience);

        final var lastDoneFloorLocal = ClassAverage50Display.lastDoneFloor;
        if (null != lastDoneFloorLocal) {
            TinyConfig.setString("lastDoneFloor", lastDoneFloorLocal.name());
        }
    }

    @Nullable
    private static final RunsTillCA50.PlayerDataHolder syncFromDisk() {
        final var healerXp = TinyConfig.getDouble("healerExperience");
        final var mageXp = TinyConfig.getDouble("mageExperience");
        final var berserkerXp = TinyConfig.getDouble("berserkerExperience");
        final var archerXp = TinyConfig.getDouble("archerExperience");
        final var tankXp = TinyConfig.getDouble("tankExperience");

        return null == healerXp || null == mageXp || null == berserkerXp || null == archerXp || null == tankXp ? null : new RunsTillCA50.PlayerDataHolder(healerXp, mageXp, berserkerXp, archerXp, tankXp);
    }

    static final void syncClassXP() {
        // When the method is called from FMLLoadCompleteEvent, Minecraft.getMinecraft().thePlayer returns null, so we need to await it being not null here.
        Utils.awaitCondition(() -> null != Minecraft.getMinecraft().thePlayer, () -> RunsTillCA50.apiFetcher.execute(() -> {
            final var cachedData = RunsTillCA50.lastData;

            final var lastDoneFloorFromDisk = TinyConfig.getString("lastDoneFloor");

            if (null != lastDoneFloorFromDisk && null == ClassAverage50Display.lastDoneFloor) {
                ClassAverage50Display.lastDoneFloor = ClassAverage50Display.DungeonFloor.valueOf(lastDoneFloorFromDisk);
            }

            final var playerData = null == cachedData ? RunsTillCA50.extractDataFor(Minecraft.getMinecraft().thePlayer.getUniqueID(), ClassAverage50Display.DungeonFloor.M7 == ClassAverage50Display.getDungeonFloor(), false) : cachedData;

            RunsTillCA50.lastData = null;

            // We will get thread-safety issues if we continue on the api fetcher thread; this runs the following on the next tick in client thread to avoid any issues.
            DarkAddons.runOnceInNextTick("class_average_L_display_sync_class_xp", () -> {
                var mutablePlayerData = playerData;

                if (null == mutablePlayerData) {
                    mutablePlayerData = ClassAverage50Display.syncFromDisk();

                    if (null == mutablePlayerData) {
                        DarkAddons.queueWarning("Could not sync Class Experiences; Class Average 50 Display HUD will show the runs needed from zero class experiences. If you see this after game start-up or after enabling the Config option; run /darkaddon rtca to see the root cause, as the automatic sync only prints this warning and can't know the root cause.");

                        ClassAverage50Display.fastSyncClassXP();
                        return;
                    }

                    DarkAddons.queueWarning("Could not sync Class Experiences; using fallback data fetched earlier. If anything is wrong, type /darkaddon rtca to try fetching again.");
                }

                ClassAverage50Display.healerExperience = mutablePlayerData.healerXp;
                ClassAverage50Display.mageExperience = mutablePlayerData.mageXp;
                ClassAverage50Display.berserkerExperience = mutablePlayerData.berserkerXp;
                ClassAverage50Display.archerExperience = mutablePlayerData.archerXp;
                ClassAverage50Display.tankExperience = mutablePlayerData.tankXp;

                ClassAverage50Display.fastSyncClassXP();

                ClassAverage50Display.syncToDisk();
            });
        }));
    }

    private static final void fastSyncClassXP() {
        ClassAverage50Display.fastSyncClassXP(ClassAverage50Display.healerExperience, ClassAverage50Display.mageExperience, ClassAverage50Display.berserkerExperience, ClassAverage50Display.archerExperience, ClassAverage50Display.tankExperience);
    }

    private static final void fastSyncClassXP(final double healerXp, final double mageXp, final double berserkerXp, final double archerXp, final double tankXp) {
        final var floor = ClassAverage50Display.getDungeonFloor();

        final var xpMap = RunsTillCA50.generateXpMap(healerXp, mageXp, berserkerXp, archerXp, tankXp);
        final var originalXpMap = new EnumMap<>(xpMap);

        ClassAverage50Display.linesToRender.clear();

        if (RunsTillCA50.isCA50(xpMap)) {
            ClassAverage50Display.linesToRender.add("§aAlready CA50");
            ClassAverage50Display.updateWidthHeightSize();
            return;
        }

        ClassAverage50Display.buildHudLines(xpMap, floor, originalXpMap);
        ClassAverage50Display.updateWidthHeightSize();
    }

    private static final void header() {
        if (0 < Config.getClassAverage50DisplayCompactness()) {
            return;
        }
        ClassAverage50Display.linesToRender.add("§bDarkAddons Class Average 50 Display");
        ClassAverage50Display.linesToRender.add("");
    }

    private static final void progress(final double totalExperiences, final double totalFinishExperiences, final int totalRuns) {
        ClassAverage50Display.linesToRender.add("§eOverall Progress: %" + String.format(Locale.ROOT, "%.2f", totalExperiences / totalFinishExperiences * 100.0D) + " - Total Runs Left: " + totalRuns);
        ClassAverage50Display.linesToRender.add("");
    }

    @Nullable
    private static final DungeonListener.DungeonClass findClass(@NotNull final EnumMap<DungeonListener.DungeonClass, Integer> classes) {
        final var self = Minecraft.getMinecraft().thePlayer;
        for (final var teammate : DungeonListener.getTeam()) {
            //noinspection ObjectEquality
            if (self == teammate.getPlayer()) {
                final var dungeonClass = teammate.getDungeonClass();
                if (null != dungeonClass) {
                    return ClassAverage50Display.lastPlayedClass = dungeonClass;
                }
            }
        }

        return null == ClassAverage50Display.lastPlayedClass ? ClassAverage50Display.findMinRunsLeftClass(classes) : ClassAverage50Display.lastPlayedClass;
    }

    @Nullable
    private static final DungeonListener.DungeonClass findMinRunsLeftClass(@SuppressWarnings("CollectionDeclaredAsConcreteClass") @NotNull final EnumMap<DungeonListener.DungeonClass, Integer> classes) {
        final var entrySet = classes.entrySet();

        DungeonListener.DungeonClass minRunsLeftClass = null;
        var minRunsLeft = Integer.MAX_VALUE;

        for (final var entry : entrySet) {
            final var dungeonClass = entry.getKey();
            final int runsLeft = entry.getValue();

            if (0 < runsLeft && runsLeft < minRunsLeft) {
                minRunsLeft = runsLeft;
                minRunsLeftClass = dungeonClass;
            }
        }

        return minRunsLeftClass;
    }

    @NotNull
    private static final EnumMap<DungeonListener.DungeonClass, Integer> createRunsMap(final int healerRuns, final int mageRuns, final int berserkRuns, final int archerRuns, final int tankRuns) {
        return RunsTillCA50.generateMap(healerRuns, mageRuns, berserkRuns, archerRuns, tankRuns);
    }

    private static final void classes(@NotNull final RunsTillCA50.ProgramResult result, @NotNull final String floorName) {
        final var healerRuns = ClassAverage50Display.defaultIfNull(result.runsAsClass.get(DungeonListener.DungeonClass.HEALER), 0);
        final var mageRuns = ClassAverage50Display.defaultIfNull(result.runsAsClass.get(DungeonListener.DungeonClass.MAGE), 0);
        final var berserkRuns = ClassAverage50Display.defaultIfNull(result.runsAsClass.get(DungeonListener.DungeonClass.BERSERK), 0);
        final var archerRuns = ClassAverage50Display.defaultIfNull(result.runsAsClass.get(DungeonListener.DungeonClass.ARCHER), 0);
        final var tankRuns = ClassAverage50Display.defaultIfNull(result.runsAsClass.get(DungeonListener.DungeonClass.TANK), 0);

        final var classes = ClassAverage50Display.createRunsMap(healerRuns, mageRuns, berserkRuns, archerRuns, tankRuns);

        final var preferred = ClassAverage50Display.findClass(classes);
        final var compactness = Config.getClassAverage50DisplayCompactness();

        ClassAverage50Display.classes0(healerRuns, mageRuns, berserkRuns, archerRuns, tankRuns, preferred, compactness, floorName);
    }

    @NotNull
    private static final String getDerpyText() {
        return DarkAddons.isDerpy() ? "Derpy " : "";
    }

    private static final void classes0(final int healerRuns, final int mageRuns, final int berserkRuns, final int archerRuns, final int tankRuns, @Nullable final DungeonListener.DungeonClass preferred, final int compactness, @NotNull final String floorName) {
        final var showZeroRuns = 1 >= compactness;
        final var showNonPreferred = 2 >= compactness;

        final var hud = ClassAverage50Display.linesToRender;

        final var suffix = ' ' + ClassAverage50Display.getDerpyText() + floorName + " Runs";

        if ((0 < healerRuns || showZeroRuns) && (showNonPreferred || DungeonListener.DungeonClass.HEALER == preferred)) {
            hud.add("§a❤ Healer: " + healerRuns + suffix);
        }

        if ((0 < mageRuns || showZeroRuns) && (showNonPreferred || DungeonListener.DungeonClass.MAGE == preferred)) {
            hud.add("§b✎ Mage: " + mageRuns + suffix);
        }

        if ((0 < berserkRuns || showZeroRuns) && (showNonPreferred || DungeonListener.DungeonClass.BERSERK == preferred)) {
            hud.add("§c⚔ Berserk: " + berserkRuns + suffix);
        }

        if ((0 < archerRuns || showZeroRuns) && (showNonPreferred || DungeonListener.DungeonClass.ARCHER == preferred)) {
            hud.add("§6➶ Archer: " + archerRuns + suffix);
        }

        if ((0 < tankRuns || showZeroRuns) && (showNonPreferred || DungeonListener.DungeonClass.TANK == preferred)) {
            hud.add("§7❈ Tank: " + tankRuns + suffix);
        }
    }

    private static final void footer() {
        if (0 < Config.getClassAverage50DisplayCompactness()) {
            return;
        }
        ClassAverage50Display.linesToRender.add("");
        ClassAverage50Display.linesToRender.add("§dFor more details or to sync class XP, do /darkaddon rtca");
    }

    @NotNull
    private static final RunsTillCA50.Mode getMode() {
        final var mode = Config.getClassAverage50DisplayMode();

        return switch (mode) {
            case 0 -> RunsTillCA50.Mode.OPTIMAL;
            case 1 -> RunsTillCA50.Mode.EARLY;
            case 2 -> RunsTillCA50.Mode.SKULL;
            default -> throw new IllegalStateException("unsupported mode index: " + mode);
        };
    }

    private static final void buildHudLines(@NotNull final EnumMap<DungeonListener.DungeonClass, Double> xpMap, @NotNull final ClassAverage50Display.DungeonFloor floor, @NotNull final EnumMap<DungeonListener.DungeonClass, Double> originalXpMap) {
        ClassAverage50Display.header();

        final var result = RunsTillCA50.simulateAllRuns(xpMap, new EnumMap<>(xpMap), 0.0D, ClassAverage50Display.DungeonFloor.M7 == floor, DarkAddons.isDerpy(), ClassAverage50Display.getMode());
        final var floorName = floor.name();

        final var totalExperiences = RunsTillCA50.getTotalExperiencesNoOverflow(originalXpMap);
        final var totalFinishExperiences = RunsTillCA50.getTotalExperiencesNoOverflow(xpMap);

        ClassAverage50Display.progress(totalExperiences, totalFinishExperiences, result.totalRuns);
        ClassAverage50Display.classes(result, floorName);

        ClassAverage50Display.footer();
    }

    private static final void updateWidthHeightSize() {
        ClassAverage50Display.linesToRenderSize = ClassAverage50Display.linesToRender.size();

        ClassAverage50Display.width = GuiElement.getTextWidth(ClassAverage50Display.findLongestLine());
        ClassAverage50Display.height = GuiElement.getFontHeight() * ClassAverage50Display.linesToRenderSize;
    }

    ClassAverage50Display() {
        super("Class Average 50 Display");

        DarkAddons.addShutdownTask(ClassAverage50Display::syncToDisk);
    }

    private static final boolean shouldRenderWithVisibility(final int visibility) {
        return switch (visibility) {
            case 0 -> DarkAddons.isInDungeons();
            case 1 -> DarkAddons.isInDungeons() && -1L != DungeonTimer.getBossClearTime();
            case 2 ->
                DarkAddons.isInDungeons() && -1L != DungeonTimer.getBossClearTime() || DarkAddons.isPlayerInDungeonHub();
            case 3 -> DarkAddons.isInDungeons() || DarkAddons.isPlayerInDungeonHub();
            case 4 -> DarkAddons.isInSkyblock();
            case 5 -> true;
            default ->
                throw new UnsupportedOperationException("unsupported visibility selection in config: " + visibility);
        };
    }

    @Override
    final void render(final boolean demo) {
        if (demo || this.isEnabled() && !DarkAddons.isInLocationEditingGui()) {
            final var visibility = Config.getClassAverage50DisplayVisibility();
            final var shouldRender = ClassAverage50Display.shouldRenderWithVisibility(visibility);

            if (demo || shouldRender) {
                final var leftAlign = this.shouldLeftAlign();
                final var xPos = leftAlign ? 0.0F : this.getWidth(demo);

                final var shadow = switch (Config.getClassAverage50DisplayShadow()) {
                    case 1 -> SmartFontRenderer.TextShadow.NORMAL;
                    case 2 -> SmartFontRenderer.TextShadow.OUTLINE;
                    default -> SmartFontRenderer.TextShadow.NONE;
                };

                final var fontHeight = GuiElement.getFontHeight();

                final var length = ClassAverage50Display.linesToRenderSize;

                for (var i = 0; i < length; ++i) {
                    GuiElement.drawString(
                        ClassAverage50Display.linesToRender.get(i),
                        xPos,
                        i * fontHeight,
                        leftAlign,
                        shadow
                    );
                }
            }
        }
    }

    @Override
    final boolean isEnabled() {
        return Config.isClassAverage50Display();
    }

    @Override
    final int getHeight() {
        return ClassAverage50Display.height;
    }

    @Override
    final int getWidth(final boolean demo) {
        return ClassAverage50Display.width;
    }
}
