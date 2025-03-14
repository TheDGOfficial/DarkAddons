package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.PublicUtils;
import gg.darkaddons.mixin.MixinUtils;
import gg.darkaddons.annotations.bytecode.Name;
import net.minecraft.client.Minecraft;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.ASMEventHandler;
import net.minecraftforge.fml.common.eventhandler.Event;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Method;

@Mixin(value = ASMEventHandler.class, priority = 1_001)
final class MixinASMEventHandler {
    private MixinASMEventHandler() {
        super();
    }

    @Nullable
    @Unique
    private String listenerMethodName;

    @Nullable
    @Shadow(remap = false)
    private ModContainer owner;

    @Unique
    private boolean startedProfiling;

    @Inject(method = "<init>", at = @At("RETURN"), remap = false)
    private final void init$darkaddons(@NotNull final Object target, @NotNull final Method method, @NotNull final ModContainer ownerIn, @NotNull final CallbackInfo ci) {
        if (DarkAddons.isProfilerMode()) {
            this.listenerMethodName = this.owner.getModId() + '_' + method.getName();
        }
    }

    @Inject(method = "invoke", at = @At("HEAD"), remap = false)
    private final void beforeInvoke$darkaddons(@NotNull final Event event, @NotNull final CallbackInfo ci) {
        if (DarkAddons.isProfilerMode() && null != this.listenerMethodName && DarkAddons.shouldProfile() && Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            PublicUtils.startProfilingSection(this.listenerMethodName);
            this.startedProfiling = true;
        }
    }

    @Inject(method = "invoke", at = @At("RETURN"), remap = false)
    private final void afterInvoke$darkaddons(@NotNull final Event event, @NotNull final CallbackInfo ci) {
        if (null != this.listenerMethodName && this.startedProfiling && Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            PublicUtils.endProfilingSection();
            this.startedProfiling = false;
        }
    }

    @Override
    @Unique
    @Name("toString$darkaddons")
    public final String toString() {
        return "MixinASMEventHandler{" +
            "listenerMethodName='" + this.listenerMethodName + '\'' +
            ", owner=" + this.owner +
            ", startedProfiling=" + this.startedProfiling +
            '}';
    }
}
