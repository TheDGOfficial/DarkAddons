package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;

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
        if (!IS_MOD_DEVELOPER || !Config.isExtraLuck()) {
            return true;
        }

        final Minecraft mc = Minecraft.getMinecraft();
        final GameSettings settings = mc.gameSettings;

        if (settings.keyBindUseItem == keyBinding) {
            return !Utils.isHoldingItemContaining(mc, "Gloomlock Grimoire") && !Utils.isHoldingItemContaining(mc, "Terminator") && !Utils.isHoldingItemContaining(mc, "Daedalus Axe") && !Utils.isHoldingItemContaining(mc, "Gyrokinetic Wand");
        }
        if (settings.keyBindAttack == keyBinding) {
            return !Utils.isHoldingItemContaining(mc, "Terminator");
        }

        return true;
    }
}
