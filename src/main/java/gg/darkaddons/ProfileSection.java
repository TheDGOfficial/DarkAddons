package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

/**
 * Helper class to close profiler sections automatically, taking advantage of try-with-resources
 * feature present in Java 7 and above.
 * <p>
 * This doesn't kill performance since we are not creating a new ProfileSection every time, instead
 * using this class from a single instance. This doesn't cause any issues as Minecraft code is single
 * threaded.
 * <p>
 * Try-finally is still not free in terms of performance though, and increases compiled code size significantly.
 * <p>
 * Here's a quick example about the usage of this class:
 * <pre>
 * {@code
 * try (final ProfileSection profileSection = ProfileSection.getInstance()) {
 *     profileSection.start("my_section");
 *     // .. section logic
 * }
 * // .. section will be closed here automatically
 * }
 * </pre>
 */
@SuppressWarnings("Singleton")
final class ProfileSection implements AutoCloseable {
    @NotNull
    private static final ProfileSection INSTANCE = new ProfileSection();
    private static boolean cleanState = true;

    private ProfileSection() {
        super();
    }

    @NotNull
    static final ProfileSection getInstance() {
        return ProfileSection.INSTANCE;
    }

    static final void markDirty() {
        ProfileSection.cleanState = false;
    }

    private static final void close0() {
        while (true) {
            McProfilerHelper.endSection();
            if (ProfileSection.cleanState) {
                break;
            }
            ProfileSection.cleanState = true;
        }
    }

    /**
     * Starts a profiling section.
     * <p>
     * This method is not static to avoid the variable in the try-with resources
     * being unused.
     *
     * @param sectionName The name of the section.
     */
    @SuppressWarnings("MethodMayBeStatic")
    final void start(@NotNull final String sectionName) {
        McProfilerHelper.startSection(sectionName);
    }

    @Override
    public final void close() {
        ProfileSection.close0(); // Necessary to not trigger "static field set from instance context" warnings
    }
}
