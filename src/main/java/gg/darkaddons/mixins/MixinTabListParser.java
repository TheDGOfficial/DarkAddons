package gg.darkaddons.mixins;

import net.minecraft.client.network.NetworkPlayerInfo;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

import com.google.common.collect.Ordering;

@Pseudo
@Mixin(targets = "codes.biscuit.skyblockaddons.features.tablist.TabListParser", priority = 999)
final class MixinTabListParser {
    private MixinTabListParser() {
        super();
    }

    @Redirect(method = "parse", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/Ordering;sortedCopy(Ljava/lang/Iterable;)Ljava/util/List;", remap = false), remap = false)
    @NotNull
    private static final List<NetworkPlayerInfo> sortedCopy$darkaddons(@NotNull final Ordering<NetworkPlayerInfo> ordering, @NotNull final Iterable<NetworkPlayerInfo> iterable) {
        return ordering.immutableSortedCopy(iterable);
    }
}
