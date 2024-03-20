package gg.darkaddons.mixins;

import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.misc.MiscFeatures", priority = 999)
final class MixinMiscFeatures {
    private MixinMiscFeatures() {
        super();
    }

    @Redirect(method = "onCheckRender", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getHealth()F", remap = true))
    private final float getHealth$darkaddons(@NotNull final EntityLivingBase entityLivingBase) {
        return entityLivingBase.isDead ? 0.0F : entityLivingBase.getHealth();
    }
}
