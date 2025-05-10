package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.slayer.SlayerFeatures", remap = false, priority = 1_001)
final class MixinSlayerFeatures {
    private MixinSlayerFeatures() {
        super();
    }

    @Inject(method = "onRenderLivingPre", remap = false, at = @At(value = "HEAD", remap = false), cancellable = true)
    private final void onRenderLivingPre$darkaddons(@NotNull @Coerce final Object event, @NotNull final CallbackInfo ci) {
        ci.cancel();
    }
}
