package gg.darkaddons;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import gg.skytils.skytilsmod.utils.graphics.colors.CommonColors;
import gg.skytils.skytilsmod.listeners.DungeonListener;
import gg.skytils.skytilsmod.utils.DungeonClass;
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
    private static DungeonClass lastPlayedClass;

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
            case 2 -> ClassAverage50Display.defaultIfNull(ClassAverage50Display.lastDoneFloor, ClassAverage50Display.DungeonFloor.M7);
            default ->
                    throw new UnsupportedOperationException("unsupported floor selection in config: " + floorSelection);
        };
    }

    static final void syncClassXP() {
        // When the method is called from FMLLoadCompleteEvent, Minecraft.getMinecraft().thePlayer returns null, so we need to await it being not null here.
        Utils.awaitCondition(() -> null != Minecraft.getMinecraft().thePlayer, () -> RunsTillCA50.apiFetcher.execute(() -> {
            final var cachedData = RunsTillCA50.lastData;
            final var playerData = null == cachedData ? RunsTillCA50.extractDataFor(Minecraft.getMinecraft().thePlayer.getUniqueID(), ClassAverage50Display.DungeonFloor.M7 == ClassAverage50Display.getDungeonFloor(), false) : cachedData;

            RunsTillCA50.lastData = null;

            if (null == playerData) {
                DarkAddons.queueWarning("Could not sync Class Experiences; Class Average 50 Display HUD will show the runs needed from zero class experiences. If you see this after game start-up or after enabling the Config option; run /darkaddon rtca to see the root cause, as the automatic sync only prints this warning and can't know the root cause.");

                ClassAverage50Display.fastSyncClassXP();
                return;
            }

            // We will get thread-safety issues if we continue on the api fetcher thread; this runs the following on the next tick in client thread to avoid any issues.
            DarkAddons.runOnceInNextTick("class_average_L_display_sync_class_xp", () -> {
                ClassAverage50Display.healerExperience = playerData.healerXp;
                ClassAverage50Display.mageExperience = playerData.mageXp;
                ClassAverage50Display.berserkerExperience = playerData.berserkerXp;
                ClassAverage50Display.archerExperience = playerData.archerXp;
                ClassAverage50Display.tankExperience = playerData.tankXp;

                ClassAverage50Display.fastSyncClassXP();
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
    private static final DungeonClass findClass(@NotNull final EnumMap<DungeonClass, Integer> classes) {
        final var self = Minecraft.getMinecraft().thePlayer;
        for (final var teammate : DungeonListener.INSTANCE.getTeam().values()) {
            //noinspection ObjectEquality
            if (self == teammate.getPlayer()) {
                return ClassAverage50Display.lastPlayedClass = teammate.getDungeonClass();
            }
        }

        return null == ClassAverage50Display.lastPlayedClass ? ClassAverage50Display.findMinRunsLeftClass(classes) : ClassAverage50Display.lastPlayedClass;
    }

    @Nullable
    private static final DungeonClass findMinRunsLeftClass(@SuppressWarnings("CollectionDeclaredAsConcreteClass") @NotNull final EnumMap<DungeonClass, Integer> classes) {
        final var entrySet = classes.entrySet();

        DungeonClass minRunsLeftClass = null;
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
    private static final EnumMap<DungeonClass, Integer> createRunsMap(final int healerRuns, final int mageRuns, final int berserkRuns, final int archerRuns, final int tankRuns) {
        final var classes = new EnumMap<DungeonClass, Integer>(DungeonClass.class);

        classes.put(DungeonClass.HEALER, healerRuns);
        classes.put(DungeonClass.MAGE, mageRuns);
        classes.put(DungeonClass.BERSERK, berserkRuns);
        classes.put(DungeonClass.ARCHER, archerRuns);
        classes.put(DungeonClass.TANK, tankRuns);

        return classes;
    }

    private static final void classes(@NotNull final RunsTillCA50.ProgramResult result, @NotNull final String floorName) {
        final var healerRuns = ClassAverage50Display.defaultIfNull(result.runsAsClass.get(RunsTillCA50.DungeonClass.HEALER), 0);
        final var mageRuns = ClassAverage50Display.defaultIfNull(result.runsAsClass.get(RunsTillCA50.DungeonClass.MAGE), 0);
        final var berserkRuns = ClassAverage50Display.defaultIfNull(result.runsAsClass.get(RunsTillCA50.DungeonClass.BERSERK), 0);
        final var archerRuns = ClassAverage50Display.defaultIfNull(result.runsAsClass.get(RunsTillCA50.DungeonClass.ARCHER), 0);
        final var tankRuns = ClassAverage50Display.defaultIfNull(result.runsAsClass.get(RunsTillCA50.DungeonClass.TANK), 0);

        final var classes = ClassAverage50Display.createRunsMap(healerRuns, mageRuns, berserkRuns, archerRuns, tankRuns);

        final var preferred = ClassAverage50Display.findClass(classes);
        final var compactness = Config.getClassAverage50DisplayCompactness();

        ClassAverage50Display.classes0(healerRuns, mageRuns, berserkRuns, archerRuns, tankRuns, preferred, compactness, floorName);
    }

    private static final void classes0(final int healerRuns, final int mageRuns, final int berserkRuns, final int archerRuns, final int tankRuns, @Nullable final DungeonClass preferred, final int compactness, @NotNull final String floorName) {
        if ((0 < healerRuns || 1 >= compactness) && (2 >= compactness || DungeonClass.HEALER == preferred)) {
            ClassAverage50Display.linesToRender.add("§a❤ Healer: " + healerRuns + ' ' + floorName + " Runs");
        }

        if ((0 < mageRuns || 1 >= compactness) && (2 >= compactness || DungeonClass.MAGE == preferred)) {
            ClassAverage50Display.linesToRender.add("§b✎ Mage: " + mageRuns + ' ' + floorName + " Runs");
        }

        if ((0 < berserkRuns || 1 >= compactness) && (2 >= compactness || DungeonClass.BERSERK == preferred)) {
            ClassAverage50Display.linesToRender.add("§c⚔ Berserk: " + berserkRuns + ' ' + floorName + " Runs");
        }

        if ((0 < archerRuns || 1 >= compactness) && (2 >= compactness || DungeonClass.ARCHER == preferred)) {
            ClassAverage50Display.linesToRender.add("§6➶ Archer: " + archerRuns + ' ' + floorName + " Runs");
        }

        if ((0 < tankRuns || 1 >= compactness) && (2 >= compactness || DungeonClass.TANK == preferred)) {
            ClassAverage50Display.linesToRender.add("§7❈ Tank: " + tankRuns + ' ' + floorName + " Runs");
        }
    }

    private static final void footer() {
        if (0 < Config.getClassAverage50DisplayCompactness()) {
            return;
        }
        ClassAverage50Display.linesToRender.add("");
        ClassAverage50Display.linesToRender.add("§dFor more details or to sync class XP, do /darkaddon rtca");
    }

    private static final void buildHudLines(@NotNull final EnumMap<RunsTillCA50.DungeonClass, Double> xpMap, @NotNull final ClassAverage50Display.DungeonFloor floor, @NotNull final EnumMap<RunsTillCA50.DungeonClass, Double> originalXpMap) {
        ClassAverage50Display.header();

        final var result = RunsTillCA50.simulateAllRuns(xpMap, new EnumMap<>(xpMap), 0.0D, ClassAverage50Display.DungeonFloor.M7 == floor, DarkAddons.isDerpy(), RunsTillCA50.Mode.OPTIMAL);
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
    }

    private static final boolean shouldRenderWithVisibility(final int visibility) {
        return switch (visibility) {
            case 0 -> DarkAddons.isInDungeons();
            case 1 -> DarkAddons.isInDungeons() && -1L != DungeonTimer.INSTANCE.getBossClearTime();
            case 2 ->
                    DarkAddons.isInDungeons() && -1L != DungeonTimer.INSTANCE.getBossClearTime() || DarkAddons.isPlayerInDungeonHub();
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
                final var alignment = leftAlign ? SmartFontRenderer.TextAlignment.LEFT_RIGHT : SmartFontRenderer.TextAlignment.RIGHT_LEFT;
                final var xPos = leftAlign ? 0.0F : this.getWidth(demo);

                final var shadow = switch (Config.getClassAverage50DisplayShadow()) {
                    case 1 -> SmartFontRenderer.TextShadow.NORMAL;
                    case 2 -> SmartFontRenderer.TextShadow.OUTLINE;
                    default -> SmartFontRenderer.TextShadow.NONE;
                };

                final var fontHeight = GuiElement.getFontHeight();
                final var color = CommonColors.Companion.getWHITE();

                final var length = ClassAverage50Display.linesToRenderSize;

                for (var i = 0; i < length; ++i) {
                    GuiElement.drawString(
                        ClassAverage50Display.linesToRender.get(i),
                        xPos,
                        i * fontHeight,
                        color,
                        alignment,
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
