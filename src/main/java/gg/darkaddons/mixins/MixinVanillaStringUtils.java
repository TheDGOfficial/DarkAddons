package gg.darkaddons.mixins;

import net.minecraft.util.StringUtils;

import gg.darkaddons.PublicUtils;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = StringUtils.class, priority = 999)
final class MixinVanillaStringUtils {
    private MixinVanillaStringUtils() {
        super();
    }

    @Inject(method = "stripControlCodes", at = @At(value = "HEAD"), cancellable = true)
    private static final void stripControlCodes$darkaddons(@NotNull final String text, @NotNull final CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(PublicUtils.removeControlCodes(text));
    }
}
