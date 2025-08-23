package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.util.DamageSource;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = EntityArrow.class, priority = 999)
final class MixinEntityArrow {
    private MixinEntityArrow() {
        super();
    }

    @Redirect(method = "onUpdate", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;attackEntityFrom(Lnet/minecraft/util/DamageSource;F)Z"), require = 0)
    private final boolean onArrowHit(@NotNull final Entity entity, @NotNull final DamageSource damageSource, final float damage) {
        DarkAddons.onArrowHit((EntityArrow) (Object) this, entity);

        return entity.attackEntityFrom(damageSource, damage);
    }
}
