package gg.darkaddons;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;

import java.nio.file.Files;
import java.nio.charset.StandardCharsets;

final class FileUtils {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private FileUtils() {
        super();

        throw Utils.staticClassException();
    }

    static final void write(@NotNull final File file, @NotNull final String text) {
        try {
            Files.write(file.toPath(), text.getBytes(StandardCharsets.UTF_8));
        } catch (final IOException e) {
            DarkAddons.modError(e);
        }
    }

    @NotNull
    static final String read(@NotNull final File file) {
        try {
            return new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        } catch (final IOException e) {
            DarkAddons.modError(e);
            throw new UncheckedIOException(e);
        }
    }
}
