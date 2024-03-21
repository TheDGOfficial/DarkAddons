package gg.darkaddons.coremod;

import gg.darkaddons.DarkAddonsTransformer;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.Supplier;

/**
 * Core mod plugin for {@link gg.darkaddons.DarkAddons}.
 */
@SuppressWarnings("WeakerAccess")
@IFMLLoadingPlugin.MCVersion("1.8.9")
@IFMLLoadingPlugin.Name("DarkAddons")
@IFMLLoadingPlugin.DependsOn("Skytils On Top")
@IFMLLoadingPlugin.SortingIndex(68)
@IFMLLoadingPlugin.TransformerExclusions({"gg.darkaddons.coremod.DarkAddonsLoadingPlugin", "gg.darkaddons.DarkAddonsTransformer", "gg.darkaddons.transformers", "gg.darkaddons.PublicUtils", "gg.darkaddons.Utils", "gg.darkaddons.TinyConfig", "gg.darkaddons.TinyConfigAccessor", "gg.skytils.skytilsmod.utils.ModChecker", "org.objenesis", "dev.falsehonesty.asmhelper", "kotlin"})
public final class DarkAddonsLoadingPlugin implements IFMLLoadingPlugin {
    @NotNull
    private static final Supplier<String[]> transformerClasses = () -> new String[]{DarkAddonsTransformer.class.getName()};

    /**
     * Constructor used by Forge, do not use manually.
     */
    @SuppressWarnings("PublicConstructor")
    public DarkAddonsLoadingPlugin() {
        super();
    }

    @Override
    @NotNull
    public final String[] getASMTransformerClass() {
        return DarkAddonsLoadingPlugin.transformerClasses.get();
    }

    @Override
    @Nullable
    public final String getModContainerClass() {
        return null;
    }

    @Override
    @Nullable
    public final String getSetupClass() {
        return null;
    }

    @Override
    public final void injectData(@SuppressWarnings("NullableProblems") @NotNull final Map<String, Object> map) {
        // do nothing
    }

    @Override
    @Nullable
    public final String getAccessTransformerClass() {
        return null;
    }
}
