package gg.darkaddons.mixins;

import gg.darkaddons.PublicUtils;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.core.util.StringUtils", priority = 999)
final class MixinNEUStringUtils {
    private MixinNEUStringUtils() {
        super();
    }

    @Redirect(method = "cleanColour", at = @At(value = "INVOKE", target = "Ljava/lang/String;replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"), remap = false)
    @NotNull
    private static final String cleanColour$darkaddons(@NotNull final String text, @NotNull final String pattern, @NotNull final String replacement) {
        return PublicUtils.removeControlCodes(text);
    }
}
