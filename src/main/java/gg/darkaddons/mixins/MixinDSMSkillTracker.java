package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "me.Danker.features.SkillTracker", remap = false, priority = 1_001)
final class MixinDSMSkillTracker {
    private MixinDSMSkillTracker() {
        super();
    }

    @Inject(method = "onChat", remap = false, at = @At("HEAD"), require = 0, cancellable = true)
    private final void onChat$darkaddons(@NotNull final CallbackInfo ci) {
        ci.cancel();
    }
}
