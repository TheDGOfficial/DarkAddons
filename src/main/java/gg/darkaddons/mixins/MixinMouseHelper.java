package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import net.minecraft.util.MouseHelper;
import org.lwjgl.input.Mouse;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = MouseHelper.class, priority = 1_001)
final class MixinMouseHelper {
    private MixinMouseHelper() {
        super();
    }

    @Inject(method = "ungrabMouseCursor", at = @At("HEAD"), cancellable = true)
    private final void ungrabMouseCursor$darkaddons(@NotNull final CallbackInfo ci) {
        if (DarkAddons.isNeverResetCursor()) {
            ci.cancel();
            Mouse.setGrabbed(false);
        }
    }
}
