package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.mixin.MixinUtils;
import net.minecraft.scoreboard.Scoreboard;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = Scoreboard.class, priority = 999)
final class MixinScoreboard {
    private MixinScoreboard() {
        super();
    }

    @Redirect(method = "createTeam", at = @At(value = "NEW", target = "(Ljava/lang/String;)Ljava/lang/IllegalArgumentException;", ordinal = 1))
    private final IllegalArgumentException newIllegalArgumentException$darkaddons(@NotNull final String message) {
        return DarkAddons.isOptimizeExceptions() ? MixinUtils.IllegalArgumentExceptionHolder.getInstance() : new IllegalArgumentException(message);
    }
}
