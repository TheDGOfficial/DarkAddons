package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.GuiModList;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

/**
 * Do not make it package-private, accessed by Forge.
 */
@SuppressWarnings("WeakerAccess")
public final class DummyScreen extends GuiScreen {
    @NotNull
    private final GuiScreen parent;

    /**
     * Called by Forge.
     *
     * @param parentScreen The parent screen.
     */
    @SuppressWarnings("PublicConstructor")
    public DummyScreen(@NotNull final GuiScreen parentScreen) {
        super();

        this.parent = parentScreen;

        // TODO closing the config via ESC doesn't go back to Mods page; it closes all open guis
        // TODO /oneconfig has 2 DarkAddons entries likely because of this class's existence
        if (this.parent instanceof GuiModList) {
            DarkAddons.openConfigEditor(); // Must open with one tick delay, otherwise go back button on Vigilant SettingsGui goes back to DummyScreen instead of Mods page.
        } else { // Parent is likely one config menu as we don't use DummyScreen internally, it can either be called by Forge or OneConfig.
            // For future reference: OneConfig menu fully qualified class name: cc.polyfrost.oneconfig.gui.OneConfigGui
            DarkAddons.runOnceInNextTick("oneconfig_workaround_delay", DarkAddons::openConfigEditor); // Must open with two ticks delay as OneConfig opens this DummyScreen itself with one tick delay, it overrides this call and the game is left with a DummyScreen guiScreen
        }
    }

    @SuppressWarnings("PublicMethodNotExposedInInterface")
    @Override
    public final void handleKeyboardInput() throws IOException {
        // Provide a way to close the DummyScreen if the call to open the actual screen fails.
        if (Keyboard.getEventKeyState() && Keyboard.KEY_ESCAPE == Keyboard.getEventKey()) {
            Minecraft.getMinecraft().displayGuiScreen(this.parent);
            return;
        }
        super.handleKeyboardInput();
    }

    @Override
    public final String toString() {
        return "DummyScreen{" +
                "parent=" + this.parent +
                '}';
    }
}
