package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "me.Danker.features.loot.CatacombsTracker", remap = false, priority = 1_001)
final class MixinDSMCatacombsTracker {
    private MixinDSMCatacombsTracker() {
        super();
    }

    @Inject(method = "onChat", remap = false, at = @At("HEAD"), cancellable = true)
    private final void onChat$darkaddons(@NotNull final CallbackInfo ci) {
        ci.cancel();
    }
}
