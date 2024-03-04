package gg.darkaddons.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Constant;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.Skytils$Companion$client$1$3", remap = false, priority = 1_001)
final class MixinSkytilsClientRetryConfig {
    private MixinSkytilsClientRetryConfig() {
        super();
    }

    @ModifyConstant(method = "invoke", constant = @Constant(intValue = 3))
    private final int getMaxRetries$darkaddons(final int originalMaxRetries) {
        return 0;
    }
}
