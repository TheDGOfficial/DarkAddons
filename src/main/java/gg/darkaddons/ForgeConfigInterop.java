package gg.darkaddons;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.fml.client.IModGuiFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

/**
 * Factory to open Vigilance config of the mod when the Config button is clicked in Mods page in Forge.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class ForgeConfigInterop implements IModGuiFactory {
    /**
     * Called by Forge. Do not call manually.
     */
    @SuppressWarnings("PublicConstructor")
    public ForgeConfigInterop() {
        super();
    }

    @Override
    public final void initialize(@SuppressWarnings("NullableProblems") @NotNull final Minecraft minecraft) {
        // do nothing
    }

    @Nullable
    @Override
    public final Class<? extends GuiScreen> mainConfigGuiClass() {
        return DummyScreen.class;
    }

    @Nullable
    @Override
    public final Set<IModGuiFactory.RuntimeOptionCategoryElement> runtimeGuiCategories() {
        return null;
    }

    @Nullable
    @Override
    public final IModGuiFactory.RuntimeOptionGuiHandler getHandlerFor(@SuppressWarnings("NullableProblems") @NotNull final IModGuiFactory.RuntimeOptionCategoryElement runtimeOptionCategoryElement) {
        return null;
    }
}
