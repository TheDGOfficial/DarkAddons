package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

final class UpdateChecker {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private UpdateChecker() {
        super();

        throw Utils.staticClassException();
    }

    @SuppressWarnings("LambdaCanBeReplacedWithAnonymous")
    @NotNull
    private static final ExecutorService updateCheckerExecutor = Executors.newSingleThreadExecutor((@NotNull final Runnable r) -> Utils.newThread(r, "DarkAddons Update Checker Thread"));

    enum UpdateCheckerResult {
        UP_TO_DATE, OUT_OF_DATE, AHEAD_OF_REMOTE, COULD_NOT_CHECK;

        private UpdateCheckerResult() {

        }
    }

    static final void checkInBackground(@NotNull final Consumer<? super UpdateChecker.UpdateCheckerResult> callback) {
        UpdateChecker.updateCheckerExecutor.execute(() -> callback.accept(UpdateChecker.checkUpdates()));
    }

    @NotNull
    private static final UpdateChecker.UpdateCheckerResult checkUpdates() {
        final var latestVersion = UpdateChecker.getLatestVersion();

        if (null == latestVersion) {
            DarkAddons.queueWarning("Unable to check mod updates, please report if this keeps continuing and you are connected to the internet!");
            return UpdateChecker.UpdateCheckerResult.COULD_NOT_CHECK;
        }

        final var currentVersion = Reference.VERSION;

        if (currentVersion.equals(latestVersion)) {
            return UpdateChecker.UpdateCheckerResult.UP_TO_DATE;
        }

        final var cmp = UpdateChecker.compareSemVer(currentVersion, latestVersion);

        if (0 == cmp) {
            return UpdateChecker.UpdateCheckerResult.UP_TO_DATE;
        }

        if (0 < cmp) {
            // The current version is higher than the latest release
            DarkAddons.queueWarning("You are running an in-development version of DarkAddons. Please report bugs and provide feedback!");
            return UpdateChecker.UpdateCheckerResult.AHEAD_OF_REMOTE;
        }

        DarkAddons.queueWarning("A new version of " + DarkAddons.MOD_NAME + " is available: v" + latestVersion + ". You are running " + DarkAddons.class.getSimpleName() + " v" + currentVersion + ". It is recommended to always use the latest version available of the mod, please update when convenient.");
        DarkAddons.queueWarning("The latest release of the mod can be installed from GitHub at: https://github.com/TheDGOfficial/DarkAddons/releases");
        DarkAddons.queueWarning("Version upgrades are always recommended and will include various bug fixes, performance enhancements, and new features. Instructions for installing from GitHub: Head down to the Assets section and expand it then download the JAR file (not source code!), and put in mods folder as usual. Remove any older versions to avoid conflicts.");
        return UpdateChecker.UpdateCheckerResult.OUT_OF_DATE;
    }

    @Nullable
    private static final String getLatestVersion() {
        return Utils.sendWebRequest("https://darkaddons.netlify.app/latest_mod_version.txt");
    }

    /**
     * Compares two semantic versions (major.minor.patch).
     * Returns positive if v1 > v2, negative if v1 < v2, 0 if equal.
     */
    private static final int compareSemVer(@NotNull final String v1, @NotNull final String v2) {
        final var p1 = v1.split("\\.");
        final var p2 = v2.split("\\.");
        final var length = Math.max(p1.length, p2.length);
        for (var i = 0; i < length; ++i) {
            final var num1 = i < p1.length ? UpdateChecker.parseIntSafe(p1[i]) : 0;
            final var num2 = i < p2.length ? UpdateChecker.parseIntSafe(p2[i]) : 0;
            if (num1 != num2) {
                return Integer.compare(num1, num2);
            }
        }
        return 0;
    }

    private static final int parseIntSafe(@NotNull final String s) {
        final var result = Utils.safeParseIntFast(s);

        return -1 == result ? 0 : result;

    }
}
