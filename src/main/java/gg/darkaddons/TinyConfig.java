package gg.darkaddons;

import java.io.File;
import java.util.Properties;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.io.InputStreamReader;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;

final class TinyConfig {
    private static final File tinyConfigFile = new File(new File(new File("config"), "darkaddons"), "tinyconfig.properties");
    private static final Properties tinyConfigSettings = new Properties();

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private TinyConfig() {
        super();

        throw new UnsupportedOperationException();
    }

    static {
        TinyConfig.load();
    }

    private static final void mkdirsAndFile() {
        new File(TinyConfig.tinyConfigFile.getParent()).mkdirs();

        if (!TinyConfig.tinyConfigFile.exists()) {
            try {
                if (!TinyConfig.tinyConfigFile.createNewFile()) {
                    throw new IOException("cannot create new file");
                }
            } catch (final IOException ioException) {
                throw new RuntimeException("Unable to create TinyConfig", ioException);
            }
        }
    }

    private static final void load() {
        TinyConfig.mkdirsAndFile();

        try (final var fileInputStream = new FileInputStream(TinyConfig.tinyConfigFile)) {
            try (final var inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8)) {
                TinyConfig.tinyConfigSettings.load(inputStreamReader);
            }
        } catch (final IOException ioException) {
            throw new RuntimeException("Unable to load TinyConfig", ioException);
        }
    }

    private static final void save() {
        TinyConfig.mkdirsAndFile();

        try (final var fileOutputStream = new FileOutputStream(TinyConfig.tinyConfigFile)) {
            try (final var outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                TinyConfig.tinyConfigSettings.store(outputStreamWriter, null);
            }
        } catch (final IOException ioException) {
            throw new RuntimeException("Unable to save TinyConfig", ioException);
        }
    }

    static final boolean getBoolean(@NotNull final String key, final boolean defaultValue) {
        return TinyConfig.parseBoolean(TinyConfig.tinyConfigSettings.getProperty(key, Boolean.toString(defaultValue)));
    }

    static final void setBoolean(@NotNull final String key, final boolean value) {
        TinyConfig.tinyConfigSettings.setProperty(key, Boolean.toString(value));
        TinyConfig.save();
    }

    private static final boolean parseBoolean(@Nullable final String value) {
        return "true".equals(value);
    }
}
