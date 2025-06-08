package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "me.Danker.features.MeterTracker", remap = false, priority = 1_001)
final class MixinMeterTracker {
    private MixinMeterTracker() {
        super();
    }

    @Inject(method = "onChat", remap = false, at = @At("HEAD"), cancellable = true)
    private final void onChat$darkaddons(@NotNull final CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onTick", remap = false, at = @At("HEAD"), cancellable = true)
    private final void onTick$darkaddons(@NotNull final CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "onSlotClick", remap = false, at = @At("HEAD"), cancellable = true)
    private final void onSlotClick$darkaddons(@NotNull final CallbackInfo ci) {
        ci.cancel();
    }
}
