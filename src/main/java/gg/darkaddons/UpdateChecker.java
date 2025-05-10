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
        UP_TO_DATE, OUT_OF_DATE, COULD_NOT_CHECK;

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

        DarkAddons.queueWarning("A new version of " + DarkAddons.MOD_NAME + " is available: v" + latestVersion + ". You are running " + DarkAddons.class.getSimpleName() + " v" + currentVersion + ". It is recommended to always use the latest version available of the mod, please update when convenient.");
        DarkAddons.queueWarning("The latest release of the mod can be installed from GitHub at: https://github.com/TheDGOfficial/DarkAddons/releases");
        DarkAddons.queueWarning("Version upgrades are always recommended and will include various bug fixes, performance enhancements, and new features. Instructions for installing from GitHub: Head down to the Assets section and expand it then download the JAR file (not source code!), and put in mods folder as usual. Remove any older versions to avoid conflicts.");
        return UpdateChecker.UpdateCheckerResult.OUT_OF_DATE;
    }

    @Nullable
    private static final String getLatestVersion() {
        return Utils.sendWebRequest("https://darkaddons.netlify.app/latest_mod_version.txt");
    }
}
