package gg.darkaddons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

final class CheckRender {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private CheckRender() {
        super();

        throw Utils.staticClassException();
    }

    static final void checkRender(@NotNull final Entity entity, @NotNull final CallbackInfoReturnable<Boolean> cir) {
        CheckRender.forwardCheckRender(entity, cir);
    }

    private static final void forwardCheckRender(@NotNull final Entity entity, @NotNull final CallbackInfoReturnable<Boolean> cir) {
        if (Config.isArmorStandOptimizer() && entity instanceof final EntityArmorStand stand && (!ArmorStandOptimizer.checkRender(stand) || !RemoveBlankArmorStands.checkRender(stand))) {
            cir.setReturnValue(false);
        }
    }
}
