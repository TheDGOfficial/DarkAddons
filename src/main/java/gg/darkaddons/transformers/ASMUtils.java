package gg.darkaddons.transformers;

import gg.darkaddons.profiler.impl.mappings.MethodMapping;
import net.minecraftforge.fml.common.asm.transformers.deobf.FMLDeobfuscatingRemapper;
import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.util.CheckClassAdapter;

public final class ASMUtils {
    private ASMUtils() {
        super();

        throw new UnsupportedOperationException("static class");
    }

    /*@NotNull
    public static final String remapToInternalClassName(@NotNull final String unobfuscatedClassName) {
        return FMLDeobfuscatingRemapper.INSTANCE.isRemappedClass(unobfuscatedClassName) ? FMLDeobfuscatingRemapper.INSTANCE.unmap(StringUtils.replace(unobfuscatedClassName, ".", "/")) : unobfuscatedClassName;
    }*/

    @NotNull
    static final String getUnobfuscatedMethodName(@NotNull final ClassNode owner, @NotNull final MethodNode method) {
        final var mapping = MethodMapping.MethodMappingsHolder.lookup(FMLDeobfuscatingRemapper.INSTANCE.mapMethodName(owner.name, method.name, method.desc));

        return null == mapping ? method.name : mapping.getDeobfName();
    }

    /*@NotNull
    public static final ClassNode readClass(@NotNull final Class<?> clazz) {
        final ClassReader classReader;
        final String clsPath = '/' + StringUtils.replace(ASMUtils.remapToInternalClassName(clazz.getName()), ".", "/") + ".class";

        try (final InputStream classStream = clazz.getClassLoader().getResourceAsStream(clsPath)) {
            if (null == classStream) {
                throw new IOException("Class " + clsPath + " not found");
            }
            classReader = new ClassReader(classStream);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }

        return ASMUtils.readClassNode(classReader);
    }*/

    @NotNull
    public static final ClassNode readClass(@NotNull final byte... bytes) {
        final var classReader = new ClassReader(bytes);

        return ASMUtils.readClassNode(classReader);
    }

    @NotNull
    private static final ClassNode readClassNode(@NotNull final ClassReader classReader) {
        final var classNode = new ClassNode();

        classReader.accept(classNode, ClassReader.SKIP_FRAMES);
        classNode.version = Opcodes.V1_8;

        return classNode;
    }

    /*public static final void dumpClass(final byte[] bytes, @NotNull final String suffix) {
        final ClassNode classNode = ASMUtils.readClass(bytes);
        ASMUtils.deobfMethodNames(classNode);

        final byte[] contents = ASMUtils.writeClass(classNode);

        final File file = new File(new File(new File("config", "darkaddons"), "classdumps"), StringUtils.replace(classNode.name, ".", "/") + suffix + ".class");

        file.getParentFile().mkdirs();

        try {
            if (file.exists()) {
                Files.delete(file.toPath());
            }

            if (!file.createNewFile()) {
                throw new IOException("cannot create new file");
            }

            Files.write(file.toPath(), contents);
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static final void deobfMethodNames(@NotNull final ClassNode classNode) {
        for (final MethodNode methodNode : classNode.methods) {
            methodNode.name = ASMUtils.getUnobfuscatedMethodName(classNode, methodNode);
        }
    }*/

    public static final byte @NotNull [] writeClass(@NotNull final ClassNode classNode) {
        final var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        classNode.accept(new CheckClassAdapter(classWriter, true));

        return classWriter.toByteArray();
    }
}
