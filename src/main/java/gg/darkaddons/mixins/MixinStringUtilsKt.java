package gg.darkaddons.mixins;

import gg.darkaddons.PublicUtils;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.utils.StringUtilsKt", priority = 999)
final class MixinStringUtilsKt {
    private MixinStringUtilsKt() {
        super();
    }

    @Redirect(method = "stripControlCodes", at = @At(value = "INVOKE", target = "Lgg/essential/universal/wrappers/message/UTextComponent$Companion;stripFormatting(Ljava/lang/String;)Ljava/lang/String;"), remap = false)
    @NotNull
    private static final String stripControlCodes$darkaddons(@NotNull @Coerce final Object companionInstance, @NotNull final String text) {
        return PublicUtils.removeControlCodes(text);
    }
}
