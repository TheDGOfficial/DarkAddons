package gg.darkaddons.mixins;

import gg.darkaddons.mixin.MixinUtils;

import gg.darkaddons.DarkAddons;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.NotEnoughUpdates", remap = false, priority = 1_001)
final class MixinNEU {
    private MixinNEU() {
        super();
    }

    @Shadow
    private final void saveConfig() {
        throw MixinUtils.shadowFail();
    }

    @Inject(method = "<init>", remap = false, at = @At(value = "RETURN"))
    private final void afterClassInitializer$darkaddons(@NotNull final CallbackInfo ci) {
        // Save before cleanly exiting the game via "Quit Game" in the main menu in case periodic save didn't kick in after changing settings in time (or disabled)
        DarkAddons.addShutdownTask(this::saveConfig);
    }
}
