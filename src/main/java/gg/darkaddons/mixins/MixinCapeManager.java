package gg.darkaddons.mixins;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "io.github.moulberry.notenoughupdates.cosmetics.CapeManager", priority = 999)
final class MixinCapeManager {
    private MixinCapeManager() {
        super();
    }

    @Redirect(method = "onTick", at = @At(value = "INVOKE", target = "Ljava/lang/String;replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;"), remap = false)
    @NotNull
    private final String replace$darkaddons(@NotNull final String text, @NotNull final CharSequence search, @NotNull final CharSequence replacement) {
        return StringUtils.replace(text, search.toString(), replacement.toString());
    }

    @Redirect(method = "onRenderPlayer", at = @At(value = "INVOKE", target = "Ljava/lang/String;replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;"), remap = false)
    @NotNull
    private final String replace$darkaddons$2(@NotNull final String text, @NotNull final CharSequence search, @NotNull final CharSequence replacement) {
        return StringUtils.replace(text, search.toString(), replacement.toString());
    }

    @Redirect(method = "onTickSlow", at = @At(value = "INVOKE", target = "Ljava/lang/String;replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;"), remap = false)
    @NotNull
    private static final String replace$darkaddons$3(@NotNull final String text, @NotNull final CharSequence search, @NotNull final CharSequence replacement) {
        return StringUtils.replace(text, search.toString(), replacement.toString());
    }

    @Redirect(method = "setCape", at = @At(value = "INVOKE", target = "Ljava/lang/String;replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;"), remap = false)
    @NotNull
    private final String replace$darkaddons$4(@NotNull final String text, @NotNull final CharSequence search, @NotNull final CharSequence replacement) {
        return StringUtils.replace(text, search.toString(), replacement.toString());
    }

    @Redirect(method = "updateCapes", at = @At(value = "INVOKE", target = "Ljava/lang/String;replace(Ljava/lang/CharSequence;Ljava/lang/CharSequence;)Ljava/lang/String;"), remap = false)
    @NotNull
    private final String replace$darkaddons$5(@NotNull final String text, @NotNull final CharSequence search, @NotNull final CharSequence replacement) {
        return StringUtils.replace(text, search.toString(), replacement.toString());
    }
}
