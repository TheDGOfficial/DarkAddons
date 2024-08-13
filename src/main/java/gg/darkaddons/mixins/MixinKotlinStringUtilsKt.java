package gg.darkaddons.mixins;

import gg.darkaddons.PublicUtils;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.util.KotlinStringUtilsKt", priority = 999)
final class MixinKotlinStringUtilsKt {
    private MixinKotlinStringUtilsKt() {
        super();
    }

    @Redirect(method = "stripControlCodes", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringUtils;stripControlCodes(Ljava/lang/String;)Ljava/lang/String;", remap = true), remap = false)
    @NotNull
    private static final String stripControlCodes$darkaddons(@NotNull final String text) {
        return PublicUtils.removeControlCodes(text);
    }
}
