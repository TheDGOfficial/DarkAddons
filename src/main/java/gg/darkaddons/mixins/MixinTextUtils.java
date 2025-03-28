package gg.darkaddons.mixins;

import gg.darkaddons.PublicUtils;

import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

@Pseudo
@Mixin(targets = "codes.biscuit.skyblockaddons.utils.TextUtils", priority = 999)
final class MixinTextUtils {
    private MixinTextUtils() {
        super();
    }

    @Unique
    private static Matcher magnitudeMatcher;

    @Redirect(method = "convertMagnitudes", at = @At(value = "INVOKE", target = "Ljava/util/regex/Pattern;matcher(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;"), remap = false)
    @NotNull
    private static final Matcher redirectMagnitudeMatcher$darkaddons(@NotNull final Pattern pattern, @NotNull final CharSequence text) {
        if (null != MixinTextUtils.magnitudeMatcher) {
            MixinTextUtils.magnitudeMatcher.reset(text);
        } else {
            MixinTextUtils.magnitudeMatcher = pattern.matcher(text);
        }

        return MixinTextUtils.magnitudeMatcher;
    }

    @Inject(method = "stripColor", at = @At("HEAD"), remap = false, cancellable = true)
    private static final void stripControlCodes$darkaddons(@NotNull final String text, @NotNull final CallbackInfoReturnable<String> cir) {
        cir.setReturnValue(PublicUtils.removeControlCodes(text));
    }
}
