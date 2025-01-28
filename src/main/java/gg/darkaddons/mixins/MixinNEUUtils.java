package gg.darkaddons.mixins;

import gg.darkaddons.PublicUtils;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.util.Utils", priority = 999)
final class MixinNEUUtils {
    private MixinNEUUtils() {
        super();
    }

    @Inject(method = "cleanColour", at = @At("HEAD"), remap = false, cancellable = true)
    private static final void cleanColour$darkaddons(@NotNull final String text, @NotNull final CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(PublicUtils.removeControlCodes(text));
    }
}
