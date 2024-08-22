package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.projectile.EntityWitherSkull;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

final class HideWitherSkeletons {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private HideWitherSkeletons() {
        super();

        throw Utils.staticClassException();
    }

    private static final boolean shouldRemove(final int mode) {
        return 2 == mode;
    }

    private static final boolean isEntityWitherSkeleton(@NotNull final EntityLivingBase entity) {
        return entity instanceof EntitySkeleton && HideWitherSkeletons.isWitherSkeleton((EntitySkeleton) entity);
    }

    private static final boolean isWitherSkeleton(@NotNull final EntitySkeleton entity) {
        return 1 == entity.getSkeletonType();
    }

    static final void checkRender(@NotNull final EntitySkeleton entity, @SuppressWarnings("BoundedWildcard") @NotNull final CallbackInfoReturnable<Boolean> cir) {
        if (AdditionalM7Features.isAtPhase1() && HideWitherSkeletons.isWitherSkeleton(entity)) {
            if (HideWitherSkeletons.shouldRemove(Config.getHideWitherSkeletonsOnMaxor())) {
                Minecraft.getMinecraft().theWorld.removeEntityFromWorld(entity.getEntityId());
            }

            cir.setReturnValue(false);
        }
    }

    static final void checkRenderSkull(@NotNull final EntityWitherSkull entity, @SuppressWarnings("BoundedWildcard") @NotNull final CallbackInfoReturnable<Boolean> cir) {
        if (AdditionalM7Features.isAtPhase1() && HideWitherSkeletons.isEntityWitherSkeleton(entity.shootingEntity)) {
            cir.setReturnValue(false);

            if (HideWitherSkeletons.shouldRemove(Config.getHideWitherSkeletonsOnMaxor())) {
                Minecraft.getMinecraft().theWorld.removeEntityFromWorld(entity.getEntityId());
            }
        }
    }
}
