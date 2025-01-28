package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Locale;

@Pseudo
@Mixin(targets = "me.Danker.features.puzzlesolvers.SelectAllColourSolver", remap = false, priority = 1_001)
final class MixinDSMSelectAllColourSolver {
    private MixinDSMSelectAllColourSolver() {
        super();
    }

    @Redirect(method = "onGuiRender", at = @At(value = "INVOKE", target = "Ljava/lang/String;toUpperCase()Ljava/lang/String;", remap = false))
    @NotNull
    private final String toUpperCase$darkaddons(@NotNull final String text) {
        return text.toUpperCase(Locale.ROOT);
    }

    @Redirect(method = "onGuiRender", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/String;contains(Ljava/lang/CharSequence;)Z"))
    private final boolean onGuiRender$darkaddons(@NotNull final String itemName, @NotNull final CharSequence termColorNeeded) {
        final var terminalColorNeeded = termColorNeeded.toString();
        if (itemName.contains(terminalColorNeeded)) {
            final var wantedBlue = "BLUE".equals(terminalColorNeeded);
            return !wantedBlue || !itemName.contains("LIGHT BLUE");
        }
        return false;
    }
}
