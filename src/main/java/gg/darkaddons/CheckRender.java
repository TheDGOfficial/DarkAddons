package gg.darkaddons;

//import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
//import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.item.EntityFallingBlock;
//import net.minecraft.entity.item.EntityXPOrb;
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

    private static final boolean isHideWitherSkeletonsModeEnabled(final int mode) {
        return 1 == mode || 2 == mode;
    }

    private static final void forwardCheckRender(@NotNull final Entity entity, @NotNull final CallbackInfoReturnable<Boolean> cir) {
        McProfilerHelper.startSection("darkaddons_check_render");

        //noinspection ChainOfInstanceofChecks
        if (Config.isArmorStandOptimizer() && entity instanceof EntityArmorStand) {
            McProfilerHelper.startSection("armor_stand_optimizer_check_render");

            ArmorStandOptimizer.checkRender(entity, cir);

            McProfilerHelper.endSection();
        } else if (CheckRender.isHideWitherSkeletonsModeEnabled(Config.getHideWitherSkeletonsOnMaxor()) && entity instanceof EntitySkeleton) {
            McProfilerHelper.startSection("hide_wither_skeletons_check_render");

            HideWitherSkeletons.checkRender((EntitySkeleton) entity, cir);

            McProfilerHelper.endSection();
        } else if (CheckRender.isHideWitherSkeletonsModeEnabled(Config.getHideWitherSkeletonsOnMaxor()) && entity instanceof EntityWitherSkull) {
            McProfilerHelper.startSection("hide_wither_skeletons_check_render_skull");

            HideWitherSkeletons.checkRenderSkull((EntityWitherSkull) entity, cir);

            McProfilerHelper.endSection();
        } else if (Config.isHideFallingBlocks() && entity instanceof EntityFallingBlock) {
            McProfilerHelper.startSection("hide_falling_blocks_check_render");

            HideFallingBlocks.checkRender(entity, cir);

            McProfilerHelper.endSection();
        }/* else if (1 >= Config.getHideWitherKing() && AdditionalM7Features.phase5Started && entity instanceof EntityWither) {
            cir.setReturnValue(false);

            if (2 >= Config.getHideWitherKing()) {
                Minecraft.getMinecraft().theWorld.removeEntityFromWorld(entity.getEntityId());
            }
        } else if (1 >= Config.getHideXPOrbs() && DarkAddons.isInDungeons() && entity instanceof EntityXPOrb) {
            cir.setReturnValue(false);

            if (2 >= Config.getHideXPOrbs()) {
                Minecraft.getMinecraft().theWorld.removeEntityFromWorld(entity.getEntityId());
            }
        }*/
        McProfilerHelper.endSection();
    }
}
