package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.mixin.MixinUtils;
import gg.darkaddons.annotations.bytecode.Name;
import net.minecraft.util.Timer;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
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
        final var limit = DarkAddons.isSmoothenFrames() ? 1 : 10;

        if (this.elapsedTicks > limit) {
            this.elapsedTicks = limit;
        }

        this.renderPartialTicks = this.elapsedPartialTicks;
        MixinUtils.setLastTicksRan(this.elapsedTicks);

        ci.cancel();
    }

    @Override
    @Unique
    @Name("toString$darkaddons")
    public final String toString() {
        return "MixinTimer{" +
            "elapsedTicks=" + this.elapsedTicks +
            ", renderPartialTicks=" + this.renderPartialTicks +
            ", elapsedPartialTicks=" + this.elapsedPartialTicks +
            '}';
    }
}
