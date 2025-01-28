package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gg.skytils.skytilsmod.utils.Utils;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.misc.MiscFeatures$WorldAgeDisplay", priority = 999)
final class MixinWorldAgeDisplay {
    private MixinWorldAgeDisplay() {
        super();
    }

    @Inject(method = "getToggled", at = @At("RETURN"), remap = false, cancellable = true)
    private final void getToggled$darkaddons(@NotNull final CallbackInfoReturnable<Boolean> cir) {
        if (cir.getReturnValue()) {
            cir.setReturnValue(!Utils.INSTANCE.getInDungeons());
        }
    }
}
