package gg.darkaddons.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.jetbrains.annotations.NotNull;

@Pseudo
@Mixin(targets = {"gg.skytils.skytilsmod.features.impl.funny.Funny", "gg.skytils.skytilsmod.features.impl.misc.Funny"}, remap = false, priority = 1_001)
final class MixinFunny {
    private MixinFunny() {
        super();
    }

    @Inject(method = "joinedSkyblock", remap = false, at = @At(value = "HEAD"), cancellable = true)
    private final void joinedSkyblock$darkaddons(@NotNull final CallbackInfo ci) {
        ci.cancel();
    }
}
