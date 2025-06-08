package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "me.semx11.autotip.core.StatsManager", remap = false, priority = 1_001)
final class MixinStatsManager {
    private MixinStatsManager() {
        super();
    }

    @Inject(method = "save", remap = false, at = @At("HEAD"), cancellable = true)
    private final void save$darkaddons(@NotNull final CallbackInfo ci) {
        ci.cancel();
    }
}
