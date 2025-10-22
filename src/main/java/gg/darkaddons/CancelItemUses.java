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
        final var mc = Minecraft.getMinecraft();
        final var settings = mc.gameSettings;
        final var rc = settings.keyBindUseItem == keyBinding;

        if (rc && Config.isDisableCellsAlignment()) {
            return !ItemUtils.isHoldingItemContaining(mc, "Gyrokinetic Wand");
        }

        if (!Config.isExtraLuck() || !CancelItemUses.IS_MOD_DEVELOPER) {
            return true;
        }

        // Custom click prevent rules only used by mod dev if extra luck is toggled (used when doing diana)
        // Disallows rc to heal yourself via gloomlock which reduces your dmg - gloom in diana used mainly to regen mana to use spade, which is leftclick
        // Disallows rc+lc with term, only used for more wither shield (term swapping for more crit dmg) against strong mobs like runic exalted inquisitor
        // Disallows rc with daedalus blade, which slows you down due to block hit.
        return rc ? !ItemUtils.isHoldingItemContaining(mc, "Gloomlock Grimoire") && !ItemUtils.isHoldingItemContaining(mc, "Terminator") && !ItemUtils.isHoldingItemContaining(mc, "Daedalus Blade") : settings.keyBindAttack != keyBinding || !ItemUtils.isHoldingItemContaining(mc, "Terminator");
    }
}
