package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Render.class, priority = 1_001)
final class MixinRender {
    private MixinRender() {
        super();
    }

    @Inject(method = "renderLivingLabel", at = @At("HEAD"), cancellable = true)
    private final void shouldRenderLivingLabel$darkaddons(@NotNull final Entity entityIn, @NotNull final String label, final double camX, final double camY, final double camZ, final int maxDistance, @NotNull final CallbackInfo ci) {
        if (!DarkAddons.checkRender(entityIn)) {
            ci.cancel();
        }
    }
}
