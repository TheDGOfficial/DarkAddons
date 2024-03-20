package gg.darkaddons.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.util.ApiUtil", remap = false, priority = 1_001)
final class MixinApiUtil {
    private MixinApiUtil() {
        super();
    }

    @ModifyConstant(method = "<clinit>", constant = @Constant(intValue = 3))
    private static final int getExecutorSize$darkaddons(final int originalExecutorSize) {
        return Math.min(3, Runtime.getRuntime().availableProcessors());
    }
}
