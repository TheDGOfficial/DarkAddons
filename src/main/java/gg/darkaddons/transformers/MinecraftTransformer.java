package gg.darkaddons.transformers;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public final class MinecraftTransformer implements Transformer {
    public MinecraftTransformer() {
        super();
    }

    @Override
    public final String @NotNull [] getApplicableClasses() {
        return new String[]{"net.minecraft.client.Minecraft"};
    }

    @Override
    public final void transform(final int matchedClass, @NotNull final ClassNode classNode) {
        classNode.access |= Opcodes.ACC_FINAL;
        for (final var methodNode : classNode.methods) {
            if (!methodNode.name.endsWith("init>")) {
                methodNode.access |= Opcodes.ACC_FINAL;
            }
        }
    }
}
