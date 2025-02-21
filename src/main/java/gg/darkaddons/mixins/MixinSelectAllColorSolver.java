package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.dungeons.solvers.terminals.SelectAllColorSolver", remap = false, priority = 1_001)
final class MixinSelectAllColorSolver {
    private MixinSelectAllColorSolver() {
        super();
    }

    @Redirect(method = "onBackgroundDrawn", remap = false, at = @At(value = "INVOKE", target = "Lkotlin/text/StringsKt;contains$default(Ljava/lang/CharSequence;Ljava/lang/CharSequence;ZILjava/lang/Object;)Z", ordinal = 1))
    private final boolean onBackgroundDrawn$darkaddons(@NotNull final CharSequence iName, @NotNull final CharSequence termColorNeeded, final boolean ignoreCase, final int i, @Nullable final Object o) {
        return MixinSelectAllColorSolver.fixBlueItemsMatchingLightBlueItems(iName, termColorNeeded);
    }

    @Redirect(method = "handleItemStack", remap = false, at = @At(value = "INVOKE", target = "Lkotlin/text/StringsKt;contains$default(Ljava/lang/CharSequence;Ljava/lang/CharSequence;ZILjava/lang/Object;)Z", ordinal = 1))
    private final boolean handleItemStack$darkaddons(@NotNull final CharSequence iName, @NotNull final CharSequence termColorNeeded, final boolean ignoreCase, final int i, @Nullable final Object o) {
        return MixinSelectAllColorSolver.fixBlueItemsMatchingLightBlueItems(iName, termColorNeeded);
    }

    private static final boolean fixBlueItemsMatchingLightBlueItems(@NotNull final CharSequence iName, @NotNull final CharSequence termColorNeeded) {
        final var itemName = iName.toString();
        final var terminalColorNeeded = termColorNeeded.toString();
        if (itemName.contains(terminalColorNeeded)) {
            final var wantedBlue = "BLUE".equals(terminalColorNeeded);
            return !wantedBlue || !itemName.contains("lightBlue");
        }
        return false;
    }
}
