package gg.darkaddons.transformers;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public final class NullStreamTransformer implements Transformer {
    public NullStreamTransformer() {
        super();
    }

    @Override
    public final String @NotNull [] getApplicableClasses() {
        return new String[]{"net.minecraft.client.stream.NullStream"};
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
