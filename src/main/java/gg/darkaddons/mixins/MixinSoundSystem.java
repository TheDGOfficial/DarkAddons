package gg.darkaddons.mixins;

import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemConfig;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = SoundSystem.class, remap = false, priority = 1_001)
final class MixinSoundSystem {
    private MixinSoundSystem() {
        super();
    }

    @Redirect(method = "cleanup", remap = false, at = @At(value = "INVOKE", target = "Lpaulscode/sound/SoundSystem;importantMessage(Ljava/lang/String;I)V", remap = false))
    private final void downgradeToInfoFromWarn$darkaddons(@NotNull final SoundSystem soundSystem, @NotNull final String message, final int indent) {
        // Replace call to importantMessage with message - this in fact not an important message. While we support the creator showing off themselves, it does not need to be logged at the WARN log level.
        SoundSystemConfig.getLogger().message(message, indent); // same as soundSystem.message(message, indent) but that one is public
    }
}
