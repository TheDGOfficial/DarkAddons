package gg.darkaddons.mixins;

import gg.darkaddons.PublicUtils;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "codes.biscuit.skyblockaddons.utils.TextUtils", priority = 999)
final class MixinTextUtils {
    private MixinTextUtils() {
        super();
    }

    @Inject(method = "stripColor", at = @At(value = "HEAD"), remap = false, cancellable = true)
    private static final void stripControlCodes$darkaddons(@NotNull final String text, @NotNull final CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(PublicUtils.removeControlCodes(text));
    }
}
