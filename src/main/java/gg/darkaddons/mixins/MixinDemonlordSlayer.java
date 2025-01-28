package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.util.BlockPos;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.slayer.impl.DemonlordSlayer$entityJoinWorld$1$1", remap = false, priority = 1_001)
final class MixinDemonlordSlayer {
    private MixinDemonlordSlayer() {
        super();
    }

    @Redirect(method = "invoke", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/item/EntityArmorStand;getDistanceSq(Lnet/minecraft/util/BlockPos;)D", remap = true))
    private final double invoke$darkaddons(@NotNull final EntityArmorStand entityArmorStand, @Nullable final BlockPos pos) {
        return null == pos ? 10.0D : entityArmorStand.getDistanceSq(pos);
    }
}
