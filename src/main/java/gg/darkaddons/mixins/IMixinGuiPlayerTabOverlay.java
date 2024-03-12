package gg.darkaddons.mixins;

import net.minecraft.client.gui.GuiPlayerTabOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.util.IChatComponent;

/**
 * Accessor mixin to allow getting a private field in {@link GuiPlayerTabOverlay} class.
 */
@Mixin(value = GuiPlayerTabOverlay.class, priority = 999)
public interface IMixinGuiPlayerTabOverlay {
    /**
     * Gets the value of the footer, a private field in {@link GuiPlayerTabOverlay} class.
     *
     * @return The value of the footer, a private field in {@link GuiPlayerTabOverlay} class.
     */
    @Accessor
    IChatComponent getFooter();
}
