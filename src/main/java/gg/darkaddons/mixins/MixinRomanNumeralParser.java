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
@Mixin(targets = "codes.biscuit.skyblockaddons.utils.RomanNumeralParser", priority = 999)
final class MixinRomanNumeralParser {
    private MixinRomanNumeralParser() {
        super();
    }

    @Unique
    private static Matcher numeralMatcher;

    @Redirect(method = "replaceNumeralsWithIntegers", at = @At(value = "INVOKE", target = "Ljava/util/regex/Pattern;matcher(Ljava/lang/CharSequence;)Ljava/util/regex/Matcher;", ordinal = 0), remap = false)
    @NotNull
    private static final Matcher redirectNumeralMatcher$darkaddons(@NotNull final Pattern pattern, @NotNull final CharSequence text) {
        if (null != MixinRomanNumeralParser.numeralMatcher) {
            MixinRomanNumeralParser.numeralMatcher.reset(text);
        } else {
            MixinRomanNumeralParser.numeralMatcher = pattern.matcher(text);
        }

        return MixinRomanNumeralParser.numeralMatcher;
    }
}
