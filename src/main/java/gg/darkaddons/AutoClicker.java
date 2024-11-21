package gg.darkaddons;

import gg.darkaddons.mixin.MixinUtils;
import gg.darkaddons.mixins.IMixinMinecraft;
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Items;
import net.minecraft.util.MovingObjectPosition;
import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;

final class AutoClicker {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private AutoClicker() {
        super();

        throw Utils.staticClassException();
    }

    private static boolean shouldLeftClick = true;
    private static boolean shouldRightClick = true;

    static final void resetShouldClick() {
        AutoClicker.shouldLeftClick = true;
        AutoClicker.shouldRightClick = true;
    }

    static final boolean isHoldingASword(@NotNull final Minecraft mc) {
        final var item = Utils.getHeldItem(mc);

        //noinspection ObjectEquality
        return Items.diamond_sword == item || Items.golden_sword == item || Items.iron_sword == item || Items.stone_sword == item || Items.wooden_sword == item;
    }

    private static final boolean isHoldingABow(@NotNull final Minecraft mc) {
        final var item = Utils.getHeldItem(mc);

        //noinspection ObjectEquality
        return Items.bow == item;
    }

    private static final boolean isHoldingTerm(@NotNull final Minecraft mc) {
       return AutoClicker.isHoldingABow(mc) && Utils.isHoldingItemContaining(mc, "Terminator");
    }

    static final boolean isHoldingHype(@NotNull final Minecraft mc) {
        return Utils.isHoldingItemContaining(mc, "Hyperion");
    }

    static final boolean isHoldingTermOrRCM(@NotNull final Minecraft mc) {
        return (AutoClicker.isHoldingTerm(mc)) || (Utils.isHoldingItemContaining(mc, "Astraea") || AutoClicker.isHoldingHype(mc));
    }

    static final boolean isHoldingClaymoreMidasOrGS(@NotNull final Minecraft mc) {
        return AutoClicker.isHoldingASword(mc) && (Utils.isHoldingItemContaining(mc, "Claymore") || Utils.isHoldingItemContaining(mc, "Midas'") || Utils.isHoldingItemContaining(mc, "Giant's Sword"));
    }

    @SuppressWarnings("TypeMayBeWeakened")
    @NotNull
    private static final SecureRandom SECURE_RANDOM = new SecureRandom(); // TODO is SecureRandom needed here?

    private static int leftClickTicks;
    private static int extraLeftClickTicks;

    private static int rightClickTicks;
    private static int extraRightClickTicks;

    private static final boolean canGetLimboed() {
        // Hypixel implemented a check where if you use Terminator AC while looking at a block above 20 >= CPS, you will get limboed
        // with the reasoning of having a bad internet connection.
        final var mc = Minecraft.getMinecraft();
        final var pos = mc.objectMouseOver;

        return null == pos || MovingObjectPosition.MovingObjectType.BLOCK == pos.typeOfHit && !mc.theWorld.isAirBlock(pos.getBlockPos());
    }

    private static final int getCpsLimit(final boolean left) {
        final var configLimit = left ? Config.getLeftClickCpsLimit() : Config.getRightClickCpsLimit();
        // TODO find a way to support custom CPS values
        return switch (configLimit) {
            case 0 -> AutoClicker.canGetLimboed() ? 20 : 45;
            case 1 -> AutoClicker.canGetLimboed() ? 20 : 40;
            case 2 -> AutoClicker.SECURE_RANDOM.nextBoolean() && !AutoClicker.canGetLimboed() ? 40 : 20;
            case 3 -> 20;
            case 4 ->
                // TODO actually add a more robust option of random CPS in a range, i.e., 30-36
                AutoClicker.SECURE_RANDOM.nextBoolean() ? 20 : 10;
            case 5 -> 15;
            case 6 -> 10;
            case 7 -> 5;
            case 8 -> 4;
            case 9 -> 2;
            case 10 -> 1;
            default -> throw new IllegalStateException("unsupported cps limit: " + configLimit);
        };
    }

    private static final int getAutoSalvationCpsLimit() {
        final var configLimit = Config.getAutoSalvationCpsLimit();
        // TODO find a way to support custom CPS values
        return switch (configLimit) {
            case 0 -> 4;
            case 1 -> AutoClicker.SECURE_RANDOM.nextBoolean() ? 4 : 8;
            case 2 -> 8;
            case 3 -> AutoClicker.SECURE_RANDOM.nextBoolean() ? 8 : 10;
            case 4 -> 10;
            case 5 -> AutoClicker.SECURE_RANDOM.nextBoolean() ? 10 : 15;
            case 6 -> 15;
            default -> throw new IllegalStateException("unsupported cps limit: " + configLimit);
        };
    }

    static final boolean isPressedStatic(@NotNull final KeyBinding keyBinding, @NotNull final Runnable leftClick, @NotNull final Runnable rightClick) {
        final var mc = Minecraft.getMinecraft();
        final var settings = mc.gameSettings;

        final var actual = keyBinding.isPressed();

        if (!actual) {
            final var left = settings.keyBindAttack;
            final var right = settings.keyBindUseItem;

            //noinspection ObjectEquality
            if (AutoClicker.shouldLeftClick && left == keyBinding) {
                AutoClicker.shouldLeftClick = false;

                return AutoClicker.handleLeftClick(leftClick, left, right, mc);
            }

            //noinspection ObjectEquality
            if (AutoClicker.shouldRightClick && right == keyBinding) {
                AutoClicker.shouldRightClick = false;

                return AutoClicker.handleRightClick(rightClick, right, mc);
            }
        }

        return actual;
    }

    private static final boolean isInF4orM4() {
        final var floor = DungeonFeatures.INSTANCE.getDungeonFloorNumber();

        return null != floor && 4 == floor;
    }

    private static final boolean isInBoss() {
        return DungeonFeatures.INSTANCE.getHasBossSpawned();
    }

    private static final boolean handleLeftClick(@NotNull final Runnable leftClick, @NotNull final KeyBinding left, @NotNull final KeyBinding right, @NotNull final Minecraft mc) {
        final var newIsPressed = Config.isLeftClickAutoClicker() && left.isKeyDown() && AutoClicker.isHoldingASword(mc);
        if (newIsPressed) {
            ++AutoClicker.leftClickTicks;

            final var limit = AutoClicker.getCpsLimit(true);
            if (20 / limit <= AutoClicker.leftClickTicks) {
                AutoClicker.leftClickTicks = 0;

                final var extraClicks = limit / 20;
                if (1 < extraClicks) {
                    for (var i = 1; i < extraClicks; ++i) {
                        if (!mc.thePlayer.isUsingItem()) {
                            leftClick.run();
                        }
                    }
                }

                if (45 == limit || 15 == limit) {
                    ++AutoClicker.extraLeftClickTicks;
                    if (4 <= AutoClicker.extraLeftClickTicks && !mc.thePlayer.isUsingItem()) {
                        AutoClicker.extraLeftClickTicks = 0;
                        leftClick.run();
                    }
                }

                return true;
            }

            return false;
        }

        if (MixinUtils.isPunchOverride() && MixinUtils.getPunchOverridePrecondition().getAsBoolean()) {
            MixinUtils.setPunchOverride(false);
            return true;
        }

        return Config.isAutoSalvation() && right.isKeyDown() && AutoClicker.isHoldingTerm(mc) && AutoClicker.triggerSalvationKeyPress(leftClick, mc);
    }

    private static final boolean triggerSalvationKeyPress(@NotNull final Runnable leftClick, @NotNull final Minecraft mc) {
        if (Config.isDisableAutoSalvationInThornBoss() && AutoClicker.isInBoss() && AutoClicker.isInF4orM4()) {
            return false;
        }

        ++AutoClicker.leftClickTicks;
        ++AutoClicker.extraLeftClickTicks;

        final var autoSalvationCpsLimit = AutoClicker.getAutoSalvationCpsLimit();

        final boolean shouldLc;
        var shouldExtraLc = false;

        switch (autoSalvationCpsLimit) {
            case 4, 10 -> shouldLc = 20 / autoSalvationCpsLimit <= AutoClicker.leftClickTicks;
            case 8 -> {
                shouldLc = 3 <= AutoClicker.leftClickTicks;
                if (10 <= AutoClicker.extraLeftClickTicks) {
                    shouldExtraLc = true;
                }
            }
            case 15 -> shouldLc = true;
            default -> throw new IllegalStateException("unexpected value: " + autoSalvationCpsLimit);
        }

        final var vanilla = AutoClicker.gonnaDoVanillaCpsInThisTick();

        if (shouldLc && !vanilla) {
            AutoClicker.leftClickTicks = 0;

            return true;
        }

        if (shouldExtraLc && !vanilla && !mc.thePlayer.isUsingItem()) {
            AutoClicker.extraLeftClickTicks = 0;

            leftClick.run();
        }

        return false;
    }

    private static final boolean gonnaDoVanillaCpsInThisTick() {
        final var mc = Minecraft.getMinecraft();
        return mc.gameSettings.keyBindUseItem.isKeyDown() && 0 == ((IMixinMinecraft) mc).getRightClickDelayTimer() && !mc.thePlayer.isUsingItem();
    }

    private static final boolean handleRightClick(@NotNull final Runnable rightClick, @NotNull final KeyBinding right, @NotNull final Minecraft mc) {
        final var newIsPressed = Config.isRightClickAutoClicker() && right.isKeyDown() && AutoClicker.isHoldingTermOrRCM(mc);
        if (newIsPressed) {
            ++AutoClicker.rightClickTicks;

            final var limit = AutoClicker.getCpsLimit(false);
            if (20 / limit <= AutoClicker.rightClickTicks) {
                AutoClicker.rightClickTicks = 0;

                final var extraClicks = limit / 20;
                if (1 < extraClicks) {
                    for (var i = 1; i < extraClicks; ++i) {
                        if (!mc.thePlayer.isUsingItem()) {
                            rightClick.run();
                        }
                    }
                }

                if (45 == limit || 15 == limit) {
                    ++AutoClicker.extraRightClickTicks;
                    if (4 <= AutoClicker.extraRightClickTicks && !mc.thePlayer.isUsingItem()) {
                        AutoClicker.extraRightClickTicks = 0;
                        rightClick.run();
                    }
                }

                return true;
            }

            return false;
        }

        return false;
    }

    /*static final void emulateACTick(final boolean left) {
        final Minecraft mc = Minecraft.getMinecraft();
        final IMixinMinecraft mm = (IMixinMinecraft) mc;

        final Runnable lc = mm::callClickMouse;
        final Runnable rc = mm::callRightClickMouse;

        if (left) {
            if (AutoClicker.isPressedStatic(mc.gameSettings.keyBindAttack, lc, rc) && !mc.thePlayer.isUsingItem()) {
                lc.run();
            }
        } else if (AutoClicker.isPressedStatic(mc.gameSettings.keyBindUseItem, lc, rc) && !mc.thePlayer.isUsingItem()) {
            rc.run();
        }
    }*/
}
