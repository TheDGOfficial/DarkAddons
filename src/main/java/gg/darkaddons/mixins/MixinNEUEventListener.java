package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.listener.NEUEventListener", remap = false, priority = 1_001)
final class MixinNEUEventListener {
    private MixinNEUEventListener() {
        super();
    }

    @Redirect(method = "onWorldLoad", remap = false, at = @At(value = "INVOKE", target = "Lio/github/moulberry/notenoughupdates/NotEnoughUpdates;saveConfig()V", remap = false), require = 0)
    private final void saveConfig$darkaddons(@NotNull @Coerce final Object neuInstance) {
        // Do nothing
    }
}
