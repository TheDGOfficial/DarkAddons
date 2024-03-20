package gg.darkaddons.mixins;

import gg.skytils.skytilsmod.events.impl.CheckRenderEntityEvent;
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;
import net.minecraft.entity.item.EntityArmorStand;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.misc.RandomStuff", priority = 999)
final class MixinRandomStuff {
    private MixinRandomStuff() {
        super();
    }

    @Inject(method = "onCheckRenderEvent", remap = false, at = @At("HEAD"), cancellable = true)
    private final void onCheckRenderEvent$darkaddons(@NotNull final CheckRenderEntityEvent<?> event, @NotNull final CallbackInfo ci) {
        final var dungeonTimerInstance = DungeonTimer.INSTANCE;
        if (-1L == dungeonTimerInstance.getPhase1ClearTime() || -1L != dungeonTimerInstance.getBossClearTime() || !(event.getEntity() instanceof EntityArmorStand)) {
            ci.cancel();
        }
    }
}
