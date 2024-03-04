package gg.darkaddons;

import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

final class HideFallingBlocks {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private HideFallingBlocks() {
        super();

        throw Utils.staticClassException();
    }

    static final void checkRender(@NotNull final Entity entity, @SuppressWarnings("BoundedWildcard") @NotNull final CallbackInfoReturnable<Boolean> cir) {
        cir.setReturnValue(false);
        entity.setDead();
    }
}
