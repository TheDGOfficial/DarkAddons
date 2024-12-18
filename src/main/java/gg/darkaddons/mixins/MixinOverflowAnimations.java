package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "org.polyfrost.overflowanimations.OverflowAnimations", remap = false, priority = 1_001)
final class MixinOverflowAnimations {
    private MixinOverflowAnimations() {
        super();
    }

    @Redirect(method = "onTick", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/Class;forName(Ljava/lang/String;)Ljava/lang/Class;", remap = false))
    private final Class<?> classForName$darkaddons(@NotNull final String className) {
        return null;
    }
}
