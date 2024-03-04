package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = RenderManager.class, priority = 1_001)
final class MixinRenderManager {
    private MixinRenderManager() {
        super();
    }

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private final void shouldRender$darkaddons(@NotNull final Entity entityIn, @NotNull final ICamera camera, final double camX, final double camY, final double camZ, @NotNull final CallbackInfoReturnable<Boolean> cir) {
        DarkAddons.checkRender(entityIn, cir);
    }

    @Inject(method = "doRenderEntity", at = @At("HEAD"))
    private final void doRenderEntityPre$darkaddons(@NotNull final Entity entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks, final boolean p_147939_10_, @NotNull final CallbackInfoReturnable<Boolean> cir) {
        DarkAddons.doRenderEntityPre(entity);
    }

    @Inject(method = "doRenderEntity", at = @At("TAIL"))
    private final void doRenderEntityPost$darkaddons(@NotNull final Entity entity, final double x, final double y, final double z, final float entityYaw, final float partialTicks, final boolean p_147939_10_, @NotNull final CallbackInfoReturnable<Boolean> cir) {
        DarkAddons.doRenderEntityPost();
    }
}
