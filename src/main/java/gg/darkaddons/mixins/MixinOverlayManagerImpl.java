package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "gg.essential.gui.overlay.OverlayManagerImpl", priority = 999)
final class MixinOverlayManagerImpl {
    private MixinOverlayManagerImpl() {
        super();
    }

    @Inject(method = "handleDraw", at = @At(value = "HEAD"), cancellable = true, remap = false)
    private final void handleDraw$darkaddons(@NotNull final CallbackInfo ci) {
        ci.cancel();
    }
}
