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

    static final boolean checkRender(@NotNull final Entity entity) {
        return CheckRender.forwardCheckRender(entity);
    }

    private static final boolean forwardCheckRender(@NotNull final Entity entity) {
        return !(entity instanceof final EntityArmorStand stand) || (ArmorStandOptimizer.checkRender(stand) && RemoveBlankArmorStands.checkRender(stand));
    }
}
