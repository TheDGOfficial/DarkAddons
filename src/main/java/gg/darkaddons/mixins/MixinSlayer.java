package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.item.EntityArmorStand;

import kotlin.Pair;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.slayer.base.Slayer$detectSlayerEntities$1", remap = false, priority = 1_001)
final class MixinSlayer {
    private MixinSlayer() {
        super();
    }

    @Inject(method = "invoke", remap = false, at = @At(value = "NEW", target = "java/lang/Exception", remap = false), cancellable = true)
    private final void invoke$darkaddons(@NotNull final CallbackInfoReturnable<Pair<EntityArmorStand, EntityArmorStand>> cir) {
        cir.setReturnValue(null);
    }
}
