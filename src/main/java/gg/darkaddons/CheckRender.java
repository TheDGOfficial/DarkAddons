package gg.darkaddons;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.projectile.EntityWitherSkull;
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

    private static final boolean isHideWitherSkeletonsModeEnabled() {
        final var mode = Config.getHideWitherSkeletonsOnMaxor();
        return 1 == mode || 2 == mode;
    }

    private static final void forwardCheckRender(@NotNull final Entity entity, @NotNull final CallbackInfoReturnable<Boolean> cir) {
        if (Config.isArmorStandOptimizer() && entity instanceof EntityArmorStand) {
            if (!ArmorStandOptimizer.checkRender(entity)) {
                cir.setReturnValue(false);
                return;
            }
            if (!RemoveBlankArmorStands.checkRender(entity)) {
                cir.setReturnValue(false);
                return;
            }
        } else if (CheckRender.isHideWitherSkeletonsModeEnabled() && entity instanceof EntitySkeleton && !HideWitherSkeletons.checkRender((EntitySkeleton) entity)) {
            cir.setReturnValue(false);
            return;
        } else if (CheckRender.isHideWitherSkeletonsModeEnabled() && entity instanceof EntityWitherSkull && !HideWitherSkeletons.checkRenderSkull((EntityWitherSkull) entity)) {
            cir.setReturnValue(false);
            return;
        }
    }
}
