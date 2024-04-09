package gg.darkaddons.mixins;

import com.google.common.collect.Ordering;
import gg.darkaddons.mixin.MixinUtils;
import net.minecraft.client.gui.GuiPlayerTabOverlay;
import net.minecraft.client.network.NetworkPlayerInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.util.IChatComponent;

/**
 * Accessor mixin to allow getting a private field in {@link GuiPlayerTabOverlay} class.
 */
@FunctionalInterface
@Mixin(value = GuiPlayerTabOverlay.class, priority = 999)
public interface IMixinGuiPlayerTabOverlay {
    /**
     * Gets the value of the footer, a private field in {@link GuiPlayerTabOverlay} class.
     *
     * @return The value of the footer, a private field in {@link GuiPlayerTabOverlay} class.
     */
    @Accessor
    @Nullable
    IChatComponent getFooter();

    @Accessor("field_175252_a")
    @NotNull
    static Ordering<NetworkPlayerInfo> getNetworkPlayerInfoOrdering() {
        throw MixinUtils.shadowFail();
    }
}
