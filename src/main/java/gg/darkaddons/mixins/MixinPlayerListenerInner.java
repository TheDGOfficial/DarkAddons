package gg.darkaddons.mixins;

import gg.darkaddons.PublicUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "de.cowtipper.cowlection.listener.PlayerListener$1", remap = false, priority = 1_001)
final class MixinPlayerListenerInner {
    private MixinPlayerListenerInner() {
        super();
    }

    @Redirect(method = "onTickCheckScoreboard", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/EnumChatFormatting;getTextWithoutFormattingCodes(Ljava/lang/String;)Ljava/lang/String;"), require = 0)
    @NotNull
    private final String getTextWithoutFormattingCodes$darkaddons(@NotNull final String text) {
        return PublicUtils.removeControlCodes(text);
    }
}
