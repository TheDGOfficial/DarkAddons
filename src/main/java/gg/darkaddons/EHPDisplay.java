package gg.darkaddons;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures;
import gg.skytils.skytilsmod.utils.graphics.SmartFontRenderer;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class EHPDisplay extends GuiElement {
    private static final float DEMO_EHP = 7_200_000.0F;
    private static final float TANK_DMG_MULTIPLIER = 8.2F;
    @Nullable
    private static String lastActionbarText;
    private static float lastKnownDefense = -1.0F;
    private static float lastEhpDiff;
    private static float ehp;

    private static final void updateEhp(@Nullable final String actionbarText) {
        if (Utils.areStringsEqual(EHPDisplay.lastActionbarText, actionbarText)) {
            return; // Prevent unnecessary split/pattern-matching if the action bar is not changed.
        }
        EHPDisplay.lastActionbarText = actionbarText;

        var health = -1.0F;
        var defense = -1.0F;

        if (null != actionbarText) {
            final var splitActionbar = StringUtils.split(actionbarText, "   ");

            // If the action bar is displaying player stats and the defense section is absent,
            // the player's defense is zero.
            if (actionbarText.contains("❤") && !actionbarText.contains("❈") && 2 == splitActionbar.length) {
                defense = 0.0F;
            }

            for (final var section : splitActionbar) {
                if (section.contains("❤")) {
                    final var stripped = Utils.removeControlCodes(section);
                    final var number = StringUtils.substringBefore(stripped, "/");
                    final float parsed = Utils.safeParseIntFast(StringUtils.remove(number, ','));

                    if (!Utils.compareFloatExact(parsed, -1.0F)) {
                        health = parsed;
                    }
                    // (implicit) continue, the next section is probably defense.
                } else {
                    if (section.contains("❈")) {
                        final var stripped = Utils.removeControlCodes(section);
                        final var number = StringUtils.substringBefore(stripped, "❈");
                        final float parsed = Utils.safeParseIntFast(StringUtils.remove(number, ','));

                        if (!Utils.compareFloatExact(parsed, -1.0F)) {
                            defense = parsed;
                        }
                    }
                    // we don't need to process anymore if we couldn't find hp or defense in the first two sections.
                    break; // break, we are done
                }
            }
        }

        if (Utils.compareFloatExact(defense, -1.0F)) {
            if (!Utils.compareFloatExact(EHPDisplay.lastKnownDefense, -1.0F)) {
                defense = EHPDisplay.lastKnownDefense;
            }
        } else {
            if (0.0F != defense) {
                EHPDisplay.lastKnownDefense = defense;
            } else if (0.0F != EHPDisplay.lastKnownDefense) {
                defense = EHPDisplay.lastKnownDefense;
            }
        }

        if (!Utils.compareFloatExact(health, -1.0F) && !Utils.compareFloatExact(defense, -1.0F)) {
            final var newEhp = EHPDisplay.getEhp(health, defense);

            EHPDisplay.lastEhpDiff = newEhp - EHPDisplay.ehp;
            EHPDisplay.ehp = newEhp;
        }
    }

    private static final float getEhp(final float health, final float defense) {
        return health * (1.0F + defense / 100.0F);
    }

    EHPDisplay() {
        super("EHP Display");
    }

    @Override
    final void render(final boolean demo) {
        if (demo || this.isEnabled() && DarkAddons.isInSkyblock() && (Config.isEhpHudOutOfDungeons() || DarkAddons.isInDungeons()) && !DarkAddons.isInLocationEditingGui()) {
            final var leftAlign = this.shouldLeftAlign();
            GuiElement.drawString(
                EHPDisplay.getEhpDisplayText(demo ? EHPDisplay.DEMO_EHP : EHPDisplay.ehp, demo),
                leftAlign ? 0.0F : this.getWidth(demo),
                0.0F,
                leftAlign
            );
        }
    }

    @Override
    final boolean isEnabled() {
        return Config.isEhpHud();
    }

    @Override
    final int getHeight() {
        return GuiElement.getFontHeight();
    }

    @Override
    final int getWidth(final boolean demo) {
        return GuiElement.getTextWidth(EHPDisplay.getEhpDisplayText(demo ? EHPDisplay.DEMO_EHP : EHPDisplay.ehp, demo));
    }

    @NotNull
    private static final String getColor(final float effectiveHealth) {
        final var threshold2 = EHPDisplay.getHighestNonBossMobDmgForCurrentFloor(-1L != DungeonTimer.getBossEntryTime() && EHPDisplay.isPlayerPlayingTankClass());
        final var threshold3 = threshold2 * 2.0F;

        if (effectiveHealth >= threshold3) {
            return "a";
        }

        if (effectiveHealth >= threshold2) {
            return "e";
        }

        final var threshold1 = threshold2 / 1.5F;
        if (effectiveHealth >= threshold1) {
            return "6";
        }

        final var threshold0 = threshold2 / 2.5F;
        return effectiveHealth >= threshold0 ? "c" : "4";
    }

    private static final boolean isPlayerPlayingTankClass() {
        return AdditionalM7Features.isPlayingTank();
    }

    private static final float getHighestNonBossMobDmgForCurrentFloor(final boolean tank) {
        final float dmg;

        if (DarkAddons.isInDungeons()) {
            final var dungeonFloor = DungeonFeatures.INSTANCE.getDungeonFloor();
            final var entranceDmg = 800.0F;

            dmg = null == dungeonFloor ? entranceDmg : EHPDisplay.getHighestNonBossMobDmgForCurrentFloor0(dungeonFloor, entranceDmg);
        } else {
            dmg = 20_000.0F;
        }

        return tank ? dmg * EHPDisplay.TANK_DMG_MULTIPLIER : dmg;
    }

    private static final float getHighestNonBossMobDmgForCurrentFloor0(@NotNull final String dungeonFloor, final float entranceDmg) {
        return switch (dungeonFloor) {
            case "M7", "M6" -> 400_000.0F;
            case "M5", "M4" -> 270_000.0F;
            case "M3" -> 175_000.0F;
            case "M2" -> 108_000.0F;
            case "M1" -> 72_000.0F;
            default -> EHPDisplay.getHighestNonBossMobDmgForCurrentFloor1(dungeonFloor, entranceDmg);
        };
    }

    private static final float getHighestNonBossMobDmgForCurrentFloor1(@NotNull final String dungeonFloor, final float entranceDmg) {
        return switch (dungeonFloor) {
            case "F7" -> 48_000.0F;
            case "F6" -> 8_640.0F;
            case "F5", "F4" -> 6_640.0F;
            case "F3" -> 3_280.0F;
            case "F2" -> 1_800.0F;
            case "F1" -> 1_200.0F;
            default -> entranceDmg;
        };
    }

    @NotNull
    private static final String getEhpDisplayText(final float effectiveHealth, final boolean demo) {
        final String suffix;
        if (!demo && 0.0F != EHPDisplay.lastEhpDiff) {
            suffix = 0.0F < EHPDisplay.lastEhpDiff ? " §a(+" + Utils.formatNumber(EHPDisplay.lastEhpDiff) + ')' : " §c(-" + Utils.formatNumber(Math.abs(EHPDisplay.lastEhpDiff)) + ')';
        } else {
            suffix = "";
        }
        return "§aEHP§f: §" + EHPDisplay.getColor(effectiveHealth) + Utils.formatNumber(effectiveHealth) + suffix;
    }

    static final void doCheckMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("ehp_display_check_message");

        if (MessageType.STATUS_MESSAGE_DISPLAYED_ABOVE_ACTIONBAR.matches(event.type)) {
            if (Config.isEhpHud() && DarkAddons.isInSkyblock() && (Config.isEhpHudOutOfDungeons() || DarkAddons.isInDungeons())) {
                EHPDisplay.updateEhp(event.message.getUnformattedText());
            } else {
                EHPDisplay.lastActionbarText = null; // Prevent memory leak
            }
        }

        McProfilerHelper.endSection();
    }
}
