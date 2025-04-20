package gg.darkaddons.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Invoker and accessor mixin to allow invoking and accessing some private methods/fields in {@link Minecraft} class.
 */
@Mixin(value = Minecraft.class, priority = 999)
public interface IMixinMinecraft {
    /**
     * Called when left mouse button is pressed (normally).
     * When called manually, it will simulate a left mouse click.
     */
    @Invoker
    void callClickMouse();

    /**
     * Called when right mouse button is pressed (normally).
     * When called manually, it will simulate a right mouse click.
     */
    @Invoker
    void callRightClickMouse();

    /**
     * Gets the value of the right click delay timer, a private field in {@link Minecraft} class.
     *
     * @return The value of the right click delay timer, a private field in {@link Minecraft} class.
     */
    @Accessor
    int getRightClickDelayTimer();

    /**
     * Gets the {@link Timer}, stored in a private field in {@link Minecraft} class.
     *
     * @return The {@link Timer}, stored in a private field in {@link Minecraft} class.
     */
    @Accessor
    Timer getTimer();
}
