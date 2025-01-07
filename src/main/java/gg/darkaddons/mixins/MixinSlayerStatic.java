package gg.darkaddons.mixins;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.item.EntityArmorStand;

import kotlin.Pair;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.features.impl.slayer.base.Slayer$1", remap = false, priority = 1_001)
final class MixinSlayerStatic {
    private MixinSlayerStatic() {
        super();
    }

    @Redirect(method = "invokeSuspend", remap = false, at = @At(value = "INVOKE", target = "Lkotlin/Pair;component1()Ljava/lang/Object;", remap = false))
    private final Object component1$darkaddons(@Nullable final Pair<EntityArmorStand, EntityArmorStand> pair, @NotNull final Object result) {
        return null == pair ? null : pair.component1();
    }

    @Redirect(method = "invokeSuspend", remap = false, at = @At(value = "INVOKE", target = "Lkotlin/Pair;component2()Ljava/lang/Object;", remap = false))
    private final Object component2$darkaddons(@Nullable final Pair<EntityArmorStand, EntityArmorStand> pair, @NotNull final Object result) {
        return null == pair ? null : pair.component2();
    }

    @Inject(method = "invokeSuspend", remap = false, at = @At(value = "INVOKE_ASSIGN", target = "Lkotlin/Pair;component2()Ljava/lang/Object;", remap = false), cancellable = true, locals = LocalCapture.CAPTURE_FAILSOFT)
    private final void invokeSuspend$darkaddons(@NotNull final Object resultParam, @NotNull final CallbackInfoReturnable<Object> cir, @NotNull final Object result, @Nullable final Pair<EntityArmorStand, EntityArmorStand> pair, @Nullable final EntityArmorStand n) {
        if (null == n) {
            cir.setReturnValue(null);
        }
    }
}
