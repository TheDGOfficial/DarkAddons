package gg.darkaddons.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Pseudo
@Mixin(targets = "com.desco.watersolver.WaterSolverMod", remap = false, priority = 1_001)
final class MixinWaterSolverMod {
    private MixinWaterSolverMod() {
        super();
    }

    @Redirect(method = "<clinit>", remap = false, at = @At(value = "INVOKE", target = "Ljava/util/concurrent/Executors;newFixedThreadPool(I)Ljava/util/concurrent/ExecutorService;", remap = false))
    private static final ExecutorService newFixedThreadPool$darkaddons(final int originalPoolSize) {
        return Executors.newFixedThreadPool(1);
    }
}
