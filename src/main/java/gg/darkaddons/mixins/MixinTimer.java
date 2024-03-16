package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.mixin.MixinUtils;
import gg.darkaddons.annotations.bytecode.Name;
import net.minecraft.util.Timer;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Timer.class, priority = 1_001)
final class MixinTimer {
    private MixinTimer() {
        super();
    }

    @Shadow
    private int elapsedTicks;

    @Shadow
    private float renderPartialTicks;

    @Shadow
    private float elapsedPartialTicks;

    @Inject(method = "updateTimer", at = @At(value = "FIELD", target = "Lnet/minecraft/util/Timer;elapsedTicks:I", opcode = Opcodes.GETFIELD, ordinal = 1), cancellable = true)
    private final void updateTimerCap$darkaddons(@NotNull final CallbackInfo ci) {
        final var smoothenFrames = DarkAddons.isSmoothenFrames();
        final var limit = smoothenFrames ? 1 : 10;

        if (this.elapsedTicks > limit) {
            /*final int skippedTicks = Math.min(10, this.elapsedTicks - limit);
            if (1 < skippedTicks && smoothenFrames && DarkAddons.isCatchupAutoClicker()) {
                DarkAddons.resetShouldClick();

                for (int i = 0; i < skippedTicks; ++i) {
                    DarkAddons.emulateACTick(true);
                    DarkAddons.emulateACTick(false);
                }
            }*/

            this.elapsedTicks = limit;
        }

        this.renderPartialTicks = this.elapsedPartialTicks;
        MixinUtils.setLastTicksRan(this.elapsedTicks);

        ci.cancel();
    }

    @Override
    @Name("toString$darkaddons")
    public final String toString() {
        return "MixinTimer{" +
            "elapsedTicks=" + this.elapsedTicks +
            ", renderPartialTicks=" + this.renderPartialTicks +
            ", elapsedPartialTicks=" + this.elapsedPartialTicks +
            '}';
    }
}
