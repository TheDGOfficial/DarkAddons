package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.listener.ItemTooltipListener", remap = false, priority = 1_001)
final class MixinNEUItemTooltipListener {
    private MixinNEUItemTooltipListener() {
        super();
    }

    @Redirect(method = "onItemTooltipLow", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/String;replaceAll(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", remap = false))
    @NotNull
    private final String replaceAll$darkaddons(@NotNull final String text, @NotNull final String search, @NotNull final String replacement) {
        // Who'll care about shiny items anyway (Cosmetics eww)
        // In the future, we should ideally just use StringUtils.replace or a compiled pattern here.
        return text;
    }
}
