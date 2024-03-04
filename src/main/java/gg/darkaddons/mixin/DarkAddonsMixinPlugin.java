package gg.darkaddons.mixin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

/**
 * Mixin plugin for {@link gg.darkaddons.DarkAddons}
 */
@SuppressWarnings("WeakerAccess")
public final class DarkAddonsMixinPlugin implements IMixinConfigPlugin {
    /**
     * Called by Forge.
     */
    @SuppressWarnings("PublicConstructor")
    public DarkAddonsMixinPlugin() {
        super();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public final void onLoad(@NotNull final String mixinPackage) {
        // do nothing
    }

    @Override
    public final String getRefMapperConfig() {
        return "mixins.darkaddons.refmap.json";
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public final boolean shouldApplyMixin(@NotNull final String targetClassName, @NotNull final String mixinClassName) {
        return true;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public final void acceptTargets(@NotNull final Set<String> myTargets, @NotNull final Set<String> otherTargets) {
        // do nothing
    }

    @Override
    @Nullable
    public final List<String> getMixins() {
        return null;
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public final void preApply(@NotNull final String targetClassName, @NotNull final ClassNode targetClass, @NotNull final String mixinClassName, @NotNull final IMixinInfo mixinInfo) {
        // do nothing
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public final void postApply(@NotNull final String targetClassName, @NotNull final ClassNode targetClass, @NotNull final String mixinClassName, @NotNull final IMixinInfo mixinInfo) {
        // do nothing
    }
}
