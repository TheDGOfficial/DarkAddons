package gg.darkaddons.transformers;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;

import java.util.Iterator;

public final class BlockPosTransformer implements Transformer {
    public BlockPosTransformer() {
        super();
    }

    @Override
    public final String @NotNull [] getApplicableClasses() {
        return new String[]{"net.minecraft.util.BlockPos", "net.minecraft.util.BlockPos$MutableBlockPos", "net.optifine.BlockPosM", "club.sk1er.patcher.mixins.performance.BlockPosMixin_ReduceAllocations", "net.minecraft.util.BlockPos$1", "net.minecraft.util.BlockPos$1$1", "net.minecraft.util.BlockPos$2", "net.minecraft.util.BlockPos$2$1", "net.optifine.BlockPosM$1", "net.optifine.BlockPosM$1$1", "net.minecraft.world.Teleporter$PortalPosition", "net.minecraft.world.gen.feature.WorldGenBigTree$FoliageCoordinates", "net.minecraft.world.Teleporter"};
    }

    private static final void transformClass(final int matchedClass, @NotNull final ClassNode classNode) {
        if (0 == (classNode.access & Opcodes.ACC_FINAL) && (2 == matchedClass || 5 == matchedClass || 7 == matchedClass || 9 == matchedClass || 10 == matchedClass || 11 == matchedClass)) {
            classNode.access |= Opcodes.ACC_FINAL;
        }

        if (3 == matchedClass || 10 == matchedClass) {
            classNode.access &= ~Opcodes.ACC_PUBLIC;
        }
    }

    private static final void transformConstructorCall(@NotNull final MethodNode methodNode) {
        for (final Iterator<AbstractInsnNode> insnNodeIterator = methodNode.instructions.iterator(); insnNodeIterator.hasNext();) {
            final var insnNode = insnNodeIterator.next();

            if (insnNode instanceof final MethodInsnNode methodInsnNode && "<init>".equals(methodInsnNode.name) && "adu$a".equals(methodInsnNode.owner)) {
                methodInsnNode.desc = StringUtils.remove(methodInsnNode.desc, "Ladu;");
            }

            if (insnNode instanceof final VarInsnNode varInsnNode && 0 == varInsnNode.var) {
                final var next = varInsnNode.getNext();

                if (next instanceof VarInsnNode && 9 == ((VarInsnNode) next).var) {
                    insnNodeIterator.remove();
                }
            }
        }
    }

    private static final void transformConstructor(@NotNull final MethodNode methodNode) {
        methodNode.access &= ~Opcodes.ACC_PUBLIC;
        methodNode.desc = StringUtils.remove(methodNode.desc, "Ladu;");

        for (final Iterator<AbstractInsnNode> insnNodeIterator = methodNode.instructions.iterator(); insnNodeIterator.hasNext();) {
            final var insnNode = insnNodeIterator.next();

            if (insnNode instanceof VarInsnNode && 0 == ((VarInsnNode) insnNode).var) {
                final var nextInsnNode = insnNode.getNext();
                if (nextInsnNode instanceof VarInsnNode && 1 == ((VarInsnNode) nextInsnNode).var) {
                    insnNodeIterator.remove();
                }
            }
        }

        for (final Iterator<AbstractInsnNode> insnNodeIterator = methodNode.instructions.iterator(); insnNodeIterator.hasNext();) {
            final var insnNode = insnNodeIterator.next();

            if (insnNode instanceof VarInsnNode && 1 == ((VarInsnNode) insnNode).var) {
                insnNodeIterator.remove();
            }

            if (insnNode instanceof FieldInsnNode && "d".equals(((FieldInsnNode) insnNode).name)) {
                insnNodeIterator.remove();
            }
        }

        for (final var localVariableNode : methodNode.localVariables) {
            if (0 != localVariableNode.index) {
                --localVariableNode.index;
            }
        }

        for (final Iterator<AbstractInsnNode> insnNodeIterator = methodNode.instructions.iterator(); insnNodeIterator.hasNext();) {
            final var insnNode = insnNodeIterator.next();

            if (insnNode instanceof final VarInsnNode varInsnNode && 0 != varInsnNode.var) {
                --varInsnNode.var;
            }
        }
    }

    private static final void transformMethods(final int matchedClass, @NotNull final ClassNode classNode) {
        for (final var methodNode : classNode.methods) {
            final var deobfName = ASMUtils.getUnobfuscatedMethodName(classNode, methodNode);
            if (0 == (methodNode.access & Opcodes.ACC_FINAL) && (10 != matchedClass && 11 != matchedClass && 12 != matchedClass && BlockPosTransformer.shouldMakeFinal(deobfName, matchedClass) || 0 == matchedClass && "crossProduct".equals(deobfName) && "(Ldf;)Lcj;".equals(methodNode.desc))) {
                methodNode.access |= Opcodes.ACC_FINAL;
            }

            if (3 == matchedClass && "<init>".equals(methodNode.name)) {
                // Note: Ideally, we should also turn invokevirtual opcodes of calls to this method that we are just making private into invokespecial calls, but since matchedClass 3 is a Mixin class and Mixin either doesn't call the constructor or use reflection, it doesn't matter in this specific case.
                methodNode.access &= ~Opcodes.ACC_PUBLIC;
                methodNode.access |= Opcodes.ACC_PRIVATE;
            }

            if (11 == matchedClass && !methodNode.name.endsWith("init>")) {
                methodNode.access &= ~Opcodes.ACC_PUBLIC;
                methodNode.access |= Opcodes.ACC_FINAL;
            }

            if (11 == matchedClass && "<init>".equals(methodNode.name)) {
                methodNode.access &= ~Opcodes.ACC_PUBLIC;
            }

            if (12 == matchedClass && "placeInExistingPortal".equals(deobfName)) {
                BlockPosTransformer.transformConstructorCall(methodNode);
            }

            if (10 == matchedClass && "<init>".equals(methodNode.name)) {
                BlockPosTransformer.transformConstructor(methodNode);
            }
        }
    }

    private static final void transformFields(final int matchedClass, @NotNull final ClassNode classNode) {
        for (final var fieldNode : classNode.fields) {
            if ("level".equals(fieldNode.name) && 0 == (fieldNode.access & Opcodes.ACC_FINAL) && 2 == matchedClass) {
                fieldNode.access |= Opcodes.ACC_FINAL;
            }

            if ("this$0".equals(fieldNode.name) && (5 == matchedClass || 7 == matchedClass || 9 == matchedClass)) {
                fieldNode.access |= Opcodes.ACC_PRIVATE;
            }

            if ("c".equals(fieldNode.name) && 10 == matchedClass) {
                fieldNode.access &= ~Opcodes.ACC_PUBLIC;
            }

            if ("d".equals(fieldNode.name) && 10 == matchedClass) {
                fieldNode.access |= Opcodes.ACC_PRIVATE;
            }
        }

        if (10 == matchedClass) {
            classNode.fields.removeIf((@NotNull final FieldNode fieldNode) -> "d".equals(fieldNode.name));
        }
    }

    @Override
    public final void transform(final int matchedClass, @NotNull final ClassNode classNode) {
        BlockPosTransformer.transformClass(matchedClass, classNode);
        BlockPosTransformer.transformMethods(matchedClass, classNode);
        BlockPosTransformer.transformFields(matchedClass, classNode);
    }

    private static final boolean regularFinalMethods(@NotNull final String deobfName) {
        // offset is overridden by net.optifine.BlockPosM
        return "getAllInBoxMutable".equals(deobfName) || "getAllInBox".equals(deobfName) || "fromLong".equals(deobfName) || "toLong".equals(deobfName) || "east".equals(deobfName) || "west".equals(deobfName) || "south".equals(deobfName) || "north".equals(deobfName) || "down".equals(deobfName) || "up".equals(deobfName) || "subtract".equals(deobfName) || "add".equals(deobfName) || "compareTo".equals(deobfName);
    }

    private static final boolean mutableFinalMethods(@NotNull final String deobfName) {
        return "getX".equals(deobfName) || "getY".equals(deobfName) || "getZ".equals(deobfName) || "set".equals(deobfName) || "getImmutable".equals(deobfName) || "a".equals(deobfName) || "b".equals(deobfName) || "c".equals(deobfName) || "crossProduct".equals(deobfName);
    }

    private static final boolean optifineMutableFinalMethods(@NotNull final String deobfName) {
        return "getAllInBoxMutable".equals(deobfName) || "toImmutable".equals(deobfName) || "update".equals(deobfName) || "offset".equals(deobfName) || "offsetMutable".equals(deobfName) || "set".equals(deobfName) || "setXyz".equals(deobfName) || "getX".equals(deobfName) || "getY".equals(deobfName) || "getZ".equals(deobfName) || "crossProduct".equals(deobfName);
    }

    private static final boolean iteratorFinalMethods(@NotNull final String deobfName) {
        return "iterator".equals(deobfName);
    }

    private static final boolean computeNextMethods(@NotNull final String deobfName) {
        return deobfName.startsWith("computeNext") || "a".equals(deobfName);
    }

    @NotNull
    private static final IllegalStateException unexpectedMatchedClass(final int matchedClass) {
        return new IllegalStateException("unexpected matchedClass of " + matchedClass);
    }

    private static final boolean shouldMakeFinal(@NotNull final String deobfName, final int matchedClass) {
        return switch (matchedClass) {
            case 0, 3 -> BlockPosTransformer.regularFinalMethods(deobfName);
            case 1 ->
                // index starts at 0, so this checks if it's the second value in the array returned by #getApplicableClasses
                    BlockPosTransformer.mutableFinalMethods(deobfName);
            case 2 -> BlockPosTransformer.optifineMutableFinalMethods(deobfName);
            case 4, 8, 6 -> BlockPosTransformer.iteratorFinalMethods(deobfName);
            case 5, 7, 9 -> BlockPosTransformer.computeNextMethods(deobfName);
            default -> throw BlockPosTransformer.unexpectedMatchedClass(matchedClass);
        };
    }
}
