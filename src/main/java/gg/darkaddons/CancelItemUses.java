package gg.darkaddons;

import net.minecraft.client.Minecraft;

import net.minecraft.client.settings.KeyBinding;

import org.jetbrains.annotations.NotNull;

final class CancelItemUses {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private CancelItemUses() {
        super();

        throw Utils.staticClassException();
    }

    private static final boolean IS_MOD_DEVELOPER = "d5c721ce-1b12-4f71-9c1d-0f4e36829f99".equals(Minecraft.getMinecraft().thePlayer.getUniqueID().toString());

    static final boolean shouldAllowKeyPress(@NotNull final KeyBinding keyBinding) {
        if (!Config.isExtraLuck() || !CancelItemUses.IS_MOD_DEVELOPER) {
            return true;
        }

        final var mc = Minecraft.getMinecraft();
        final var settings = mc.gameSettings;

        return settings.keyBindUseItem == keyBinding ? !Utils.isHoldingItemContaining(mc, "Gloomlock Grimoire") && !Utils.isHoldingItemContaining(mc, "Terminator") && !Utils.isHoldingItemContaining(mc, "Daedalus Axe") && !Utils.isHoldingItemContaining(mc, "Gyrokinetic Wand") : settings.keyBindAttack != keyBinding || !Utils.isHoldingItemContaining(mc, "Terminator");
    }
}
