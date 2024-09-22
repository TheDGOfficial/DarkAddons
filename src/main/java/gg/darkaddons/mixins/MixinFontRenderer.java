package gg.darkaddons.mixins;

import net.minecraft.client.gui.FontRenderer;

import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.NotNull;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Locale;

@Mixin(value = FontRenderer.class, priority = 1_001)
final class MixinFontRenderer {
    private MixinFontRenderer() {
        super();
    }

    @Unique
    @Nullable
    private static String cachedLowerCaseText;

    @Inject(method = "renderStringAtPos", at = @At(value = "HEAD"))
    private final void renderStringAtPosStart$darkaddons(@NotNull final String text, final boolean shadow, @NotNull final CallbackInfo ci) {
        MixinFontRenderer.cachedLowerCaseText = text.toLowerCase(Locale.ROOT);
    }

    @Redirect(method = "renderStringAtPos", at = @At(value = "INVOKE", target = "Ljava/lang/String;toLowerCase(Ljava/util/Locale;)Ljava/lang/String;", remap = false))
    @NotNull
    private final String toLowerCase$darkaddons(@NotNull final String text, @NotNull final Locale locale) {
        // Optimization: Normally, the toLowerCase method is called inside the loop. We make it so we only call toLowerCase 1 time out of loop and save it to a variable, just use the variable when lower case is needed and then clear the variable at method exit points.

        // We also use Locale.ROOT instead of Locale.ENGLISH which should skip any localization so even more performance.
        return MixinFontRenderer.cachedLowerCaseText;
    }

    @Inject(method = "renderStringAtPos", at = @At(value = "TAIL"))
    private final void renderStringAtPosEnd$darkaddons(@NotNull final String text, final boolean shadow, @NotNull final CallbackInfo ci) {
        MixinFontRenderer.cachedLowerCaseText = null;
    }
}
