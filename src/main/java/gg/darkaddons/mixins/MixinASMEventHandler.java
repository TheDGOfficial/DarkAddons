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

    @Unique
    private final void sanityCheck$darkaddons(final boolean checkListener) {
        if (null == this.owner) {
            throw MixinUtils.shadowFail();
        }

        //noinspection IfCanBeAssertion
        if (checkListener && null == this.listenerMethodName) {
            throw new IllegalStateException("this.listenerMethodName is null");
        }
    }

    @Inject(method = "<init>", at = @At("TAIL"), remap = false)
    private final void init$darkaddons(@NotNull final Object target, @NotNull final Method method, @NotNull final ModContainer ownerIn, @NotNull final CallbackInfo ci) {
        if (DarkAddons.isProfilerMode()) {
            this.listenerMethodName = method.getName();
            this.sanityCheck$darkaddons(true);
        }
    }

    @Inject(method = "invoke", at = @At("HEAD"), remap = false)
    private final void beforeInvoke$darkaddons(@NotNull final Event event, @NotNull final CallbackInfo ci) {
        if (DarkAddons.isProfilerMode() && null != this.listenerMethodName && DarkAddons.shouldProfile() && Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            this.sanityCheck$darkaddons(false);

            PublicUtils.startProfilingSection(this.owner.getModId() + '_' + this.listenerMethodName);
            this.startedProfiling = true;
        }
    }

    @Inject(method = "invoke", at = @At("TAIL"), remap = false)
    private final void afterInvoke$darkaddons(@NotNull final Event event, @NotNull final CallbackInfo ci) {
        if (null != this.listenerMethodName && this.startedProfiling && Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            this.sanityCheck$darkaddons(false);

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
