package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "club.sk1er.patcher.hooks.FontRendererHook", remap = false, priority = 1_001)
final class MixinFontRendererHook {
    private MixinFontRendererHook() {
        super();
    }

    @Inject(method = "renderStringAtPos(Ljava/lang/String;Z)Z", at = @At("HEAD"), cancellable = true)
    public void darkaddons$disablePatcherFontRenderer(@NotNull final String text, final boolean shadow, @NotNull final CallbackInfoReturnable<Boolean> cir) {
        if (DarkAddons.isDisablePatcherFontRenderer()) {
            cir.setReturnValue(false);
        }
    }

    @ModifyConstant(method = "getStringWidth", constant = @Constant(intValue = 5_000))
    private final int getStringWidthCacheLimit$darkaddons(final int originalMaxRetries) {
        return 10_000;
    }
}
