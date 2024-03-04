package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.PublicUtils;
import gg.darkaddons.mixin.MixinUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Minecraft.class, priority = 1_001)
final class MixinMinecraft {
    private MixinMinecraft() {
        super();
    }

    @Inject(method = "runGameLoop", at = @At("HEAD"))
    private final void runGameLoopPre$darkaddons(@NotNull final CallbackInfo ci) {
        DarkAddons.handleGameLoopPre();
    }

    @Inject(method = "runGameLoop", at = @At("TAIL"))
    private final void runGameLoopPost$darkaddons(@NotNull final CallbackInfo ci) {
        DarkAddons.handleGameLoopPost();
    }

    private static final void start$darkaddons() {
        MixinUtils.setElapsedTicksStart(System.currentTimeMillis());
    }

    private static final void end$darkaddons() {
        MixinUtils.setElapsedTicksEnd(System.currentTimeMillis());
    }

    @Inject(method = "runGameLoop", at = @At(value = "FIELD", target = "Lnet/minecraft/util/Timer;elapsedTicks:I", opcode = Opcodes.GETFIELD))
    private final void beforeRunElapsedTicks$darkaddons(@NotNull final CallbackInfo ci) {
        MixinMinecraft.start$darkaddons();
    }

    @Inject(method = "runGameLoop", at = @At(value = "INVOKE", target = "Lnet/minecraft/profiler/Profiler;endStartSection(Ljava/lang/String;)V"))
    private final void afterRunElapsedTicks$darkaddons(@NotNull final CallbackInfo ci) {
        MixinMinecraft.end$darkaddons();
    }

    @Redirect(method = "runGameLoop", at = @At(value = "INVOKE", target = "Ljava/lang/Thread;yield()V", remap = false))
    private final void runGameLoopYield$darkaddons() {
        MixinMinecraft.runGameLoopYieldStatic$darkaddons();
    }

    private static final void runGameLoopYieldStatic$darkaddons() {
        if (!DarkAddons.isDisableYield()) {
            final var shouldProfile = DarkAddons.isProfilerMode() && DarkAddons.shouldProfile();
            if (shouldProfile) {
                PublicUtils.startProfilingSection("yield");
            }
            //noinspection CallToThreadYield
            Thread.yield();
            if (shouldProfile) {
                PublicUtils.endProfilingSection();
            }
        }
    }

    @Inject(method = "getLimitFramerate", at = @At("HEAD"), cancellable = true)
    private final void getLimitFramerate$darkaddons(@SuppressWarnings("BoundedWildcard") @NotNull final CallbackInfoReturnable<Integer> cir) {
        final var mc = Minecraft.getMinecraft();
        final var limit = DarkAddons.getMainMenuFrameLimit();

        if (30 != limit && null == mc.theWorld && null != mc.currentScreen) {
            cir.setReturnValue(limit);
        }
    }

    @Inject(method = "runTick", at = @At("HEAD"))
    private final void runTick$darkaddons(@NotNull final CallbackInfo ci) {
        DarkAddons.resetShouldClick();
    }

    private static final boolean isKeyDownStatic$darkaddons(@NotNull final KeyBinding keyBinding) {
        final var mc = Minecraft.getMinecraft();
        final var settings = mc.gameSettings;

        final var actual = keyBinding.isKeyDown();

        //noinspection ObjectEquality
        return (settings.keyBindUseItem != keyBinding || !DarkAddons.isRightClickAutoClicker() || !DarkAddons.isHoldingTerm(mc)) && (settings.keyBindAttack != keyBinding || !DarkAddons.isLeftClickAutoClicker() || !DarkAddons.isHoldingASword(mc)) && actual;
    }

    private static final boolean isKeyDownWithHooks$darkaddons(@NotNull final KeyBinding keyBinding) {
        return MixinMinecraft.isKeyDownStatic$darkaddons(keyBinding) && DarkAddons.isKeyDownHook(keyBinding);
    }

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isKeyDown()Z"))
    private final boolean isKeyDown$darkaddons(@NotNull final KeyBinding keyBinding) {
        return MixinMinecraft.isKeyDownWithHooks$darkaddons(keyBinding);
    }

    @Shadow
    private final void clickMouse() {
        throw MixinUtils.shadowFail();
    }

    @Shadow
    private final void rightClickMouse() {
        throw MixinUtils.shadowFail();
    }

    private final boolean isPressed0$darkaddons(@NotNull final KeyBinding keyBinding) {
        return DarkAddons.isPressedStatic(keyBinding, this::clickMouse, this::rightClickMouse);
    }

    @Redirect(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/settings/KeyBinding;isPressed()Z"))
    private final boolean isPressed$darkaddons(@NotNull final KeyBinding keyBinding) {
        return this.isPressed0$darkaddons(keyBinding);
    }

    @Inject(method = "shutdownMinecraftApplet", at = @At("HEAD"))
    private final void shutdownMinecraftApplet$darkaddons(@NotNull final CallbackInfo ci) {
        DarkAddons.runShutdownTasks();
    }
}
