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
    private static final @NotNull File tinyConfigFile = new File(new File(new File("config"), "darkaddons"), "tinyconfig.properties");
    private static final @NotNull Properties tinyConfigSettings = new Properties();

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private TinyConfig() {
        super();

        throw new UnsupportedOperationException("static class");
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
                throw new IllegalStateException("Unable to create TinyConfig", ioException);
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
            throw new IllegalStateException("Unable to load TinyConfig", ioException);
        }
    }

    private static final void save() {
        TinyConfig.mkdirsAndFile();

        try (final var fileOutputStream = new FileOutputStream(TinyConfig.tinyConfigFile)) {
            try (final var outputStreamWriter = new OutputStreamWriter(fileOutputStream, StandardCharsets.UTF_8)) {
                TinyConfig.tinyConfigSettings.store(outputStreamWriter, null);
            }
        } catch (final IOException ioException) {
            throw new IllegalStateException("Unable to save TinyConfig", ioException);
        }
    }

    @Nullable
    static final String getString(@NotNull final String key) {
        return TinyConfig.tinyConfigSettings.getProperty(key);
    }

    @NotNull
    static final String getString(@NotNull final String key, @NotNull final String defaultValue) {
        return TinyConfig.tinyConfigSettings.getProperty(key, defaultValue);
    }

    static final void setString(@NotNull final String key, @NotNull final String value) {
        TinyConfig.tinyConfigSettings.setProperty(key, value);
        TinyConfig.save();
    }

    @Nullable
    static final Boolean getBoolean(@NotNull final String key) {
        final var value = TinyConfig.tinyConfigSettings.getProperty(key);

        return null == value ? null : TinyConfig.parseBoolean(value);
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

    static final Double getDouble(@NotNull final String key) {
        final var value = TinyConfig.tinyConfigSettings.getProperty(key);

        return null == value ? null : TinyConfig.parseDouble(value);
    }

    static final double getDouble(@NotNull final String key, final double defaultValue) {
        return TinyConfig.parseDouble(TinyConfig.tinyConfigSettings.getProperty(key, Double.toString(defaultValue)));
    }

    static final void setDouble(@NotNull final String key, final double value) {
        TinyConfig.tinyConfigSettings.setProperty(key, Double.toString(value));
        TinyConfig.save();
    }

    private static final double parseDouble(@NotNull final String value) {
        try {
            return Double.parseDouble(value);
        } catch (final NumberFormatException numberFormatException) {
            throw new IllegalArgumentException("Unable to parse \"" + value + "\" as double", numberFormatException);
        }
    }

    static final Long getLong(@NotNull final String key) {
        final var value = TinyConfig.tinyConfigSettings.getProperty(key);

        return null == value ? null : TinyConfig.parseLong(value);
    }

    static final long getLong(@NotNull final String key, final long defaultValue) {
        return TinyConfig.parseLong(TinyConfig.tinyConfigSettings.getProperty(key, Long.toString(defaultValue)));
    }

    static final void setLong(@NotNull final String key, final long value) {
        TinyConfig.tinyConfigSettings.setProperty(key, Long.toString(value));
        TinyConfig.save();
    }

    private static final long parseLong(@NotNull final String value) {
        try {
            return Long.parseLong(value);
        } catch (final NumberFormatException numberFormatException) {
            throw new IllegalArgumentException("Unable to parse \"" + value + "\" as long", numberFormatException);
        }
    }

    static final Integer getInt(@NotNull final String key) {
        final var value = TinyConfig.tinyConfigSettings.getProperty(key);

        return null == value ? null : TinyConfig.parseInt(value);
    }

    static final int getInt(@NotNull final String key, final int defaultValue) {
        return TinyConfig.parseInt(TinyConfig.tinyConfigSettings.getProperty(key, Integer.toString(defaultValue)));
    }

    static final void setInt(@NotNull final String key, final int value) {
        TinyConfig.tinyConfigSettings.setProperty(key, Integer.toString(value));
        TinyConfig.save();
    }

    private static final int parseInt(@NotNull final String value) {
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException numberFormatException) {
            throw new IllegalArgumentException("Unable to parse \"" + value + "\" as int", numberFormatException);
        }
    }
}
