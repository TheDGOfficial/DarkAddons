package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.util.ItemResolutionQuery", remap = false, priority = 1_001)
final class MixinItemResolutionQuery {
    private MixinItemResolutionQuery() {
        super();
    }

    @Redirect(method = "resolveInternalName", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/String;intern()Ljava/lang/String;", remap = false))
    @NotNull
    private final String intern$darkaddons(@NotNull final String originalString) {
        return originalString;
    }
}
