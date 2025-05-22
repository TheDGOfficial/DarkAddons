import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

final class JarDependencyChecker {
    private static final int ASM_API_VERSION = Opcodes.ASM9;
    private static final int READER_ACCEPT_FLAGS = 0;

    private JarDependencyChecker() {
        super();

        throw new UnsupportedOperationException("static class");
    }

    public static final void main(final String... args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java JarDependencyChecker <path-to-jar> <discouraged-package> [<discouraged-package> ...]");
            return;
        }

        final var jarPath = args[0];
        final var discouragedPackages = new HashSet<String>(16);
        for (var i = 1; i < args.length; ++i) {
            discouragedPackages.add(args[i].replace('.', '/') + '/');
        }

        final var totalMatches = new int[]{0};

        try (final var jarFile = new JarFile(jarPath)) {
            final var sortedEntries = Collections.list(jarFile.entries()).stream()
                .filter(entry -> entry.getName().endsWith(".class"))
                .sorted(Comparator.comparing(JarEntry::getName, (a, b) -> {
                    final var nameA = a.replace(".class", "");
                    final var nameB = b.replace(".class", "");

                    final var outerA = nameA.split("\\$")[0];
                    final var outerB = nameB.split("\\$")[0];
                    final var cmp = outerA.compareTo(outerB);
                    return (0 != cmp) ? cmp : nameA.compareTo(nameB);
                }))
                .collect(Collectors.toList());

            for (final var entry : sortedEntries) {
                final var currentClass = entry.getName();
                try {
                    final var results = new LinkedHashMap<String, List<String>>(100);
                    final var reader = new ClassReader(jarFile.getInputStream(entry));
                    reader.accept(new ClassVisitor(JarDependencyChecker.ASM_API_VERSION) {
                        @Override
                        public final MethodVisitor visitMethod(final int access, final String name, final String descriptor, final String signature, final String[] exceptions) {
                            return new MethodVisitor(this.api) {
                                final void recordEntry(final String type, final String ref) {
                                    results.computeIfAbsent(currentClass, k -> new ArrayList<>(16))
                                           .add("[" + type + "] " + ref);
                                }

                                @Override
                                public final void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface) {
                                    discouragedPackages.stream()
                                        .filter(owner::startsWith)
                                        .findFirst()
                                        .ifPresent(pkg -> recordEntry("Method", owner + '.' + name));
                                }

                                @Override
                                public final void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor) {
                                    discouragedPackages.stream()
                                        .filter(owner::startsWith)
                                        .findFirst()
                                        .ifPresent(pkg -> recordEntry("Field", owner + '.' + name));
                                }

                                @Override
                                public final void visitTypeInsn(final int opcode, final String type) {
                                    discouragedPackages.stream()
                                        .filter(type::startsWith)
                                        .findFirst()
                                        .ifPresent(pkg -> recordEntry("Type", type));
                                }
                            };
                        }

                        @Override
                        public final void visitEnd() {
                            final var classResults = results.get(currentClass);
                            if (null != classResults && !classResults.isEmpty()) {
                                System.out.println(currentClass + ':');
                                classResults.forEach(line -> {
                                    System.out.println("  " + line);
                                    ++totalMatches[0];
                                });
                            }
                        }
                    }, READER_ACCEPT_FLAGS);
                } catch (final IOException ioException) {
                    System.err.println("Failed to read class: " + currentClass);
                }
            }
        }

        System.out.println("\nTotal discouraged references found: " + totalMatches[0]);
    }
}
