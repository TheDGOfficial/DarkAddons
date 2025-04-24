import org.objectweb.asm.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.jar.*;

public class JarDependencyChecker {
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            System.out.println("Usage: java JarDependencyChecker <path-to-jar> <discouraged-package> [<discouraged-package> ...]");
            return;
        }

        String jarPath = args[0];
        Set<String> discouragedPackages = new HashSet<>();
        for (int i = 1; i < args.length; i++) {
            discouragedPackages.add(args[i].replace('.', '/') + "/");
        }

        final int[] totalMatches = {0};

        try (JarFile jarFile = new JarFile(jarPath)) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try {
                        Map<String, List<String>> results = new LinkedHashMap<>();
                        String currentClass = entry.getName();

                        ClassReader reader = new ClassReader(jarFile.getInputStream(entry));
                        reader.accept(new ClassVisitor(Opcodes.ASM9) {
                            @Override
                            public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                                return new MethodVisitor(Opcodes.ASM9) {
                                    void record(String type, String ref) {
                                        results.computeIfAbsent(currentClass, k -> new ArrayList<>())
                                               .add(String.format("[%s] %s", type, ref));
                                    }

                                    @Override
                                    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                                        discouragedPackages.stream()
                                                .filter(owner::startsWith)
                                                .findFirst()
                                                .ifPresent(pkg -> record("Method", owner + "." + name));
                                    }

                                    @Override
                                    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                                        discouragedPackages.stream()
                                                .filter(owner::startsWith)
                                                .findFirst()
                                                .ifPresent(pkg -> record("Field", owner + "." + name));
                                    }

                                    @Override
                                    public void visitTypeInsn(int opcode, String type) {
                                        discouragedPackages.stream()
                                                .filter(type::startsWith)
                                                .findFirst()
                                                .ifPresent(pkg -> record("Type", type));
                                    }
                                };
                            }

                            @Override
                            public void visitEnd() {
                                List<String> classResults = results.get(currentClass);
                                if (classResults != null && !classResults.isEmpty()) {
                                    System.out.println(currentClass + ":");
                                    classResults.forEach(line -> {
                                        System.out.println("  " + line);
                                        totalMatches[0]++;
                                    });
                                }
                            }
                        }, 0);
                    } catch (IOException e) {
                        System.err.println("Failed to read class: " + entry.getName());
                    }
                }
            }
        }

        System.out.println("\nTotal discouraged references found: " + totalMatches[0]);
    }
}
