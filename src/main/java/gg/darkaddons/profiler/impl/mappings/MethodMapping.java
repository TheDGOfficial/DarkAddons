package gg.darkaddons.profiler.impl.mappings;

import gg.darkaddons.PublicUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Objects;

/**
 * Represents an obfuscation mapping for a method.
 */
public final class MethodMapping {
    /**
     * Holds the actual method mappings in a hash map that is lazily loaded when this clas is first accessed.
     */
    public static final class MethodMappingsHolder {
        /**
         * Private constructor to prevent instance initialization.
         */
        private MethodMappingsHolder() {
            super();

            throw new UnsupportedOperationException("mappings class is only for static initialization, instance initialization is not permitted");
        }

        /**
         * Holds the cached mappings.
         */
        @NotNull
        private static final HashMap<String, MethodMapping> mappings = MethodMapping.MethodMappingsHolder.loadMappings();

        /**
         * Loads the mappings to the RAM by first extracting the mapping file to a location on the disk,
         * then reading that file from disk, parsing the contents of the file, constructing method mapping instances
         * corresponding to the mappings and putting them to a {@link HashMap}, then returning the hash map.
         *
         * @return A {@link HashMap} that contains all the mappings.
         */
        @NotNull
        private static final HashMap<String, MethodMapping> loadMappings() {
            final var mappingsFile = new File(new File(new File("config"), "darkaddons"), "methods.v2.csv");

            try {
                //if (mappingsFile.exists()) {
                //    Files.delete(mappingsFile.toPath()); // Delete the file if it already exists, so we can update mappings later if we want
                //}

                // Copy the file to the location on the disk.
                if (!mappingsFile.exists()) {
                    FileUtils.copyInputStreamToFile(Objects.requireNonNull(Objects.requireNonNull(MethodMapping.MethodMappingsHolder.class.getClassLoader()).getResourceAsStream(mappingsFile.getName())), mappingsFile);
                }
            } catch (final IOException e) {
                throw new UncheckedIOException(e);
            }

            // Initial size hint that matches 1.8.9 mapping size (with new mappings appended from 1.9.4 mappings, because some classes like JsonToNBT lack mappings in 1.8.9 + plus some yarn mappings for remaining missing mappings)
            final var mappingSize = 9_123;

            final var map = new HashMap<String, MethodMapping>((int) Math.ceil(mappingSize / 0.75D)); // Inlined short version of Utils#calculateHashMapCapacity

            // Read file on the disk line by line
            for (final var line : StringUtils.replace(PublicUtils.read(mappingsFile), "\\n", "<br>").split("\n")) {
                MethodMapping.MethodMappingsHolder.parseCSVLineIntoMap(Objects.requireNonNull(line), map);
            }

            final var mapSize = map.size();

            //noinspection IfCanBeAssertion
            if (mappingSize != mapSize) {
                // Raise an error if the initial size hint doesn't match the actual size since it's likely that loading all mappings failed or the mapping file (or the algorithm parsing it) is corrupt, or the size hint is forgotten to be updated by the devs after updating mappings.
                throw new IllegalStateException("couldn't load all mappings, expected " + mappingSize + " mappings, loaded " + mapSize + " mappings");
            }

            return map;
        }

        private static final void parseCSVLineIntoMap(@NotNull final String line, @NotNull @SuppressWarnings({"CollectionDeclaredAsConcreteClass", "BoundedWildcard", "TypeMayBeWeakened"}) final HashMap<String, MethodMapping> map) {
            final var quotedDesc = line.contains("\"") ? StringUtils.remove(StringUtils.substringBeforeLast(line, "\""), StringUtils.remove(line, StringUtils.substringAfter(line, "\""))) : "";
            final var hasNoQuotedDesc = quotedDesc.isEmpty();

            final var columns = (hasNoQuotedDesc ? line : StringUtils.remove(StringUtils.remove(line, quotedDesc), '"')).split(","); // CSV uses "," to split columns

            // Probably an invalid line or empty line at the end of file
            if (3 != columns.length && 4 != columns.length) {
                PublicUtils.printErr("[mappings] invalid column length " + columns.length + ", line: " + line + ", columns: " + Arrays.toString(columns) + ", quoted desc: " + quotedDesc + ", line without desc: " + StringUtils.remove(StringUtils.remove(line, quotedDesc), '"') + ", skipping...");
                return;
            }

            final var columnObfName = columns[0];
            final var columnDeobfName = columns[1];
            final var columnSide = columns[2];

            final int parsedSide;

            // Ignore the header for the CSV file that lists the column names
            if (1 == Objects.requireNonNull(columnSide).length() && -1 != (parsedSide = PublicUtils.safeParseIntFast(columnSide))) {
                final var columnDesc = hasNoQuotedDesc ? 4 == columns.length ? columns[3] : "no description" : quotedDesc;
                final var convertedSide = MethodMapping.ObfSide.fromId(parsedSide);

                //noinspection IfCanBeAssertion
                if (null == convertedSide) {
                    throw new UnsupportedOperationException("unsupported side value " + parsedSide);
                }

                map.compute(columnObfName, (@NotNull final String key, @Nullable final MethodMapping value) -> {
                    if (null == value) {
                        return new MethodMapping(Objects.requireNonNull(columnObfName), Objects.requireNonNull(columnDeobfName), convertedSide, Objects.requireNonNull(columnDesc));
                    }

                    throw new IllegalStateException("duplicate mappings for " + key);
                });
            }
        }

        /**
         * Lookups the given (possibly) obfuscated name in the method obfuscation mapping table.
         *
         * @param paramObfName The (possibly) obfuscated method name to search a mapping for.
         * @return A method mapping if a mapping for the given obfuscated method name is found,
         * or null if no mapping is found or the given method name is not obfuscated.
         */
        @Nullable
        public static final MethodMapping lookup(@NotNull final String paramObfName) {
            return MethodMapping.MethodMappingsHolder.mappings.get(paramObfName);
        }
    }

    /**
     * Holds the obfuscated name of the method.
     */
    @NotNull
    private final String obfName;
    /**
     * Holds the de-obfuscated name of the method.
     */
    @NotNull
    private final String deobfName;

    /**
     * Represents a side used in obfuscation mappings.
     */
    public enum ObfSide {
        /**
         * The method belongs to the client. It is not present on servers.
         */
        CLIENT(0),
        /**
         * The method belongs to the server. It is available on the client since the client
         * is a non-stripped version of the full game, but it is not called from client code,
         * except the internal integrated server.
         */
        SERVER(2),
        /**
         * The method belongs to neither the client nor the server and can be used or present on both sides.
         */
        BOTH(1);

        /**
         * Holds the id representation of the side.
         */
        private final int id;

        /**
         * Enum constructor.
         *
         * @param idIn The ID representing this side.
         */
        private ObfSide(final int idIn) {
            this.id = idIn;
        }

        /**
         * Gets the internal ID representing this side.
         *
         * @return The internal ID representing this side.
         */
        /*private final int getId() {
            return this.id;
        }*/

        /**
         * Gets the side from internal ID.
         * <p>
         * This method loops through all sides and checks if the internal ID of that side
         * matches the given id parameter, and so the performance of it is not the best.
         *
         * @param paramId The ID to get the side from.
         * @return The side matching the given internal ID.
         */
        @Nullable
        private static final MethodMapping.ObfSide fromId(final int paramId) {
            for (final var side : MethodMapping.ObfSide.values()) {
                if (paramId == Objects.requireNonNull(side).id) {
                    return side;
                }
            }
            return null;
        }

        /**
         * Returns a debug string representation of this side.
         *
         * @return A debug string representation of this side.
         */
        @Override
        public final String toString() {
            return "ObfSide{" +
                "id=" + this.id +
                '}';
        }
    }

    /**
     * Holds the side of the method.
     */
    @NotNull
    private final MethodMapping.ObfSide obfSide;

    /**
     * Holds description of the method.
     */
    @NotNull
    private final String desc;

    /**
     * Creates a new method mapping instance.
     *
     * @param obfNameIn The obfuscated name of the method.
     * @param deobfNameIn The de-obfuscated name of the method.
     * @param obfSideIn The side of the method.
     * @param descIn The description of the method.
     */
    private MethodMapping(@NotNull final String obfNameIn, @NotNull final String deobfNameIn, @NotNull final MethodMapping.ObfSide obfSideIn, @NotNull final String descIn) {
        super();

        this.obfName = obfNameIn;
        this.deobfName = deobfNameIn;
        this.obfSide = obfSideIn;
        this.desc = descIn;
    }

    /**
     * Gets the obfuscated name of this method.
     *
     * @return The obfuscated name of this method.
     */
    /*@NotNull
    private final String getObfName() {
        return this.obfName;
    }*/

    /**
     * Gets the de-obfuscated name of this method.
     *
     * @return The de-obfuscated name of this method.
     */
    @NotNull
    public final String getDeobfName() {
        return this.deobfName;
    }

    /**
     * Gets the side this method belongs to.
     *
     * @return The side this method belongs to.
     */
    /*@NotNull
    public final MethodMapping.ObfSide getObfSide() {
        return this.obfSide;
    }*/

    /**
     * Gets the description of this method.
     *
     * @return The description of this method.
     */
    /*@NotNull
    private final String getDesc() {
        return this.desc;
    }*/

    /**
     * Returns the debug string representation of this method mapping.
     *
     * @return The debug string representation of this method mapping.
     */
    @Override
    public final String toString() {
        return "MethodMapping{" +
            "obfName='" + this.obfName + '\'' +
            ", deobfName='" + this.deobfName + '\'' +
            ", obfSide=" + this.obfSide +
            ", desc='" + this.desc + '\'' +
            '}';
    }
}
