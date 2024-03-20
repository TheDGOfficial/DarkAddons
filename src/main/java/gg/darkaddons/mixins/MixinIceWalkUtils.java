package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;
import gg.skytils.skytilsmod.utils.Utils;

import java.util.List;
import java.util.ArrayList;

@Pseudo
@Mixin(targets = "me.Danker.utils.IceWalkUtils", priority = 999)
final class MixinIceWalkUtils {
    private MixinIceWalkUtils() {
        super();
    }

    @Inject(method = "findSolution", remap = false, at = @At("HEAD"))
    private static final void findSolution$darkaddons(@NotNull final char[][] board, @NotNull @Coerce final Object startPos, @NotNull @Coerce final Object endPos, @NotNull final List<Object> route, @NotNull final CallbackInfoReturnable<List<Object>> cir) {
        if (-1L != DungeonTimer.INSTANCE.getBossEntryTime() || !Utils.INSTANCE.getInDungeons()) {
            cir.setReturnValue(new ArrayList<Object>(0));
        }
    }
}
