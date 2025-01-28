package gg.darkaddons.mixins;

import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
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
            case 4 -> values[0].toString() + ":textures/models/armor/" + values[1].toString() + "_layer_" + values[2].toString() + values[3].toString() + ".png";
            case 1 -> '_' + values[0].toString();
            case 3 -> "textures/models/armor/" + values[0].toString() + "_layer_" + values[1].toString() + values[2].toString() + ".png";
            default -> throw new IllegalStateException("unexpected len of " + len);
        };
    }
}
