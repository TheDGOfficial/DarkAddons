package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "codes.biscuit.skyblockaddons.core.dungeons.DungeonManager", remap = false, priority = 1_001)
final class MixinSBADungeonManager {
    private MixinSBADungeonManager() {
        super();
    }

    @Redirect(method = "updateDungeonPlayer", remap = false, at = @At(value = "INVOKE", target = "Ljava/lang/String;equals(Ljava/lang/Object;)Z", remap = false))
    private final boolean equals$darkaddons(@NotNull final String s, @NotNull final Object o) {
        return s.isEmpty() || s.equals(o);
    }
}
