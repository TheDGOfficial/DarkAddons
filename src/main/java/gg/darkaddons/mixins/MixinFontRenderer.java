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

    @Redirect(method = "renderStringAtPos", at = @At(value = "INVOKE", target = "Ljava/lang/String;toLowerCase(Ljava/util/Locale;)Ljava/lang/String;", remap = false))
    @NotNull
    private final String toLowerCase$darkaddons(@NotNull final String text, @NotNull final Locale locale) {
        return text;
    }

    @Redirect(method = "renderStringAtPos", at = @At(value = "INVOKE", target = "Ljava/lang/String;charAt(I)C", ordinal = 1, remap = false))
    private final char charAt$darkaddons(@NotNull final String text, @NotNull final int index) {
        final var character = text.charAt(index);

        return (character >= 'A' && character <= 'Z') ? (char) (character + 32) : character;
    }
}
