package gg.darkaddons.mixins;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundCategory;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.client.audio.SoundPoolEntry;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import org.jetbrains.annotations.NotNull;

@Mixin(value = SoundManager.class, priority = 1_001)
final class MixinSoundManager {
    private MixinSoundManager() {
        super();
    }

    @Inject(method = "getNormalizedVolume", at = @At("HEAD"), cancellable = true)
    private final void getNormalizedVolume$darkaddons(@NotNull final ISound sound, @NotNull final SoundPoolEntry entry, @NotNull final SoundCategory category, @NotNull final CallbackInfoReturnable<Float> cir) {
        if (gg.darkaddons.SoundManager.isShouldBypassVolumeLimit()) {
            cir.setReturnValue(1.0F);
        }
    }
}
