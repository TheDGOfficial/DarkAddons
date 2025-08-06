package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.settings.KeyBinding;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntityPlayerSP.class, priority = 1001)
final class MixinEntityPlayerSP {
    private MixinEntityPlayerSP() {
        super();
    }

    @Redirect(method = "onLivingUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"), require = 0)
    private final boolean isKeyDown$darkaddons(@NotNull final KeyBinding keyBinding) {
        return DarkAddons.isAlwaysSprint() || keyBinding.isKeyDown();
    }
}
