package gg.darkaddons.mixins;

import gg.skytils.skytilsmod.features.impl.misc.MiscFeatures;
import net.minecraft.entity.EntityLivingBase;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(value = MiscFeatures.class, priority = 999)
final class MixinMiscFeatures {
    private MixinMiscFeatures() {
        super();
    }

    @Redirect(method = "onCheckRender", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/EntityLivingBase;getHealth()F"))
    private final float getHealth$darkaddons(@NotNull final EntityLivingBase entityLivingBase) {
        return entityLivingBase.isDead ? 0.0F : entityLivingBase.getHealth();
    }
}
