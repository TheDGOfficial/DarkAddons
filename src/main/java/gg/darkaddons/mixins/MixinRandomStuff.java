package gg.darkaddons.mixins;

import gg.skytils.skytilsmod.events.impl.CheckRenderEntityEvent;
import net.minecraft.entity.item.EntityArmorStand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import gg.darkaddons.DungeonTimer;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.misc.RandomStuff", priority = 999)
final class MixinRandomStuff {
    private MixinRandomStuff() {
        super();
    }

    @Inject(method = "onCheckRenderEvent", remap = false, at = @At("HEAD"), cancellable = true)
    private final void onCheckRenderEvent$darkaddons(@NotNull final CheckRenderEntityEvent<?> event, @NotNull final CallbackInfo ci) {
        if (-1L == DungeonTimer.getPhase1ClearTime() || -1L != DungeonTimer.getBossClearTime() || !(event.getEntity() instanceof EntityArmorStand)) {
            ci.cancel();
        }
    }
}
