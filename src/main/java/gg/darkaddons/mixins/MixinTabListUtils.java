package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.ArrayList;

import com.google.common.collect.Ordering;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.util.TabListUtils", priority = 999)
final class MixinTabListUtils {
    private MixinTabListUtils() {
        super();
    }

    @Unique
    private List<?> immutableSortedCopy;

    @Redirect(method = "getTabList0", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;", remap = false), remap = false)
    @NotNull
    private final <T> List<T> sortedCopy$darkaddons(@NotNull final Ordering<T> ordering, @NotNull final Iterable<T> iterable) {
        final var copy = ordering.immutableSortedCopy(iterable);
        this.immutableSortedCopy = copy;

        return copy;
    }

    @Redirect(method = "getTabList0", at = @At(value = "NEW", target = "java/util/ArrayList", remap = false), remap = false)
    @NotNull
    private final ArrayList<String> newArrayList$darkaddons() {
        final var list = new ArrayList<String>(this.immutableSortedCopy.size());
        this.immutableSortedCopy = null;

        return list;
    }
}
