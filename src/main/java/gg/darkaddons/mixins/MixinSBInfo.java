package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.text.SimpleDateFormat;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.utils.SBInfo", priority = 999)
final class MixinSBInfo {
    private MixinSBInfo() {
        super();
    }

    @Unique
    private SimpleDateFormat cachedInstance;

    @Redirect(method = "onTick", at = @At(value = "NEW", target = "java/text/SimpleDateFormat", remap = false), remap = false)
    @NotNull
    private final SimpleDateFormat newSimpleDateFormat$darkaddons(@NotNull final String format) {
        if (null != this.cachedInstance) {
            return this.cachedInstance;
        }

        return this.cachedInstance = new SimpleDateFormat(format);
    }
}
