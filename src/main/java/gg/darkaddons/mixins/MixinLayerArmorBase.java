package gg.darkaddons.mixins;

import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LayerArmorBase.class)
final class MixinLayerArmorBase {
    private MixinLayerArmorBase() {
        super();
    }

    @Redirect(method = "getArmorResource", at = @At(value = "INVOKE", target = "Ljava/lang/String;format(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;"), remap = false)
    @NotNull
    private final String stringFormat$darkaddons(@NotNull final String text, @NotNull final Object... values) {
        final var len = values.length;
        return switch (len) {
            case 1 -> StringUtils.replaceOnce(text, "%s", values[0].toString());
            case 4 ->
                StringUtils.replaceOnce(StringUtils.replaceOnce(StringUtils.replaceOnce(StringUtils.replaceOnce(text, "%s", values[0].toString()), "%s", values[1].toString()), "%d", values[2].toString()), "%s", values[3].toString());
            case 3 ->
                StringUtils.replaceOnce(StringUtils.replaceOnce(StringUtils.replaceOnce(text, "%s", values[0].toString()), "%d", values[1].toString()), "%s", values[2].toString());
            default -> throw new IllegalStateException("unexpected len of " + len);
        };
    }
}
