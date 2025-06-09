package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Pseudo
@Mixin(targets = "me.Danker.utils.Utils", remap = false, priority = 1_001)
final class MixinDSMUtils {
    private MixinDSMUtils() {
        super();
    }

    @ModifyArg(method = "checkForDungeonFloor", remap = false, at = @At(value = "INVOKE", target = "Lme/Danker/locations/DungeonFloor;valueOf(Ljava/lang/String;)Lme/Danker/locations/DungeonFloor;"))
    private static final String fixEntrance$darkaddons(@NotNull final String floor) {
        return "E".equals(floor) ? "E0" : floor;
    }
}
