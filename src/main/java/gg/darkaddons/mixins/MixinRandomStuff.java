package gg.darkaddons.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import gg.darkaddons.DungeonTimer;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.misc.RandomStuff", priority = 999)
final class MixinRandomStuff {
    private MixinRandomStuff() {
        super();
    }

    @Redirect(method = "onCheckRenderEvent", remap = false, at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isInvisible()Z", remap = true))
    private final boolean isInvisible$darkaddons(@NotNull final Entity entity) {
        if (-1L == DungeonTimer.getPhase1ClearTime() || -1L != DungeonTimer.getBossClearTime() || !(entity instanceof EntityArmorStand)) {
            return false;
        }

        return entity.isInvisible();
    }
}
