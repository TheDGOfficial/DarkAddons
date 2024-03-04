package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.mixin.MixinUtils;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.network.handshake.FMLHandshakeMessage;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.Map;

@Mixin(value = FMLHandshakeMessage.ModList.class, priority = 1_001)
final class MixinFMLHandshakeMessageModList {
    private MixinFMLHandshakeMessageModList() {
        super();
    }

    @Shadow(remap = false)
    @NotNull
    private final Map<String, String> modList() {
        throw MixinUtils.shadowFail();
    }

    @Inject(method = "<init>(Ljava/util/List;)V", remap = false, at = @At("RETURN"))
    private final void onInitLast$darkaddons(@NotNull final List<ModContainer> modList, @NotNull final CallbackInfo ci) {
        this.modList().remove(DarkAddons.MOD_ID);
    }
}
