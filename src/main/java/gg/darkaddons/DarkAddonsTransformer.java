package gg.darkaddons;

import net.minecraft.launchwrapper.IClassTransformer;
import gg.darkaddons.transformers.ASMUtils;
import gg.darkaddons.transformers.BlockPosTransformer;
import gg.darkaddons.transformers.NullStreamTransformer;
import gg.darkaddons.transformers.Transformer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Supplier;

/**
 * Transformer for {@link DarkAddons} mod.
 * Do not make it package-private.
 */
@SuppressWarnings({"WeakerAccess", "ClassNamePrefixedWithPackageName"})
public final class DarkAddonsTransformer implements IClassTransformer {
    private static final @NotNull ConcurrentLinkedQueue<Transformer> transformers = new ConcurrentLinkedQueue<>();

    static {
        // This can't be a Config option because transformers are ran before Minecraft classes are loaded, and Config class depends on GuiScreen because of it's Vigilant#gui method, it will give NoClassDefFoundError otherwise.
        if (TinyConfig.getBoolean("blockPosOptimizer", false)) {
            DarkAddonsTransformer.transformers.add(new BlockPosTransformer());
        }
        if (TinyConfig.getBoolean("nullStreamOptimizer", false)) {
            DarkAddonsTransformer.transformers.add(new NullStreamTransformer());
        }
    }

    /**
     * Called by forge {@link net.minecraftforge.fml.common.asm.ASMTransformerWrapper} via ASM,
     * do not make it package-private.
     */
    @SuppressWarnings("PublicConstructor")
    public DarkAddonsTransformer() {
        super();
    }

    private static final byte @NotNull [] transformCatching(final byte @NotNull [] defaultValue, @NotNull final Supplier<byte[]> transformer) {
        try {
            return transformer.get();
        } catch (final Throwable t) {
            PublicUtils.printStackTrace(t);
            return defaultValue;
        }
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public final byte @Nullable [] transform(@Nullable final String name, @Nullable final String transformedName, final byte @Nullable [] basicClass) {
        return null == basicClass ? null : DarkAddonsTransformer.transformCatching(basicClass, () -> {
            var postTransform = basicClass;

            for (final var transformer : DarkAddonsTransformer.transformers) {
                final var applicableClasses = transformer.getApplicableClasses();
                for (int i = 0, applicableClassesLength = applicableClasses.length; i < applicableClassesLength; i++) {
                    final var candidate = applicableClasses[i];
                    if (candidate.equals(transformedName)) {
                        final var classNode = ASMUtils.readClass(postTransform);
                        //ASMUtils.dumpClass(ASMUtils.writeClass(classNode), "before");

                        transformer.transform(i, classNode);

                        postTransform = ASMUtils.writeClass(classNode);
                        //ASMUtils.dumpClass(postTransform, "after");

                        break;
                    }
                }
            }

            return postTransform;
        });
    }
}
