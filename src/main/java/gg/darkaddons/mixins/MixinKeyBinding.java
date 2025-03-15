package gg.darkaddons.mixins;

import gg.darkaddons.mixin.MixinUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = KeyBinding.class, priority = 1_001)
final class MixinKeyBinding {
    private MixinKeyBinding() {
        super();
    }

    @Shadow
    private final int getKeyCode() {
        throw MixinUtils.shadowFail();
    }

    @Inject(method = "isKeyDown", at = @At("RETURN"), cancellable = true)
    private final void isKeyDown$darkaddons(@NotNull final CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValue()) {
            final var gameSettings = Minecraft.getMinecraft().gameSettings;
            final var keyCode = this.getKeyCode();
            if (keyCode == gameSettings.keyBindJump.getKeyCode() && MixinUtils.isJumpOverride() && MixinUtils.getJumpOverridePrecondition().getAsBoolean() || keyCode == gameSettings.keyBindSneak.getKeyCode() && MixinUtils.isSneakOverride() && MixinUtils.getSneakOverridePrecondition().getAsBoolean()) {
                cir.setReturnValue(true);
            }
        }
    }
}
