package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import net.minecraft.util.Util;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ExecutionException;

@Mixin(value = Util.class, priority = 999)
final class MixinUtil {
    private MixinUtil() {
        super();
    }

    @Redirect(method = "runTask", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;fatal(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false))
    private static final void fatal$darkaddons(@NotNull final Logger logger, @NotNull final String message, @NotNull final Throwable throwable) {
        logger.fatal(message, DarkAddons.isOptimizeExceptions() && throwable instanceof ExecutionException ? throwable.getCause() : throwable);
    }
}
