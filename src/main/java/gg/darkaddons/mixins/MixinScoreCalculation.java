package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.NotNull;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.dungeons.ScoreCalculation$bonusScore$1", remap = false, priority = 1_001)
final class MixinScoreCalculation {
    private MixinScoreCalculation() {
        super();
    }

    @Inject(method = "invoke", remap = false, at = @At(value = "RETURN"), cancellable = true)
    private final void invoke$darkaddons(@NotNull final CallbackInfoReturnable<Integer> cir) {
        final var ret = cir.getReturnValueI();

        if (DarkAddons.isPrinceKilled()) {
            cir.setReturnValue(ret + 1);
        }
    }
}
