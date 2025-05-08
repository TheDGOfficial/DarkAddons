package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.DungeonTimer;

import java.util.List;
import java.util.ArrayList;

@Pseudo
@Mixin(targets = "me.Danker.utils.IceWalkUtils", priority = 999)
final class MixinIceWalkUtils {
    private MixinIceWalkUtils() {
        super();
    }

    @Inject(method = "findSolution", remap = false, at = @At("HEAD"))
    private static final void findSolution$darkaddons(final char[] @NotNull [] board, @NotNull @Coerce final Object startPos, @NotNull @Coerce final Object endPos, @NotNull final List<Object> route, @NotNull final CallbackInfoReturnable<List<Object>> cir) {
        if (-1L != DungeonTimer.getBossEntryTime() || !DarkAddons.isInDungeons()) {
            cir.setReturnValue(new ArrayList<>(0));
        }
    }
}
