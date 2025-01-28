package gg.darkaddons.mixins;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.UUID;

import net.minecraft.client.Minecraft;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.cosmetics.CapeManager", priority = 999)
final class MixinCapeManager {
    private MixinCapeManager() {
        super();
    }

    @Unique
    @Nullable
    private static String selfUUIDCached;

    @Redirect(method = "onPlayerTick", at = @At(value = "INVOKE", target = "Ljava/util/UUID;toString()Ljava/lang/String;"), remap = false)
    @NotNull
    private final String toString$darkaddons(@NotNull final UUID uuid) {
        final var selfUUID = Minecraft.getMinecraft().thePlayer.getUniqueID();
        final var self = selfUUID == uuid;

        if (self && null == MixinCapeManager.selfUUIDCached) {
            MixinCapeManager.selfUUIDCached = StringUtils.remove(selfUUID.toString(), '-');
        }

        return self ? MixinCapeManager.selfUUIDCached : uuid.toString();
    }

    @Redirect(method = "onPlayerTick", at = @At(value = "INVOKE", target = "Ljava/lang/String;replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;"), remap = false)
    @NotNull
    private final String replace$darkaddons(@NotNull final String text, @NotNull final CharSequence search, @NotNull final CharSequence replacement) {
        return StringUtils.remove(text, '-');
    }

    @Redirect(method = "onRenderPlayer", at = @At(value = "INVOKE", target = "Ljava/lang/String;replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;"), remap = false)
    @NotNull
    private final String replace$darkaddons$2(@NotNull final String text, @NotNull final CharSequence search, @NotNull final CharSequence replacement) {
        return StringUtils.remove(text, '-');
    }

    @Redirect(method = "setCape", at = @At(value = "INVOKE", target = "Ljava/lang/String;replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;"), remap = false)
    @NotNull
    private final String replace$darkaddons$3(@NotNull final String text, @NotNull final CharSequence search, @NotNull final CharSequence replacement) {
        return StringUtils.remove(text, '-');
    }

    @Redirect(method = "updateCapes", at = @At(value = "INVOKE", target = "Ljava/lang/String;replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;"), remap = false)
    @NotNull
    private final String replace$darkaddons$4(@NotNull final String text, @NotNull final CharSequence search, @NotNull final CharSequence replacement) {
        return StringUtils.remove(text, '-');
    }
}
