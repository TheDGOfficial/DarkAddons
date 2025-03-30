package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

import com.google.common.collect.Ordering;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.util.TabListUtils", priority = 999)
final class MixinTabListUtils {
    private MixinTabListUtils() {
        super();
    }

    @Redirect(method = "getTabList0", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;", remap = false), remap = false)
    @NotNull
    private final <T> List<T> sortedCopy$darkaddons(@NotNull final Ordering<T> ordering, @NotNull final Iterable<T> iterable) {
        return ordering.immutableSortedCopy(iterable);
    }
}
