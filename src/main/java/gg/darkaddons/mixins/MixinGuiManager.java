package gg.darkaddons.mixins;

import gg.essential.elementa.ElementaVersion;
import gg.darkaddons.DarkAddons;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "gg.skytils.skytilsmod.core.GuiManager", priority = 1_001)
final class MixinGuiManager {
    private MixinGuiManager() {
        super();
    }

    @Redirect(method = "<clinit>", remap = false, at = @At(value = "FIELD", target = "Lgg/essential/elementa/ElementaVersion;V2:Lgg/essential/elementa/ElementaVersion;", opcode = Opcodes.GETSTATIC, remap = false))
    @NotNull
    private static final ElementaVersion getElementaVersion$darkaddons() {
        return DarkAddons.ELEMENTA_VERSION;
    }
}
