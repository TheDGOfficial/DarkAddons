package gg.darkaddons;

import gg.darkaddons.mixins.IMixinGuiPlayerTabOverlay;
import net.minecraft.client.Minecraft;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Locale;
import java.util.function.BooleanSupplier;

final class BlessingDisplay extends GuiElement {
    private static final int BLESSING_UPDATE_INTERVAL = 20;

    private static final int @NotNull [] blessings = new int[BlessingDisplay.BlessingType.getValuesLength()];

    private static boolean needBlessingInfo;

    private static final boolean needBlessingInfo() {
        return Config.isSendDetailedBlessingsMessage() && BlessingDisplay.needBlessingInfo;
    }

    private static final void clearBlessings() {
        Utils.fillIntArray(BlessingDisplay.blessings, -1);
    }

    private static final void updateBlessingsFromTab() {
        if (!Config.isBlessingHud() && !BlessingDisplay.needBlessingInfo() || !DarkAddons.isInDungeons()) {
            return;
        }

        final var footer = ((IMixinGuiPlayerTabOverlay) Minecraft.getMinecraft().ingameGUI.getTabList()).getFooter();
        if (null == footer) {
            return;
        }

        BlessingDisplay.updateBlessings(footer.getUnformattedText());
    }

    static {
        BlessingDisplay.clearBlessings();

        DarkAddons.registerTickTask("update_blessings_tab", BlessingDisplay.BLESSING_UPDATE_INTERVAL, true, BlessingDisplay::updateBlessingsFromTab);
    }

    private static final double getBaseDamageBonusFromBlessingOfStone() {
        // Source: https://wiki.hypixel.net/Blessings
        // Assume F3 or an above floor cause this for M7.
        // Assume maxed wither essence blessing perk
        // because you are throwing if you don't have it.
        final var blessingLevel = BlessingDisplay.getBlessingOrDefault(BlessingDisplay.BlessingType.STONE, 0);
        final var forbiddenBlessingMultiplier = 1.1D;
        final var fakerAttributeMultiplier = 1.1D;
        final var paulMultiplier = DarkAddons.isPaulMoreEffectiveBlessingsActive() ? 1.25D : 1.0D;
        final var floorBonusMultiplier = 1.2D;
        final var blessingOfStoneFlatIncreaseMultiplier = 6.0D;

        return forbiddenBlessingMultiplier * fakerAttributeMultiplier * paulMultiplier * floorBonusMultiplier * (blessingLevel * blessingOfStoneFlatIncreaseMultiplier);
    }

    static final void doCheckMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("blessing_display_check_message");

        if ((Config.isSendDetailedBlessingsMessage() || Config.isSpawningNotification()) && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type) && DarkAddons.isInSkyblock() && DarkAddons.isInDungeons()) {
            final var message = Utils.removeControlCodes(event.message.getUnformattedText());
            if (message.endsWith(" picked the Corrupted Blue Relic!")) {
                M7Features.clearSplit();
                BlessingDisplay.needBlessingInfo = true;

                if (Config.isSpawningNotification()) {
                    DarkAddons.registerTickTask("send_split_message", 40, false, () ->
                        Utils.awaitCondition(() -> !BlessingDisplay.needBlessingInfo, () -> {
                            final var power = BlessingDisplay.getBlessingOrDefault(BlessingDisplay.BlessingType.POWER, 0);
                            final var time = BlessingDisplay.getBlessingOrDefault(BlessingDisplay.BlessingType.TIME, 0);

                            final var truePower = ((double) power) + ((double) time / 2.0D);
                            final var truePowerTrimUnnecessaryDecimal = (truePower == Math.rint(truePower)) ? Long.toString((long) truePower) : Double.toString(truePower);

                            DarkAddons.queueUserSentMessageOrCommand("/pc Power: " + truePower + " || Split on all drags!");
                        })
                    );
                }

                if (Config.isSendDetailedBlessingsMessage()) {
                    DarkAddons.registerTickTask("send_detailed_blessings_message", 46, false, () ->
                        Utils.awaitCondition(() -> !BlessingDisplay.needBlessingInfo, () -> {
                            final var power = BlessingDisplay.getBlessingOrDefault(BlessingDisplay.BlessingType.POWER, 0);
                            final var time = BlessingDisplay.getBlessingOrDefault(BlessingDisplay.BlessingType.TIME, 0);
                            final var wisdom = BlessingDisplay.getBlessingOrDefault(BlessingDisplay.BlessingType.WISDOM, 0);
                            final var baseDamageBonus = BlessingDisplay.getBaseDamageBonusFromBlessingOfStone();

                            DarkAddons.queueUserSentMessageOrCommand("/pc Detailed Blessings: Power " + power + (0 == time ? "" : " - Time " + time) + " - Wisdom " + wisdom + " - Base Weapon Damage Bonus from Stone Blessing: " + String.format("%.2f", baseDamageBonus));
                        })
                    );
                }
            }
        }

        McProfilerHelper.endSection();
    }

    @SuppressWarnings("FieldNotUsedInToString")
    private enum BlessingType {
        POWER(14, 19, 24, 29, Config::isPowerBlessing),
        TIME(5, 5, 5, 5, Config::isTimeBlessing),
        LIFE(16, 21, 26, 32, Config::isLifeBlessing),
        STONE(7, 9, 12, 14, Config::isStoneBlessing),
        WISDOM(7, 9, 12, 14, Config::isWisdomBlessing);

        @NotNull
        private static final BlessingDisplay.BlessingType[] values = BlessingDisplay.BlessingType.values();
        private static final int VALUES_LENGTH = BlessingDisplay.BlessingType.values.length;

        @NotNull
        private static final String BLESSING_OF = "Blessing of ";

        private final int threshold1;
        private final int threshold2;
        private final int threshold3;
        private final int threshold4;

        @NotNull
        private final String enumName = this.name();
        private final int enumOrdinal = this.ordinal();

        @NotNull
        private final String prettyName = this.generatePrettyName();
        @NotNull
        private final String prettyNamePlusSpace = this.prettyName + ' ';

        @NotNull
        private final String blessingInTabPrefix = BlessingDisplay.BlessingType.BLESSING_OF + this.prettyName;

        @NotNull
        private final BooleanSupplier isEnabledChecker;

        private BlessingType(final int firstThreshold, final int secondThreshold, final int thirdThreshold, final int fourthThreshold, @NotNull final BooleanSupplier isEnabled) {
            this.threshold1 = firstThreshold;
            this.threshold2 = secondThreshold;
            this.threshold3 = thirdThreshold;
            this.threshold4 = fourthThreshold;

            this.isEnabledChecker = isEnabled;
        }

        @NotNull
        private static final BlessingDisplay.BlessingType[] getValues() {
            //noinspection AssignmentOrReturnOfFieldWithMutableType
            return BlessingDisplay.BlessingType.values;
        }

        private static final int getValuesLength() {
            return BlessingDisplay.BlessingType.VALUES_LENGTH;
        }

        @SuppressWarnings("MethodReturnAlwaysConstant")
        @NotNull
        private static final String getBlessingOf() {
            return BlessingDisplay.BlessingType.BLESSING_OF;
        }

        private final boolean isEnabled() {
            return this.isEnabledChecker.getAsBoolean();
        }

        @NotNull
        private final String generatePrettyName() {
            final var lowerCase = this.enumName.toLowerCase(Locale.ROOT);
            final var firstChar = lowerCase.substring(0, 1);

            return firstChar.toUpperCase(Locale.ROOT) + lowerCase.substring(1);
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        @NotNull
        public final String toString() {
            return this.prettyName;
        }

        private final int getThreshold1() {
            return this.threshold1;
        }

        private final int getThreshold2() {
            return this.threshold2;
        }

        private final int getThreshold3() {
            return this.threshold3;
        }

        private final int getThreshold4() {
            return this.threshold4;
        }

        @NotNull
        private final String getPrettyName() {
            return this.prettyName;
        }

        @NotNull
        private final String getBlessingInTabPrefix() {
            return this.blessingInTabPrefix;
        }
    }

    private static final void updateBlessings(@NotNull final String footer) {
        if (!footer.contains(BlessingDisplay.BlessingType.getBlessingOf())) {
            return;
        }

        for (final var line : footer.split(Utils.UNIX_NEW_LINE)) {
            if (!line.contains(BlessingDisplay.BlessingType.getBlessingOf())) {
                continue;
            }

            for (final var blessingType : BlessingDisplay.BlessingType.getValues()) {
                if ((blessingType.isEnabled() || BlessingDisplay.BlessingType.LIFE != blessingType && BlessingDisplay.needBlessingInfo()) && line.contains(blessingType.getBlessingInTabPrefix())) {
                    final var level = StringUtils.remove(line, BlessingDisplay.BlessingType.BLESSING_OF + blessingType.prettyNamePlusSpace);
                    BlessingDisplay.blessings[blessingType.enumOrdinal] = Utils.fastRomanToInt(level);

                    break;
                }
            }
        }

        if (BlessingDisplay.needBlessingInfo()) {
            BlessingDisplay.needBlessingInfo = false;
        }
    }

    private static final int getBlessingOrDefault(@NotNull final BlessingDisplay.BlessingType blessingType, final int def) {
        final var value = BlessingDisplay.blessings[blessingType.enumOrdinal];

        return -1 == value ? def : value;

    }

    BlessingDisplay() {
        super("Blessing Display");
    }

    @Override
    final void render(final boolean demo) {
        if (demo || this.isEnabled() && DarkAddons.isInSkyblock() && DarkAddons.isInDungeons() && !DarkAddons.isInLocationEditingGui()) {
            final var leftAlign = this.shouldLeftAlign();
            final var xPos = leftAlign ? 0.0F : this.getWidth(demo);

            final var fontHeight = GuiElement.getFontHeight();

            final var white = SmartFontRenderer.CommonColors.WHITE;
            final var red = SmartFontRenderer.CommonColors.RED;
            final var yellow = SmartFontRenderer.CommonColors.YELLOW;
            final var green = SmartFontRenderer.CommonColors.GREEN;
            final var rainbow = SmartFontRenderer.CommonColors.RAINBOW;

            final var hideBlessingWhenZero = Config.isHideBlessingWhenZero();

            var filteredIndex = 0;

            for (final var blessingType : BlessingDisplay.BlessingType.getValues()) {
                if (blessingType.isEnabled()) {
                    final var threshold4 = blessingType.getThreshold4();
                    final var blessingLevel = demo ? threshold4 : BlessingDisplay.getBlessingOrDefault(blessingType, 0);

                    if (hideBlessingWhenZero && 0 == blessingLevel) {
                        continue;
                    }

                    final var threshold = BlessingDisplay.getThreshold(blessingType, threshold4, blessingLevel);

                    /*if (threshold != 4 && blessings[BlessingType.TIME.ordinal()] == 5) {
                        ++threshold
                    }*/

                    final var color = BlessingDisplay.getColorFromThreshold(white, red, yellow, green, rainbow, threshold);
                    final var blessingText = blessingType.prettyNamePlusSpace + blessingLevel;

                    GuiElement.drawString(
                        white == color ? "§c" + blessingText : blessingText,
                        xPos,
                        filteredIndex * fontHeight,
                        color,
                        leftAlign
                    );

                    ++filteredIndex;
                }

            }
        }
    }

    @Override
    final boolean isEnabled() {
        return Config.isBlessingHud();
    }

    @Override
    final int getHeight() {
        return GuiElement.getFontHeight() * BlessingDisplay.getEnabledBlessingCount();
    }

    @Override
    final int getWidth(final boolean demo) {
        return GuiElement.getTextWidth(BlessingDisplay.BlessingType.WISDOM.getPrettyName() + ' ' + (demo ? BlessingDisplay.BlessingType.WISDOM.getThreshold4() : BlessingDisplay.getBlessingOrDefault(BlessingDisplay.BlessingType.WISDOM, BlessingDisplay.BlessingType.WISDOM.getThreshold4())));
    }

    @NotNull
    private static final SmartFontRenderer.CommonColors getColorFromThreshold(@NotNull final SmartFontRenderer.CommonColors white, @NotNull final SmartFontRenderer.CommonColors red, @NotNull final SmartFontRenderer.CommonColors yellow, @NotNull final SmartFontRenderer.CommonColors green, @NotNull final SmartFontRenderer.CommonColors rainbow, final int threshold) {
        return switch (threshold) {
            case 4 -> rainbow;
            case 3 -> green;
            case 2 -> yellow;
            case 1 -> white;
            default -> red;
        };
    }

    private static final int getThreshold(@NotNull final BlessingDisplay.BlessingType blessingType, final int threshold4, final int blessingLevel) {
        if (blessingLevel >= threshold4) {
            return 4;
        }

        if (blessingLevel >= blessingType.getThreshold3()) {
            return 3;
        }

        if (blessingLevel >= blessingType.getThreshold2()) {
            return 2;
        }

        return blessingLevel >= blessingType.getThreshold1() ? 1 : 0;
    }

    private static final int getEnabledBlessingCount() {
        var enabledCount = 0;

        for (final var blessingType : BlessingDisplay.BlessingType.getValues()) {
            if (blessingType.isEnabled()) {
                ++enabledCount;
            }
        }

        return enabledCount;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onWorldUnload(@NotNull final WorldEvent.Unload ignoredEvent) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        if (DarkAddons.shouldProfile()) {
            DarkAddons.handleEvent("blessing_display_clear_blessings", BlessingDisplay::clearBlessings);
        } else {
            BlessingDisplay.clearBlessings();
        }
    }
}
