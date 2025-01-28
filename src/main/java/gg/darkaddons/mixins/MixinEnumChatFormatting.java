package gg.darkaddons.mixins;

import net.minecraft.util.EnumChatFormatting;

import gg.darkaddons.PublicUtils;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EnumChatFormatting.class, priority = 999)
final class MixinEnumChatFormatting {
    private MixinEnumChatFormatting() {
        super();
    }

    @Inject(method = "getTextWithoutFormattingCodes", at = @At("HEAD"), cancellable = true)
    private static final void getTextWithoutFormattingCodes$darkaddons(@NotNull final String text, @NotNull final CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(PublicUtils.removeControlCodes(text));
    }
}
