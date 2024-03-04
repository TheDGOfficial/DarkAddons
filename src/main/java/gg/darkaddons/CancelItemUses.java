package gg.darkaddons;

//import net.minecraft.client.Minecraft;//
//import net.minecraft.client.settings.GameSettings;//

import net.minecraft.client.settings.KeyBinding;
//import net.minecraft.item.ItemStack;//

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

    static final boolean shouldAllowKeyPress(@NotNull final KeyBinding keyBinding) {
        //
        /*
        final Minecraft mc = Minecraft.getMinecraft();
        final GameSettings settings = mc.gameSettings;

        if (settings.keyBindUseItem == keyBinding) {
            final ItemStack itemStack = Utils.getHeldItemStack(mc);

            if (null != itemStack) {
                final String displayName = itemStack.getDisplayName();

                if (null != displayName) {
                    return !displayName.contains("Gloomlock Grimoire") && !displayName.contains("Terminator") &&
                            !displayName.contains("Daedalus Axe") && !displayName.contains("Gyrokinetic Wand");
                }
            }
        } else if (settings.keyBindAttack == keyBinding) {
            final ItemStack itemStack = Utils.getHeldItemStack(mc);

            if (null != itemStack) {
                final String displayName = itemStack.getDisplayName();

                if (null != displayName) {
                    return !displayName.contains("Terminator");
                }
            }
        }
        */
        //

        return true;
    }
}
