package gg.darkaddons;

import com.google.common.collect.ImmutableSet;
import com.google.common.reflect.ClassPath;

import org.apache.commons.lang3.StringUtils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.util.CheckClassAdapter;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.DataInputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javassist.bytecode.ClassFile;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.CodeAttribute;

final class MarkCompilerGeneratedMethodsFinal {
    @NotNull
    private static final String FINAL = "final";
    @NotNull
    private static final String VALUES = "values";
    @NotNull
    private static final String VALUE_OF = "valueOf";
    @NotNull
    private static final String TRANSFORMED_CLASSES = "transformed_classes";
    @NotNull
    private static final String BUILD = "build";

    @Nullable
    private static final Class<? extends Annotation> bridgeAnnotation;
    @Nullable
    private static final Class<? extends Annotation> packagePrivateAnnotation;
    @Nullable
    private static final Class<? extends Annotation> privateAnnotation;
    @Nullable
    private static final Class<? extends Annotation> syntheticAnnotation;
    @Nullable
    private static final Class<? extends Annotation> nameAnnotation;

    private static final boolean ANNOTATIONS_PRESENT;

    static {
        bridgeAnnotation = MarkCompilerGeneratedMethodsFinal.getAnnotation("Bridge");
        packagePrivateAnnotation = MarkCompilerGeneratedMethodsFinal.getAnnotation("PackagePrivate");
        privateAnnotation = MarkCompilerGeneratedMethodsFinal.getAnnotation("Private");
        syntheticAnnotation = MarkCompilerGeneratedMethodsFinal.getAnnotation("Synthetic");
        nameAnnotation = MarkCompilerGeneratedMethodsFinal.getAnnotation("Name");

        ANNOTATIONS_PRESENT = null != MarkCompilerGeneratedMethodsFinal.bridgeAnnotation && null != MarkCompilerGeneratedMethodsFinal.packagePrivateAnnotation && null != MarkCompilerGeneratedMethodsFinal.privateAnnotation && null != MarkCompilerGeneratedMethodsFinal.syntheticAnnotation && null != MarkCompilerGeneratedMethodsFinal.nameAnnotation;
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static final Class<? extends Annotation> getAnnotation(@NotNull final String annotationName) {
        try {
            return (Class<? extends Annotation>) Class.forName("gg.darkaddons.annotations.bytecode." + annotationName);
        } catch (final ClassNotFoundException ignored) {
            return null;
        }
    }

    private MarkCompilerGeneratedMethodsFinal() {
        super();

        throw new UnsupportedOperationException("static class");
    }

    private static final void info(@NotNull final String text) {
        MarkCompilerGeneratedMethodsFinal.println(text, false);
    }

    private static final void err(@NotNull final String desc) {
        MarkCompilerGeneratedMethodsFinal.println(desc, true);
    }

    private static final void println(@NotNull final String text, final boolean err) {
        (err ? System.err : System.out).println(text);
    }

    public static final void main(@NotNull final String... args) throws IOException {
        MarkCompilerGeneratedMethodsFinal.main0(args);
    }

    private static final void main0(@NotNull final String... args) throws IOException {
        if (0 < args.length) {
            if ("postRun".equals(args[0])) {
                try {
                    //System.out.println("Ensuring classses to transform is zero after already transforming...");
                    final var classes = MarkCompilerGeneratedMethodsFinal.findClassesToTransform(true);
                    MarkCompilerGeneratedMethodsFinal.err("ERROR: Optimizations couldn't be applied. Wrong classpath? Optimizer error? Gradle build failed?");
                    MarkCompilerGeneratedMethodsFinal.err("ERROR: The following classes weren't able to be fully optimized:");
                    for (final var clazzName : classes) {
                        MarkCompilerGeneratedMethodsFinal.err(clazzName);
                    }
                } catch (final IOException e) {
                    if (!"found nothing to transform".equals(e.getMessage())) {
                        throw e;
                    }
                }
                //System.out.println("Collecting classes we didn't transform because they can't be transformed...");
                final var classesReversed = MarkCompilerGeneratedMethodsFinal.findClassesToTransform(false, true);
                //System.out.println("Ensuring the collected classes can't be transformed...");
                MarkCompilerGeneratedMethodsFinal.transformClasses(classesReversed, true);
                //System.out.println("Checking method sizes...");
                MarkCompilerGeneratedMethodsFinal.checkMethodSizes();
                return;
            }
        }
        if (!MarkCompilerGeneratedMethodsFinal.ANNOTATIONS_PRESENT) {
            MarkCompilerGeneratedMethodsFinal.err("ERROR: Can't find annotations on the classpath, things might not work. Proceeding anyway.");
        }
        MarkCompilerGeneratedMethodsFinal.markCompilerGeneratedMethodsFinal();
    }

    private static final boolean compilerGenerated(@NotNull final String methodDefinition) {
        return methodDefinition.contains("bridge") || methodDefinition.contains("$") && !methodDefinition.endsWith("$darkaddons");
    }

    private static final boolean shouldMakeFinal(@NotNull final String methodDefinition) {
        return !methodDefinition.contains(MarkCompilerGeneratedMethodsFinal.FINAL) && MarkCompilerGeneratedMethodsFinal.compilerGenerated(methodDefinition);
    }

    private static final boolean isNonFinalStaticMethod(@NotNull final String methodDefinition) {
        return !methodDefinition.contains(MarkCompilerGeneratedMethodsFinal.FINAL) && !MarkCompilerGeneratedMethodsFinal.compilerGenerated(methodDefinition) && methodDefinition.contains("static");
    }

    private static final boolean isNonFinalPrivateMethod(@NotNull final String methodDefinition) {
        return !methodDefinition.contains(MarkCompilerGeneratedMethodsFinal.FINAL) && !MarkCompilerGeneratedMethodsFinal.compilerGenerated(methodDefinition) && methodDefinition.contains("private");
    }

    @NotNull
    private static final ArrayList<String> findClassesToTransform() throws IOException {
        return MarkCompilerGeneratedMethodsFinal.findClassesToTransform(false, false);
    }

    @NotNull
    private static final ArrayList<String> findClassesToTransform(final boolean verbose) throws IOException {
        return MarkCompilerGeneratedMethodsFinal.findClassesToTransform(verbose, false);
    }

    @NotNull
    private static final ImmutableSet<ClassPath.ClassInfo> getAllClasses() throws IOException {
        final var classInfos = ClassPath.from(Thread.currentThread().getContextClassLoader()).getAllClasses();
        //noinspection IfCanBeAssertion
        if (classInfos.isEmpty()) {
            throw new IOException("no classes found");
        }
        return classInfos;
    }

    private static final boolean isModClass(final ClassPath.ClassInfo info) {
        return (info.getPackageName().startsWith("gg.darkaddons") || info.getPackageName().startsWith("darkaddons")) && !info.getName().contains(MarkCompilerGeneratedMethodsFinal.class.getName());
    }

    @NotNull
    private static final ArrayList<String> findClassesToTransform(final boolean verbose, final boolean reversedLogic) throws IOException {
        var willTransformAtLeastOne = false;
        var notFoundAmount = 0;
        final var classesToProc = new ArrayList<String>(100);
        for (final var info : MarkCompilerGeneratedMethodsFinal.getAllClasses()) {
            if (!MarkCompilerGeneratedMethodsFinal.isModClass(info)) {
                continue;
            }
            if (!info.getSimpleName().startsWith("Test") && !MarkCompilerGeneratedMethodsFinal.class.getSimpleName().equals(info.getSimpleName())) {
                //System.out.println("Found class: " + info.getName());
                try {
                    final var clazz = info.load();
                    final var classMod = clazz.getModifiers();
                    final var synthetic = clazz.isSynthetic();
                    var shouldProc = false;
                    for (final var method : clazz.getDeclaredMethods()) {
                        final var mod = method.getModifiers();
                        final var name = method.getName();
                        final var methodDefinition = MarkCompilerGeneratedMethodsFinal.getMethodDefinition(name, mod);

                        if (!MarkCompilerGeneratedMethodsFinal.VALUES.equals(name) && !MarkCompilerGeneratedMethodsFinal.VALUE_OF.equals(name) && MarkCompilerGeneratedMethodsFinal.isNonFinalStaticMethod(methodDefinition) && !clazz.isInterface()) { // Interfaces cant have final methods
                            MarkCompilerGeneratedMethodsFinal.err("Non-final static method: " + methodDefinition + " in class " + clazz.getName());
                        }

                        if (MarkCompilerGeneratedMethodsFinal.isNonFinalPrivateMethod(methodDefinition) && !clazz.isInterface()) { // Interfaces cant have final methods
                            MarkCompilerGeneratedMethodsFinal.err("Non-final private method: " + methodDefinition + " in class " + clazz.getName());
                        }

                        if ((MarkCompilerGeneratedMethodsFinal.shouldMakeFinal(methodDefinition) && !"invoke".equals(method.getName()) || (MarkCompilerGeneratedMethodsFinal.VALUES.equals(name) || MarkCompilerGeneratedMethodsFinal.VALUE_OF.equals(name)) && !Modifier.isFinal(mod)) && !clazz.isInterface() && !Modifier.isAbstract(mod)) { // Interfaces cant have final methods and abstract methods cant be final
                            if (verbose) {
                                MarkCompilerGeneratedMethodsFinal.info("verbose: Optimizing method: " + methodDefinition);
                            }
                            shouldProc = true;
                            break;
                        }

                        /* && !method.getName().equals(((Name) method.getAnnotation(nameAnnotation)).value())*/
                        if (MarkCompilerGeneratedMethodsFinal.ANNOTATIONS_PRESENT && (method.isAnnotationPresent(MarkCompilerGeneratedMethodsFinal.bridgeAnnotation) && !methodDefinition.contains("bridge") || method.isAnnotationPresent(MarkCompilerGeneratedMethodsFinal.syntheticAnnotation) && !method.isSynthetic() || method.isAnnotationPresent(MarkCompilerGeneratedMethodsFinal.privateAnnotation) && !methodDefinition.contains("private") || method.isAnnotationPresent(MarkCompilerGeneratedMethodsFinal.packagePrivateAnnotation) && methodDefinition.contains("public") || method.isAnnotationPresent(MarkCompilerGeneratedMethodsFinal.nameAnnotation))) {
                            if (verbose) {
                                MarkCompilerGeneratedMethodsFinal.info("verbose: Using custom annotation for method: " + methodDefinition);
                            }
                            shouldProc = true;
                            break;
                        }

                        if ("darkaddons.installer.DarkAddonsInstaller$OperatingSystem".equals(clazz.getName()) && method.getName().startsWith("values$") && Modifier.isPublic(method.getModifiers())) {
                            shouldProc = true;
                            break;
                        }

                        if ("gg.darkaddons.mixins.MixinItemModelGenerator".equals(clazz.getName()) && method.getName().startsWith("func_178397_a$")) {
                            shouldProc = true;
                            break;
                        }

                        if ("gg.darkaddons.mixins.MixinTabListUtils".equals(clazz.getName()) && method.getName().startsWith("sortedCopy$darkaddons$")) {
                            shouldProc = true;
                            break;
                        }

                        if ("gg.darkaddons.mixins.MixinTabListParser".equals(clazz.getName()) && method.getName().startsWith("sortedCopy$darkaddons$")) {
                            shouldProc = true;
                            break;
                        }
                    }
                    if ((synthetic || clazz.getName().contains("Kt")) && (synthetic && !verbose || Modifier.isPublic(classMod))) {
                        if (verbose) {
                            MarkCompilerGeneratedMethodsFinal.info("verbose: Optimizing class: " + Modifier.toString(classMod) + ' ' + clazz.getName());
                        }
                        shouldProc = true;
                    }
                    if (clazz.getName().contains("Kt")) {
                        for (final var field : clazz.getFields()) {
                            if (Modifier.isPublic(field.getModifiers())) {
                                //System.out.println(clazz.getName() + ": " + Modifier.toString(field.getModifiers()) + " " + field.getName());
                                shouldProc = true;
                                break;
                            }
                        }
                    }
                    if (shouldProc) {
                        if (!reversedLogic) {
                            classesToProc.add(clazz.getName());
                        }
                        willTransformAtLeastOne = true;
                        //} else if (!verbose && !reversedLogic) {
                        //System.out.println("Will not process class " + clazz.getName());
                    } else if (reversedLogic) {
                        classesToProc.add(clazz.getName());
                    }
                } catch (final NoClassDefFoundError ncdfe) {
                    if (verbose) {
                        ++notFoundAmount;
                        MarkCompilerGeneratedMethodsFinal.err("Can't find " + ncdfe.getMessage() + ", please check class path!");
                    }
                }
            }
        }
        if (0 != notFoundAmount) {
            MarkCompilerGeneratedMethodsFinal.err("Couldn't find " + notFoundAmount + " classes, please check class path.");
        }
        //noinspection IfCanBeAssertion
        if (!willTransformAtLeastOne) {
            throw new IOException("found nothing to transform");
        }
        return classesToProc;
    }

    private static final void deleteAllFilesInDir(@NotNull final File dir) throws IOException {
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("argument is not a directory");
            }

            for (final var file : dir.listFiles()) {
                if (file.isDirectory()) {
                    MarkCompilerGeneratedMethodsFinal.deleteAllFilesInDir(file);
                }
                Files.delete(file.toPath());
            }
        }
    }

    private static final void transformClasses(@SuppressWarnings({"CollectionDeclaredAsConcreteClass", "TypeMayBeWeakened"}) @NotNull final ArrayList<String> classes, final boolean checkRun) throws IOException {
        final var transformedClassesDir = new File(new File(MarkCompilerGeneratedMethodsFinal.BUILD), MarkCompilerGeneratedMethodsFinal.TRANSFORMED_CLASSES);
        if (!checkRun) {
            MarkCompilerGeneratedMethodsFinal.deleteAllFilesInDir(transformedClassesDir);
        }

        for (final var clazzName : classes) {
            final var classReader = new ClassReader(clazzName);
            final var classNode = new ClassNode();
            classReader.accept(classNode, ClassReader.SKIP_FRAMES);
            classNode.version = Opcodes.V1_8;

            final var className = classNode.name;
            final var classModStr = Modifier.toString(classNode.access);
            if (!classModStr.contains(MarkCompilerGeneratedMethodsFinal.FINAL) && StringUtils.replace(classModStr, "synchronized", "synthetic").contains("synthetic") && !Modifier.isAbstract(classNode.access) && 0 == (classNode.access & Opcodes.ACC_ENUM)) { // Making enums final gives errors at runtime if enum variants override non-final methods specified in the enum
                classNode.access |= Opcodes.ACC_FINAL;
                if (checkRun) {
                    throw new IllegalStateException("class " + className + " should've been on the list of classes to optimize, but it was not (class info: " + classModStr + ' ' + className + ')');
                }
            }

            if (className.contains("Kt") && classModStr.contains("public")) {
                classNode.access &= ~Opcodes.ACC_PUBLIC;
                if (checkRun) {
                    throw new IllegalStateException("class " + className + " should've been on the list of classes to optimize, but it was not (class info: " + classModStr + ' ' + className + ')');
                }
            }

            for (final var mn : classNode.methods) {
                final var methodName = mn.name;
                final Supplier<String> getDefinition = () -> MarkCompilerGeneratedMethodsFinal.getMethodDefinition(methodName, mn.access);
                final var definition = getDefinition.get();
                if (0 == (classNode.access & Opcodes.ACC_INTERFACE) && 0 == (mn.access & Opcodes.ACC_ABSTRACT) && !definition.contains(MarkCompilerGeneratedMethodsFinal.FINAL) && (MarkCompilerGeneratedMethodsFinal.shouldMakeFinal(definition) || MarkCompilerGeneratedMethodsFinal.VALUES.equals(methodName) || MarkCompilerGeneratedMethodsFinal.VALUE_OF.equals(methodName))) { // Interfaces can't have final methods and abstract methods can't be final
                    //System.out.println("Old signature: " + definition);
                    mn.access |= Opcodes.ACC_FINAL;
                    if (checkRun) {
                        throw new IllegalStateException("class " + className + " should've been on the list of classes to optimize, but it was not (method info: " + definition + ')');
                    }

                    //final String newDefinition = getDefinition.get();
                    //System.out.println("New signature: " + newDefinition);
                }

                if (0 != (mn.access & Opcodes.ACC_PUBLIC) && className.contains("Kt") && !"invoke".equals(methodName) && !methodName.contains("<")) {
                    mn.access &= ~Opcodes.ACC_PUBLIC;
                    if (checkRun) {
                        throw new IllegalStateException("class " + className + " should've been on the list of classes to optimize, but it was not (method info: " + definition + ')');
                    }
                }

                if (0 != (mn.access & Opcodes.ACC_PUBLIC) && "darkaddons/installer/DarkAddonsInstaller$OperatingSystem".equals(className) && mn.name.startsWith("values$")) {
                    mn.access &= ~Opcodes.ACC_PUBLIC;
                    if (checkRun) {
                        throw new IllegalStateException("class " + className + " should've been on the list of classes to optimize, but it was not (method info: " + definition + ')');
                    }
                }

                if ("gg/darkaddons/Utils".equals(className) && mn.name.equals("getHeldItemStack")) {
                    for (final Iterator<AbstractInsnNode> insnNodeIterator = mn.instructions.iterator(); insnNodeIterator.hasNext();) {
                        final var insnNode = insnNodeIterator.next();

                        if (insnNode instanceof final MethodInsnNode methodInsnNode) {
                            methodInsnNode.owner = StringUtils.replace(methodInsnNode.owner, "net/minecraft/entity/EntityLivingBase", "net/minecraft/client/entity/EntityPlayerSP");
                        }
                    }
                }

                if ("gg/darkaddons/mixins/MixinItemModelGenerator".equals(className) && mn.name.startsWith("func_178397_a$")) {
                    mn.name = StringUtils.substringBefore(mn.name, "$");
                    mn.desc = StringUtils.replace(mn.desc, "ArrayList", "List");
                }

                if ("gg/darkaddons/mixins/MixinTabListUtils".equals(className) && mn.name.startsWith("sortedCopy$darkaddons$")) {
                    mn.name = StringUtils.substringBeforeLast(mn.name, "$");
                    mn.desc = StringUtils.replace(mn.desc, "com/google/common/collect/ImmutableList", "java/util/List");
                }

                if ("gg/darkaddons/mixins/MixinTabListParser".equals(className) && mn.name.startsWith("sortedCopy$darkaddons$")) {
                    mn.name = StringUtils.substringBeforeLast(mn.name, "$");
                    mn.desc = StringUtils.replace(mn.desc, "com/google/common/collect/ImmutableList", "java/util/List");
                }

                if ("gg/darkaddons/SubCommand$StopCodeflowException".equals(className)) {
                    if ("java/lang/RuntimeException".equals(classNode.superName)) {
                        classNode.superName = "java/lang/Throwable";
                        if ("<init>".equals(mn.name)) {
                            for (final Iterator<AbstractInsnNode> insnNodeIterator = mn.instructions.iterator(); insnNodeIterator.hasNext();) {
                                final var insnNode = insnNodeIterator.next();

                                if (insnNode instanceof final MethodInsnNode mnInsnNode && "<init>".equals(mnInsnNode.name) && "java/lang/RuntimeException".equals(mnInsnNode.owner)) {
                                    mnInsnNode.owner = "java/lang/Throwable";
                                }
                            }
                        }
                    }
                }

                final var annotationNodeList = mn.visibleAnnotations;
                if (null != annotationNodeList && MarkCompilerGeneratedMethodsFinal.ANNOTATIONS_PRESENT) {
                    for (final var it = mn.visibleAnnotations.iterator(); it.hasNext();) {
                        final var annotationNode = it.next();
                        final var reflectionRepresentationOfFullyQualifiedName = StringUtils.replaceChars(StringUtils.removeEnd(StringUtils.removeStart(annotationNode.desc, "L"), ";"), '/', '.');
                        var modified = false;
                        if (MarkCompilerGeneratedMethodsFinal.bridgeAnnotation.getName().equals(reflectionRepresentationOfFullyQualifiedName)) {
                            if (0 == (mn.access & Opcodes.ACC_BRIDGE)) {
                                mn.access |= Opcodes.ACC_BRIDGE;
                                it.remove();

                                modified = true;
                            }
                        } else if (MarkCompilerGeneratedMethodsFinal.syntheticAnnotation.getName().equals(reflectionRepresentationOfFullyQualifiedName)) {
                            if (0 == (mn.access & Opcodes.ACC_SYNTHETIC)) {
                                mn.access |= Opcodes.ACC_SYNTHETIC;
                                it.remove();

                                modified = true;
                            }
                        } else if (MarkCompilerGeneratedMethodsFinal.packagePrivateAnnotation.getName().equals(reflectionRepresentationOfFullyQualifiedName)) {
                            if (0 != (mn.access & Opcodes.ACC_PUBLIC)) {
                                mn.access &= ~Opcodes.ACC_PUBLIC;
                                it.remove();

                                modified = true;
                            }
                        } else if (MarkCompilerGeneratedMethodsFinal.privateAnnotation.getName().equals(reflectionRepresentationOfFullyQualifiedName)) {
                            if (0 != (mn.access & Opcodes.ACC_PUBLIC)) {
                                mn.access &= ~Opcodes.ACC_PUBLIC;
                                mn.access |= Opcodes.ACC_PRIVATE;
                                it.remove();

                                modified = true;
                            }
                        } else if (MarkCompilerGeneratedMethodsFinal.nameAnnotation.getName().equals(reflectionRepresentationOfFullyQualifiedName)) {
                            final var values = annotationNode.values;

                            if (null == values) {
                                continue;
                            }

                            final var newName = (String) values.get(1);

                            if (!mn.name.equals(newName)) {
                                mn.name = newName;
                                it.remove();

                                modified = true;
                            }
                        }
                        if (checkRun && modified) {
                            throw new IllegalStateException("class " + className + " should've been on the list of classes to optimize, but it was not (method info: " + definition + ", annotation info: " + annotationNode.desc + ')');
                        }
                    }
                }
            }

            for (final var fd : classNode.fields) {
                final var fieldModStr = Modifier.toString(fd.access);

                if (className.contains("Kt") && fieldModStr.contains("public")) {
                    fd.access &= ~Opcodes.ACC_PUBLIC;
                    if (checkRun) {
                        throw new IllegalStateException("class " + className + " should've been on the list of classes to optimize, but it was not (field info: " + fieldModStr + ' ' + fd.name + ')');
                    }
                }

                if (0 == (classNode.access & Opcodes.ACC_PUBLIC)) {
                    var modified = false;
                    if (0 != (fd.access & Opcodes.ACC_PROTECTED)) {
                        fd.access &= ~Opcodes.ACC_PROTECTED;

                        modified = true;
                    } else if (0 != (fd.access & Opcodes.ACC_PUBLIC)) {
                        fd.access &= ~Opcodes.ACC_PUBLIC;

                        modified = true;
                    }
                    if (checkRun && modified) {
                        throw new IllegalStateException("class " + className + " should've been on the list of classes to optimize, but it was not (field info: " + fieldModStr + ' ' + fd.name + ')');
                    }
                }
            }

            if (!checkRun) {
                final var classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
                classNode.accept(new CheckClassAdapter(classWriter, true));

                final var nonTransformedClassFile = new File(new File(new File(new File(new File(MarkCompilerGeneratedMethodsFinal.BUILD), "classes"), "java"), "main"), classNode.name + ".class");

                if (!nonTransformedClassFile.exists()) {
                    throw new IOException("original class file not found: " + nonTransformedClassFile);
                }

                final var transformedClassFile = new File(transformedClassesDir, classNode.name + ".class");
                final var transformedClassFilePath = transformedClassFile.toPath();

                Files.createDirectories(transformedClassFilePath.getParent());
                Files.createFile(transformedClassFilePath);

                //noinspection TryStatementWithMultipleResources
                try (final var newOutputStream = Files.newOutputStream(transformedClassFilePath);
                     final var bufferedOutputStream = new BufferedOutputStream(newOutputStream);
                     final var dataOutputStream = new DataOutputStream(bufferedOutputStream)) {
                    dataOutputStream.write(classWriter.toByteArray());
                    dataOutputStream.flush();
                }

                if (!transformedClassFile.setLastModified(nonTransformedClassFile.lastModified())) {
                    throw new IOException("can't set last modified date");
                }
            }
        }
    }

    @NotNull
    private static final File findModJarFile() throws IOException {
        final var libsDir = new File(new File(MarkCompilerGeneratedMethodsFinal.BUILD), "libs");
        for (final var file : libsDir.listFiles()) {
            if (file.getName().endsWith(".jar") && !file.getName().endsWith("-dev.jar") && !file.getName().endsWith("-opt.jar") && file.getName().endsWith("-proguarded.jar")) {
                return file;
            }
        }
        throw new IOException("can't find mod jar file in " + libsDir);
    }

    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    @NotNull
    private static final ArrayList<File> getFilesInDirectoryRecursively(@NotNull final File dir) {
        final var files = new ArrayList<File>(100);
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new IllegalArgumentException("argument is not a directory");
            }

            for (final var file : dir.listFiles()) {
                if (file.isDirectory()) {
                    files.addAll(MarkCompilerGeneratedMethodsFinal.getFilesInDirectoryRecursively(file));
                } else {
                    files.add(file);
                }
            }
        }
        return files;
    }

    @SuppressWarnings("CollectionDeclaredAsConcreteClass")
    @NotNull
    private static final ArrayList<File> getTransformedClassFiles() throws IOException {
        final var transformedClassFiles = new ArrayList<File>(100);
        final var transformedClassesDir = new File(new File(MarkCompilerGeneratedMethodsFinal.BUILD), MarkCompilerGeneratedMethodsFinal.TRANSFORMED_CLASSES);
        for (final var file : MarkCompilerGeneratedMethodsFinal.getFilesInDirectoryRecursively(transformedClassesDir)) {
            if (file.getName().endsWith(".class")) {
                transformedClassFiles.add(file);
            } else {
                throw new IOException("non-class file in " + transformedClassesDir);
            }
        }
        //noinspection IfCanBeAssertion
        if (transformedClassFiles.isEmpty()) {
            throw new IOException("no transformed class files in " + transformedClassesDir);
        }
        return transformedClassFiles;
    }

    private static final void generateNewJarWithTransformedClasses() throws IOException {
        @SuppressWarnings("TypeMayBeWeakened") final var env = new HashMap<String, String>(PublicUtils.calculateHashMapCapacity(1));
        env.put("create", "true");
        final var currFile = MarkCompilerGeneratedMethodsFinal.findModJarFile();
        final var path = new File(currFile.getParent(), StringUtils.remove(currFile.getName(), "-proguarded.jar") + "-opt.jar").toPath();
        Files.copy(currFile.toPath(), path, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
        final var uri = URI.create("jar:" + path.toUri());
        try (final var fs = FileSystems.newFileSystem(uri, env)) {
            for (final var fileInDir : MarkCompilerGeneratedMethodsFinal.getTransformedClassFiles()) {
                var fileInJarPath = StringUtils.remove(StringUtils.remove(fileInDir.getPath(), MarkCompilerGeneratedMethodsFinal.TRANSFORMED_CLASSES), MarkCompilerGeneratedMethodsFinal.BUILD);
                while (!fileInJarPath.isEmpty() && '/' == fileInJarPath.charAt(0)) {
                    fileInJarPath = MarkCompilerGeneratedMethodsFinal.removeOnce(fileInJarPath, '/');
                }
                final var fileInJar = fs.getPath(fileInJarPath);
                Files.copy(fileInDir.toPath(), fileInJar, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
            }
            try (final var walk = Files.walk(fs.getPath("/gg/darkaddons/annotations"), Integer.MAX_VALUE)) {
                for (final var it = walk.iterator(); it.hasNext();) {
                    final var p = it.next();
                    if (!Files.isDirectory(p)) {
                        Files.delete(p);
                    }
                }
            }
        }
    }

    @NotNull
    private static final String removeOnce(@NotNull final String str, final char remove) {
        if (StringUtils.isEmpty(str) || StringUtils.INDEX_NOT_FOUND == str.indexOf(remove)) {
            return str;
        }
        final var chars = str.toCharArray();
        var pos = 0;
        var replacedOnce = false;
        final var charsLength = chars.length;
        for (var i = 0; i < charsLength; ++i) {
            if (chars[i] != remove || replacedOnce) {
                chars[pos++] = chars[i];
            } else {
                replacedOnce = true;
            }
        }
        return new String(chars, 0, pos);
    }

    @NotNull
    private static final String getMethodDefinition(@NotNull final String name, final int mod) {
        return StringUtils.replace(Modifier.toString(mod), "volatile", "bridge") + ' ' + name;
    }

    private static final void markCompilerGeneratedMethodsFinal() throws IOException {
        //System.out.println("Finding classes to transform...");
        final var classes = MarkCompilerGeneratedMethodsFinal.findClassesToTransform();
        //System.out.println("Transforming classes...");
        MarkCompilerGeneratedMethodsFinal.transformClasses(classes, false);
        //System.out.println("Generate new JAR with transformed classes...");
        MarkCompilerGeneratedMethodsFinal.generateNewJarWithTransformedClasses();
    }

    private enum MethodFlawDetected {
        HUGE_METHOD("huge method"),
        LARGE_METHOD("large method"),
        NONSMALL_METHOD("non-small method");

        @NotNull
        private final String displayName;

        private MethodFlawDetected(@NotNull final String displayNameIn) {
            this.displayName = displayNameIn;
        }

        @NotNull
        private final String displayName() {
            return this.displayName;
        }

        @Override
        public final String toString() {
            return "MethodFlawDetected{" +
                "displayName='" + this.displayName + '\'' +
                '}';
        }
    }

    private static final class MethodInfoWithClass implements Comparable<MarkCompilerGeneratedMethodsFinal.MethodInfoWithClass> {
        @NotNull
        private final MethodInfo mi;
        @NotNull
        private final Class<?> clazz;

        private MethodInfoWithClass(@NotNull final MethodInfo miIn, @NotNull final Class<?> clazzIn) {
            super();

            this.mi = miIn;
            this.clazz = clazzIn;
        }

        @Override
        public final int compareTo(@NotNull final MarkCompilerGeneratedMethodsFinal.MethodInfoWithClass other) {
            return Integer.compare(this.mi.getCodeAttribute().getCode().length, other.mi.getCodeAttribute().getCode().length);
        }

        @Override
        public final boolean equals(@Nullable final Object obj) {
            if (this == obj) {
                return true;
            }

            if (null == obj || this.getClass() != obj.getClass()) {
                return false;
            }

            final var that = (MarkCompilerGeneratedMethodsFinal.MethodInfoWithClass) obj;

            return this.mi.equals(that.mi) && this.clazz == that.clazz;
        }

        @Override
        public final int hashCode() {
            int result = this.mi.hashCode();
            result = 31 * result + this.clazz.hashCode();

            return result;
        }

        @Override
        public final String toString() {
            return "MethodInfoWithClass{" +
                "mi=" + this.mi +
                ", clazz=" + this.clazz +
                '}';
        }
    }

    private static final void checkMethodSizes() throws IOException {
        final var notIdealMethods = new HashMap<MarkCompilerGeneratedMethodsFinal.MethodInfoWithClass, MarkCompilerGeneratedMethodsFinal.MethodFlawDetected>(PublicUtils.calculateHashMapCapacity(100));
        for (final var info : MarkCompilerGeneratedMethodsFinal.getAllClasses()) {
            if (!MarkCompilerGeneratedMethodsFinal.isModClass(info)) {
                continue;
            }

            try {
                final var clazz = info.load();
                try (final var is = Thread.currentThread().getContextClassLoader().getResourceAsStream(StringUtils.replaceChars(clazz.getName(), '.', '/') + ".class")) {
                    try (final var dataInputStream = new DataInputStream(is)) {
                        final var cf = new ClassFile(dataInputStream);
                        for (final MethodInfo mi : cf.getMethods()) {
                            final CodeAttribute ca = mi.getCodeAttribute();
                            if (null == ca) {
                                continue; // abstract or native method
                            }
                            final int bLen = ca.getCode().length;
                            if (8_000 < bLen) { // Default method size to be excluded from compilation if -XX:+DontCompileHugeMethods (which is on by default) - This a serious issue as it prevents both C1 and C2 compilation.
                                notIdealMethods.put(new MarkCompilerGeneratedMethodsFinal.MethodInfoWithClass(mi, clazz), MarkCompilerGeneratedMethodsFinal.MethodFlawDetected.HUGE_METHOD);
                            } else if (2_000 < bLen) { // Default value of -XX:+InlineSmallCode.
                                notIdealMethods.put(new MarkCompilerGeneratedMethodsFinal.MethodInfoWithClass(mi, clazz), MarkCompilerGeneratedMethodsFinal.MethodFlawDetected.LARGE_METHOD);
                            } else if (325 < bLen) { // Default value of -XX:+FreqInlineSize.
                                notIdealMethods.put(new MarkCompilerGeneratedMethodsFinal.MethodInfoWithClass(mi, clazz), MarkCompilerGeneratedMethodsFinal.MethodFlawDetected.NONSMALL_METHOD);
                            }
                        }
                    }
                }
            } catch (final NoClassDefFoundError ncdfe) {
                MarkCompilerGeneratedMethodsFinal.err("Can't find " + ncdfe.getMessage() + ", please check class path!");
            }
        }
        final var notIdealMethodsComparator = Map.Entry.<MarkCompilerGeneratedMethodsFinal.MethodInfoWithClass, MarkCompilerGeneratedMethodsFinal.MethodFlawDetected>comparingByValue().thenComparing(Map.Entry::getKey);
        final Map<MarkCompilerGeneratedMethodsFinal.MethodInfoWithClass, MarkCompilerGeneratedMethodsFinal.MethodFlawDetected> sortedNotIdealMethods = notIdealMethods.entrySet().stream().sorted(notIdealMethodsComparator).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (@NotNull final var e1, @NotNull final var e2) -> e1, LinkedHashMap::new));
        sortedNotIdealMethods.forEach((@NotNull final MarkCompilerGeneratedMethodsFinal.MethodInfoWithClass miWCl, @NotNull final MarkCompilerGeneratedMethodsFinal.MethodFlawDetected flaw) -> {
            final var mi = miWCl.mi;
            final var cl = miWCl.clazz;

            MarkCompilerGeneratedMethodsFinal.err(flaw.displayName() + ": " + cl.getName() + '.' + mi.getName() + ' ' + mi.getCodeAttribute().getCode().length);
        });
    }
}
