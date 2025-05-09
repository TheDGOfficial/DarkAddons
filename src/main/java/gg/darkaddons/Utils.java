package gg.darkaddons;

import gg.darkaddons.annotations.bytecode.Bridge;
import gg.darkaddons.annotations.bytecode.Synthetic;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

import java.net.URI;
import java.net.IDN;
import java.net.URL;
import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.time.Duration;

/**
 * Contains some useful utility methods that usually reduce code duplication
 * and allowing cleaner code.
 *
 * @apiNote This class is meant to be used internally; therefore, it is
 * currently package-private, although in the future it will be public when we
 * have too many classes in the root package.
 */
final class Utils {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private Utils() {
        super();

        throw Utils.staticClassException();
    }

    /**
     * Returns a new {@link UnsupportedOperationException} to throw in
     * private constructors, meant to be used by static classes.
     * <p>
     * A class becomes static when it only contains static members,
     * and thus constructing a new instance of it is pointless.
     *
     * @return A new {@link UnsupportedOperationException} to throw
     * in private constructors.
     */
    @NotNull
    static final UnsupportedOperationException staticClassException() {
        return new UnsupportedOperationException("static class");
    }

    /**
     * Returns a {@link HashMap} with the given key and value in it.
     * <p>
     * The returned map is meant to be not modified, although it is permitted,
     * it will be inefficient if you add more entries yourself, because of it
     * being initialized with capacity of 1.
     * <p>
     * This done to not waste space and have maximum performance, since default
     * capacity is 16, and we're only adding/using 1.
     *
     * @param key The key.
     * @param value The value.
     * @return A {@link HashMap} with the given key and value in it.
     * @param <K> The type of the key.
     * @param <V> The type of the value.
     *
     * @apiNote This method intentionally doesn't use {@link Map} as
     * a return type since we don't need the wide type, the method is clearly
     * named to imply returning a {@link HashMap}.
     */
    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    @NotNull
    static final <K, V> HashMap<K, V> hashMapOf(@NotNull final K key, @NotNull final V value) {
        final var map = new HashMap<K, V>(Utils.calculateHashMapCapacity(1));
        map.put(key, value);

        return map;
    }

    private static final class EnumChatFormattingHolder {
        /**
         * Private constructor since this class only contains static members.
         * <p>
         * Always throws {@link UnsupportedOperationException} (for when
         * constructed via reflection).
         */
        private EnumChatFormattingHolder() {
            super();

            throw Utils.staticClassException();
        }

        /**
         * Removes Minecraft color and formatting codes from the given string.
         * <p>
         * Note that unlike other methods this doesn't utilize a
         * {@link java.util.regex.Pattern} or {@link java.util.regex.Matcher} and
         * just uses simple {@link StringUtils#replace(String, String, String)}
         * against all known codes (0-9 a-f color codes plus formatting codes (k-r)
         * and z for chroma).
         *
         * @param text The text to remove Minecraft control codes from.
         * @return Empty string if the given text is null, or the given text
         * without control codes otherwise.
         */
        @Contract("null -> null")
        private static final String removeControlCodes(@Nullable final String text) {
            if (null == text) {
                return null;
            }

            final var length = text.length();

            if (0 == length) {
                return "";
            }

            var nextFormattingSequence = text.indexOf(Utils.CONTROL_START);

            if (-1 == nextFormattingSequence) {
                return text;
            }

            final var cleanedString = new StringBuilder(length);

            var readIndex = 0;

            while (-1 != nextFormattingSequence) {
                cleanedString.append(text, readIndex, nextFormattingSequence);

                readIndex = nextFormattingSequence + 2;
                nextFormattingSequence = text.indexOf(Utils.CONTROL_START, readIndex);

                readIndex = Math.min(length, readIndex);
            }

            cleanedString.append(text, readIndex, length);

            return cleanedString.toString();
        }
    }

    /**
     * The character used to signal the start of a formatting code.
     * <p>
     * We store this as a {@link String} since that has more performance
     * than an unboxed {@link Character}, although primitive char is more
     * performant, we need the {@link Character} for string operations.
     */
    @NotNull
    private static final String CONTROL_START = "§";

    /**
     * The character used to signal a new line under Unix platforms.
     * <p>
     * Windows and macOS use different line-ending characters, so do
     * not use this for reading/writing to files in disk.
     * <p>
     * One valid use case would be splitting something known to use
     * unix line endings by new lines and iterating over it.
     */
    @SuppressWarnings("HardcodedLineSeparator")
    @NotNull
    static final String UNIX_NEW_LINE = "\n";

    /**
     * Removes Minecraft color and formatting codes from the given string.
     * <p>
     * Note that unlike other methods this doesn't utilize a
     * {@link java.util.regex.Pattern} or {@link java.util.regex.Matcher} and
     * just uses simple {@link StringUtils#replace(String, String, String)}
     * against all known codes (0-9 a-f color codes plus formatting codes (k-r)
     * and z for chroma).
     *
     * @param text The text to remove Minecraft control codes from.
     * @return Empty string if the given text is null, or the given text
     * without control codes otherwise.
     */
    @NotNull
    static final String removeControlCodes(@Nullable final String text) {
        return Utils.EnumChatFormattingHolder.removeControlCodes(text);
    }

    /**
     * Fills an array with the given value.
     * <p>
     * This method is optimized to not calculate the length of array each iteration.
     * Also uses pre-increment instead of post-increment, i.e., ++i instead of i++.
     *
     * @param a The array to fill.
     * @param val The value to assign.
     */
    static final void fillFloatArray(final float @NotNull [] a, @SuppressWarnings("SameParameterValue") final float val) {
        final var len = a.length;
        for (var i = 0; i < len; ++i) {
            a[i] = val;
        }
    }

    /**
     * Fills an array with the given value.
     * <p>
     * This method is optimized to not calculate the length of array each iteration.
     * Also uses pre-increment instead of post-increment, i.e., ++i instead of i++.
     *
     * @param a The array to fill.
     * @param val The value to assign.
     */
    static final void fillIntArray(final int @NotNull [] a, @SuppressWarnings("SameParameterValue") final int val) {
        final var len = a.length;
        for (var i = 0; i < len; ++i) {
            a[i] = val;
        }
    }

    /**
     * Tests if an object equals to another (null-safe).
     *
     * @param first First object. The {@link Object#equals(Object)} method will be called in this object.
     * @param second Second object.
     * @return True if both objects are equal, false otherwise.
     */
    @SuppressWarnings({"EqualsReplaceableByObjectsCall", "StaticMethodOnlyUsedInOneClass"})
    static final boolean areEqual(@Nullable final Object first, @Nullable final Object second) {
        //noinspection ObjectEquality
        return first == second || null != first && first.equals(second);
    }

    /**
     * Tests if a string equals to another (null-safe).
     *
     * @param first First string. The {@link String#equals(Object)} method will be called in this string.
     * @param second Second string.
     * @return True if both strings are equal, false otherwise.
     */
    @SuppressWarnings("EqualsReplaceableByObjectsCall")
    static final boolean areStringsEqual(@Nullable final String first, @Nullable final String second) {
        return null != first && first.equals(second);
    }

    /**
     * Returns the first element that matches the given predicate, or null if no matches.
     *
     * @param elements The elements.
     * @param predicate The predicate.
     * @return The first element that matches the given predicate.
     * @param <T> The type of the elements.
     */
    @Nullable
    static final <T> T findElement(@NotNull final T[] elements, @NotNull final Predicate<? super T> predicate) {
        for (final var elem : elements) {
            if (predicate.test(elem)) {
                return elem;
            }
        }
        return null;
    }

    /**
     * Returns true if any of the elements matches the given predicate.
     *
     * @param elements The elements.
     * @param predicate The predicate.
     * @return True if any of the elements match the predicate, false otherwise.
     * @param <T> The type of the elements.
     */
    static final <T> boolean isAnyMatch(@NotNull final Iterable<? extends T> elements, @NotNull final Predicate<? super T> predicate) {
        for (final var elem : elements) {
            if (predicate.test(elem)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the first element yielding the smallest value of the given function returning double, or null if there are no elements.
     *
     * @param elements The elements.
     * @param doubleFunction The function that returns double for any given element.
     * @return The first element yielding the smallest value of the given function.
     * @param <T> The type of the elements.
     */
    @Nullable
    static final <T> T minValue(@NotNull final T[] elements, @NotNull final ToDoubleFunction<? super T> doubleFunction) {
        final var length = elements.length;

        if (0 == length) {
            return null;
        }

        var minElem = elements[0];
        var minValue = doubleFunction.applyAsDouble(minElem);

        for (var i = 1; i < length; ++i) {
            final var elem = elements[i];
            final var value = doubleFunction.applyAsDouble(elem);

            if (minValue > value) {
                minElem = elem;
                minValue = value;
            }
        }

        return minElem;
    }

    /**
     * Returns the last element on the given array of elements, or null if the element array is empty.
     *
     * @param elements The elements.
     * @return The last element on the given array of elements, or null if the element array is empty.
     * @param <T> The type of the elements.
     */
    @Nullable
    @SafeVarargs
    static final <T> T lastValue(@NotNull final T... elements) {
        return elements[elements.length - 1];
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

    @NotNull
    private static final JsonElement parseJsonFromString(@NotNull final String json) {
        return Utils.GsonHolder.jsonParser.parse(json);
    }

    @NotNull
    static final JsonObject parseJsonObjectFromString(@NotNull final String json) {
        return Utils.parseJsonFromString(json).getAsJsonObject();
    }

    private static final class GsonHolder {
        /**
         * Private constructor since this class only contains static members.
         * <p>
         * Always throws {@link UnsupportedOperationException} (for when
         * constructed via reflection).
         */
        private GsonHolder() {
            super();

            throw Utils.staticClassException();
        }

        @NotNull
        private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

        @NotNull
        private static final JsonParser jsonParser = new JsonParser();
    }

    @NotNull
    static final String toJson(@NotNull final JsonElement jsonElement) {
        return Utils.GsonHolder.gson.toJson(jsonElement);
    }

    @NotNull
    static final String chromaIfEnabledOrAqua() {
        return Utils.chromaIfEnabledOr('b');
    }

    @NotNull
    private static final String chromaIfEnabledOr(final char alternativeColor) {
        return Utils.CONTROL_START + (Config.isChromaToggle() && DarkAddons.isUsingSBA() ? 'z' : alternativeColor);
    }

    static final void reloadChunks() {
        Minecraft.getMinecraft().renderGlobal.loadRenderers();
    }

    @NotNull
    static final <T> Consumer<T> runnableToConsumer(@NotNull final Runnable runnable) {
        return (@Nullable final T value) -> runnable.run();
    }

    static final boolean toPrimitive(@Nullable final Boolean nullableBool) {
        return Utils.toPrimitive(nullableBool, false);
    }

    static final boolean toPrimitive(@Nullable final Boolean nullableBool, final boolean defaultValue) {
        return null == nullableBool ? defaultValue : nullableBool;
    }

    static final double toPrimitive(@Nullable final Double nullableDouble) {
        return Utils.toPrimitive(nullableDouble, 0.0D);
    }

    static final double toPrimitive(@Nullable final Double nullableDouble, final double defaultValue) {
        return null == nullableDouble ? defaultValue : nullableDouble;
    }

    @NotNull
    static final Thread newThread(@NotNull final Runnable r, @NotNull final String name) {
        final var thread = new Thread(r, name);
        thread.setDaemon(true);

        //noinspection CallToThreadSetPriority
        thread.setPriority(Thread.MIN_PRIORITY);
        thread.setUncaughtExceptionHandler((@NotNull final Thread t, @NotNull final Throwable e) -> DarkAddons.modError(e));

        return thread;
    }

    @SuppressWarnings("varargs")
    @SafeVarargs
    @NotNull
    static final <T> ArrayList<T> asArrayList(@NotNull final T... array) {
        final var arrayList = new ArrayList<T>(array.length);

        Collections.addAll(arrayList, array);
        return arrayList;
    }

    @NotNull
    static final String formatMillisecondsAsSeconds(final long ms) {
        return Utils.formatMillisecondsAsSecondsWithPrecision(ms, 2); // TODO make precision a config option
    }

    private static final String formatMillisecondsAsSecondsWithPrecision(final long ms, final int precision) {
        final var millis = switch (precision) {
            case 3 -> ms % 1_000L;
            case 2 -> ms / 10L % 100L;
            case 1 -> ms / 100L % 10L;
            case 0 -> 0L;
            default -> throw new IllegalArgumentException("invalid precision");
        };

        final var seconds = ms / 1_000L % 60L;
        return seconds + (0L == millis ? "s" : '.' + Long.toString(millis) + 's');
    }

    private static final boolean isNotDigit(final char c) {
        return '0' != c && '1' != c && '2' != c && '3' != c && '4' != c && '5' != c && '6' != c && '7' != c && '8' != c && '9' != c;
    }

    static final int safeParseIntFast(@NotNull final String text) {
        if (text.isEmpty()) {
            return -1;
        }

        return "0".equals(text) ? 0 : switch (text) {
            case "900000000" -> 900_000_000;
            case "1800000000" -> 1_800_000_000;
            case "100" -> 100;
            default -> {
                var answer = 0;
                var factor = 1;
                final var textLength = text.length();
                for (var i = textLength - 1; 0 <= i; --i) {
                    final var c = text.charAt(i);

                    if (Utils.isNotDigit(c)) {
                        yield -1;
                    }

                    //noinspection CharUsedInArithmeticContext
                    answer += (c - '0') * factor;
                    factor *= 10;
                }
                yield answer;
            }
        }; // fast-path for easy-case

        // other common fast-paths
    }

    /**
     * Awaits a condition being true, running the given continuation hook once it becomes true.
     * This will check the condition each tick, and the continuation hook will also run in the main thread.
     * <p>
     * If the initial call to this method was from another thread, as specified above, both the checks and the
     * continuation hook will now run from the main client thread. Care must be taken to ensure correct behavior in this
     * case for possible concurrency issues.
     * <p>
     * The hook will only be run once unless this method is called again, even if the condition remains being true.
     * <p>
     * As the method name implies, this method will not block. It is not a while loop, lock or a synchronization, it's a
     * fixed check of the condition every 50~ milliseconds (each tick). If the local client TPS is low, it might be delayed further,
     * but it will not freeze the game or the calling thread if it is not the main client thread.
     * <p>
     * Do note that this method will delay the running of the continuation to the next tick even if the condition was true
     * to start with. Optimal use case would be to only call this method if the condition is initially false, to not cause
     * unnecessary delays, like this:
     * <pre>
     * {@code
     * BooleanSupplier condition = () -> Minecraft.getMinecraft().thePlayer.isSprinting(); // Just an example condition.
     * Runnable action = () -> System.out.println("Started sprinting"); // Will only be printed once unless this code block is called again.
     *
     * if (!condition.getAsBoolean()) {
     *     Utils.awaitCondition(condition, action);
     * } else {
     *     action.run(); // Instantly run without delay if the condition was true to start with.
     * }
     * }
     * </pre>
     * However, this code might introduce concurrency issues if the code could be called outside the main thread. In that case,
     * if the condition was initially true, then the code would be run on the current thread, a.k.a. outside of the main thread. But
     * if it was initially false, the code will be run on the main thread once the condition becomes true.
     * <p>
     * This method will keep checking the condition till it becomes true, even if you, e.g., pass an always false condition, like this:
     * <pre>
     * {@code
     * Utils.awaitCondition(() -> false, () -> throw new IllegalStateException("should never run"));
     * }
     * </pre>
     * In that case, the continuation hook will never run, but the method will still keep checking the condition.
     *
     * @param condition The condition to await for.
     * @param continuation The continuation hook to run once the condition becomes true.
     */
    static final void awaitCondition(@NotNull final BooleanSupplier condition, @NotNull final Runnable continuation) {
        DarkAddons.runOnceInNextTick("await_condition", () -> {
            if (condition.getAsBoolean()) {
                continuation.run();
            } else {
                Utils.awaitCondition(condition, continuation); // No StackOverflowError since its called 1 times each tick (50ms) at most with a lot of other methods in the stack in between calls.
            }
        });
    }

    /**
     * Holds suffix values used internally in the {@link Utils#formatNumber(float)} method.
     */
    @SuppressWarnings({"CollectionDeclaredAsConcreteClass", "TypeMayBeWeakened", "StaticCollection"})
    @NotNull
    private static final TreeMap<Long, String> suffixes = new TreeMap<>();

    static {
        Utils.suffixes.put(1_000L, "k"); // A thousand
        Utils.suffixes.put(1_000_000L, "M"); // A million
        Utils.suffixes.put(1_000_000_000L, "B"); // A billion
        Utils.suffixes.put(1_000_000_000_000L, "T"); // A trillion
        Utils.suffixes.put(1_000_000_000_000_000L, "q"); // A quadrillion
        Utils.suffixes.put(1_000_000_000_000_000_000L, "Q"); // A quintillion
    }

    /**
     * Formats a number.
     *
     * @param number The number.
     * @return The number formatted with the {@link Utils#suffixes}.
     */
    @NotNull
    static final String formatNumber(final float number) {
        return Utils.formatNumber0((long) number);
    }

    /**
     * Formats a number.
     *
     * @param number The number.
     * @return The number formatted with the {@link Utils#suffixes}.
     */
    @SuppressWarnings("AssignmentToMethodParameter")
    @NotNull
    private static final String formatNumber0(long number) {
        var wasNegative = false;

        while (true) {
            // Long.MIN_VALUE == -Long.MIN_VALUE, so we need an adjustment here
            if (Long.MIN_VALUE == number) {
                number = Long.MIN_VALUE + 1L;
                wasNegative = false;
                continue;
            }

            if (0L <= number) {
                if (1_000L > number) { // deal with the easy case
                    return wasNegative ? '-' + Long.toString(number) : Long.toString(number);
                }

                final var entry = Utils.suffixes.floorEntry(number);
                final var returnValue = Utils.getString(number, entry);

                return wasNegative ? '-' + returnValue : returnValue;
            }

            number = -number;
            wasNegative = true;
        }
    }

    @NotNull
    private static final String getString(final long number, @NotNull final Map.Entry<Long, String> entry) {
        final long divideBy = entry.getKey();
        final var suffix = entry.getValue();

        final var truncated = number / (divideBy / 10L); // the number part of the output times 10
        final var hasDecimal = 100L > truncated && !Utils.compareDoubleToLongExact(truncated / 10.0D, truncated / 10L);

        //noinspection ConditionalCanBePushedInsideExpression
        return hasDecimal ? truncated / 10.0D + suffix : truncated / 10L + suffix;
    }

    @SuppressWarnings({"FloatingPointEquality", "strictfp"})
    static final strictfp boolean compareFloatExact(final float f, final float test) {
        return test == f;
    }

    @SuppressWarnings({"FloatingPointEquality", "strictfp"})
    static final strictfp boolean compareDoubleExact(final double d, final double test) {
        return test == d;
    }

    @SuppressWarnings({"FloatingPointEquality", "strictfp"})
    private static final strictfp boolean compareDoubleToLongExact(final double d, final long l) {
        return d == l;
    }

    @SuppressWarnings({"FloatingPointEquality", "strictfp"})
    static final strictfp boolean compareDoubleToIntExact(final double d, final int i) {
        return i == d;
    }

    @Nullable
    private static final ItemStack getHeldItemStack(@NotNull final Minecraft mc) {
        return mc.thePlayer.getHeldItem();
    }

    @Nullable
    static final Item getHeldItem(@NotNull final Minecraft mc) {
        return Utils.getItem(Utils.getHeldItemStack(mc));
    }

    @Nullable
    private static final Item getItem(@Nullable final ItemStack itemStack) {
        return null == itemStack ? null : itemStack.getItem();
    }

    static final boolean isHoldingItemContaining(@NotNull final Minecraft mc, @SuppressWarnings("TypeMayBeWeakened") @NotNull final String search) {
        final var itemStack = Utils.getHeldItemStack(mc);

        return null != itemStack && itemStack.getDisplayName().contains(search);
    }

    private static final void setupConnectionProperties(@NotNull final URLConnection con,
                                                        @NotNull final Duration timeout,
                                                        @NotNull final String charset,
                                                        @NotNull final String contentType,
                                                        @NotNull final String userAgent,
                                                        @Nullable final String referer) {
        con.setAllowUserInteraction(false);

        if (con instanceof final HttpURLConnection conn) {

            conn.setInstanceFollowRedirects(true);
        }

        con.setDoOutput(false);
        //con.setUseCaches(false);

        final var actualTimeout = Math.toIntExact(timeout.toMillis());
        con.setConnectTimeout(actualTimeout);
        con.setReadTimeout(actualTimeout);

        con.setRequestProperty("Method", "GET");

        final var actualCharset = charset.toUpperCase(Locale.ROOT);
        con.setRequestProperty("Charset", actualCharset);
        con.setRequestProperty("Encoding", actualCharset);

        final var contentTypeHeader = contentType + "; charset=" + charset;
        con.setRequestProperty("Content-Type", contentTypeHeader.trim());

        con.setRequestProperty("Accept", "*/*");

        con.setRequestProperty("User-Agent", userAgent.trim());

        if (null != referer) {
            con.setRequestProperty("Referer", referer.trim());
        }
    }

    @NotNull
    private static final String getResponse(@NotNull final URLConnection con) throws IOException {
        try (final var is = con.getInputStream()) {
            try (final var in = new BufferedInputStream(is)) {
                final var encoding = con.getContentEncoding();

                if (null != encoding) {
                    if ("gzip".equalsIgnoreCase(encoding)) {
                        try (final var gzipIs = new GZIPInputStream(in)) {
                            try (final var gzip = new BufferedInputStream(gzipIs)) {
                                return StringUtils.remove(Utils.readString(gzip), '\n');
                            }
                        }
                    }
                    if ("deflate".equalsIgnoreCase(encoding)) {
                        try (final var inf = new InflaterInputStream(in, new Inflater(true))) {
                            try (final var deflate = new BufferedInputStream(inf)) {
                                return StringUtils.remove(Utils.readString(deflate), '\n');
                            }
                        }
                    }
                }

                return StringUtils.remove(Utils.readString(in), '\n');
            }
        }
    }

    @NotNull
    private static final String readString(@NotNull final InputStream is) throws IOException {
        try (final var ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            try (final var br = new BufferedReader(ir)) {
                String line;

                final var responseBody = new StringBuilder(4_096);
                while (null != (line = br.readLine())) {
                    responseBody.append(line.trim()).append('\n');
                }

                return responseBody.toString().trim();
            }
        }
    }

    @NotNull
    private static final URL urlOf(@NotNull final String address) throws MalformedURLException, URISyntaxException {
        final var url = new URL(address);

        return new URI(url.getProtocol(), url.getUserInfo(), IDN.toASCII(url.getHost()), url.getPort(), url.getPath(), url.getQuery(), url.getRef()).toURL();
    }

    @Nullable
    static final String sendWebRequest(@NotNull final String url) {
        return Utils.sendWebRequest(url, "text/plain", true);
    }

    @Nullable
    static final String sendWebRequest(@NotNull final String url, @NotNull final String contentTypeWithoutEncoding, final boolean setUserAgentAndReferer) {
        return Utils.sendWebRequest(url, contentTypeWithoutEncoding, setUserAgentAndReferer, 5L);
    }

    @Nullable
    static final String sendWebRequest(@NotNull final String url, @NotNull final String contentTypeWithoutEncoding, final boolean setUserAgentAndReferer, final long timeoutSeconds) {
        try {
            final var con = Utils.urlOf(url).openConnection();

            // Connection settings
            final var timeout = Duration.ofSeconds(timeoutSeconds);

            final String userAgent;
            @Nullable final String referer;

            if (setUserAgentAndReferer) {
                userAgent = DarkAddons.MOD_NAME + '/' + Reference.VERSION;
                referer = Thread.currentThread().getName();
            } else {
                userAgent = "Mozilla/5.0";
                referer = null;
            }

            final var charset = "utf-8";
            Utils.setupConnectionProperties(con, timeout, charset, contentTypeWithoutEncoding, userAgent, referer);

            return Utils.getResponse(con);
        } catch (final MalformedURLException | URISyntaxException e) {
            DarkAddons.modError(e);
        } catch (final IOException ignored) {
            // Likely no internet connection, fall through to return null
        }

        return null;
    }

    @Synthetic
    @Bridge
    static final int fastRomanToInt(@NotNull final String number) {
        return switch (number) {
            case "I" -> 1;
            case "II" -> 2;
            case "III" -> 3;
            case "IV" -> 4;
            case "V" -> 5;
            case "VI" -> 6;
            case "VII" -> 7;
            default -> Utils.fastRomanToInt2(number);
        };
    }

    @Synthetic
    @Bridge
    private static final int fastRomanToInt2(@NotNull final String number) {
        return switch (number) {
            case "VIII" -> 8;
            case "IX" -> 9;
            case "X" -> 10;
            case "XI" -> 11;
            case "XII" -> 12;
            case "XIII" -> 13;
            case "XIV" -> 14;
            default -> Utils.fastRomanToInt3(number);
        };
    }

    @Synthetic
    @Bridge
    private static final int fastRomanToInt3(@NotNull final String number) {
        return switch (number) {
            case "XV" -> 15;
            case "XVI" -> 16;
            case "XVII" -> 17;
            case "XVIII" -> 18;
            case "XIX" -> 19;
            case "XX" -> 20;
            case "XXI" -> 21;
            default -> Utils.fastRomanToInt4(number);
        };
    }

    @Synthetic
    @Bridge
    private static final int fastRomanToInt4(@NotNull final String number) {
        return switch (number) {
            case "XXII" -> 22;
            case "XXIII" -> 23;
            case "XXIV" -> 24;
            case "XXV" -> 25;
            case "XXVI" -> 26;
            case "XXVII" -> 27;
            case "XXVIII" -> 28;
            default -> Utils.fastRomanToInt5(number);
        };
    }

    @Synthetic
    @Bridge
    private static final int fastRomanToInt5(@NotNull final String number) {
        return switch (number) {
            case "XXIX" -> 29;
            case "XXX" -> 30;
            case "XXXI" -> 31;
            case "XXXII" -> 32;
            case "XXXIII" -> 33;
            case "XXXIV" -> 34;
            case "XXXV" -> 35;
            default -> Utils.romanToInt(number);
        };
    }

    private static final int romanToInt(@SuppressWarnings("TypeMayBeWeakened") @NotNull final String number) {
        var accumulator = 0;

        var i = 0;
        final var numberLength = number.length();
        while (i < numberLength) {
            final var c = number.charAt(i);

            final var nextIndex = i + 1;
            final var nextChar = nextIndex < numberLength ? number.charAt(nextIndex) : ' ';

            final var num = Utils.romanHash(c);

            if ('I' == c && ('V' == nextChar || 'X' == nextChar)) {
                accumulator += Utils.romanHash(nextChar) - num;
                ++i;
            } else {
                accumulator += num;
            }

            ++i;
        }

        return accumulator;
    }

    private static final int romanHash(final char c) {
        return switch (c) {
            case 'I' -> 1;
            case 'V' -> 5;
            case 'X' -> 10;
            default -> 0;
        };
    }

    @NotNull
    private static final ThreadGroup getRootThreadGroup() {
        var threadGroup = Thread.currentThread().getThreadGroup();

        ThreadGroup parent;
        while (null != threadGroup && null != (parent = threadGroup.getParent())) {
            threadGroup = parent;
        }

        //noinspection IfCanBeAssertion
        if (null == threadGroup) {
            throw new IllegalStateException("can't find root thread group");
        }

        return threadGroup;
    }

    @NotNull
    static final Thread @Nullable [] getAllThreads() {
        final var threadGroup = Utils.getRootThreadGroup();
        var count = threadGroup.activeCount();

        Thread[] threads;
        do {
            threads = new Thread[count];
            count = threadGroup.enumerate(threads, true);
        } while (count > threads.length);

        //noinspection IfCanBeAssertion
        if (1 > threads.length) {
            throw new IllegalStateException("getAllThreads failure: couldn't even find the currently executing thread let alone all threads");
        }

        return threads;
    }

    private static final class LoggerHolder {
        /**
         * Private constructor since this class only contains static members.
         * <p>
         * Always throws {@link UnsupportedOperationException} (for when
         * constructed via reflection).
         */
        private LoggerHolder() {
            super();

            throw Utils.staticClassException();
        }

        @NotNull
        private static final Logger LOGGER = LogManager.getLogger();
    }

    static final long threadId(@NotNull final Thread thread) {
        final var id = thread.getId();

        final var originalThreadClass = Thread.class;
        final var clazz = thread.getClass();

        //noinspection ObjectEquality
        if (originalThreadClass != clazz) {
            try {
                //noinspection ObjectEquality
                if (originalThreadClass != clazz.getMethod("getId").getDeclaringClass()) {
                    Utils.LoggerHolder.LOGGER.warn("Thread {} derives its own thread class from {} at {}, which overrides the getId() method. This method is not meant to be overridden.", thread.getName(), originalThreadClass.getName(), clazz.getName());
                }
            } catch (final NoSuchMethodException noSuchMethodException) {
                throw new IllegalStateException(noSuchMethodException); // Should never happen
            }
        }

        return id;
    }

    static final String bytesToString(final long bytes) {
        final var kb = bytes >> 10;
        final var mb = kb >> 10;
        final var gb = mb >> 10;

        return 1L <= gb ? gb + " GB" : 1L <= mb ? mb + " MB" : 1L <= kb ? kb + " kB" : bytes + " bytes";
    }

    @NotNull
    static final String formatTime(final long time, final boolean includeSeconds) {
        final var seconds = time / 1_000L;
        final var minutes = seconds / 60L;
        final var hours = minutes / 60L;
        final var days = hours / 24L;

        return Utils.formatTime(days, hours % 24L, minutes % 60L, includeSeconds ? seconds % 60L : 0L, includeSeconds ? time % 1_000L : 0L);
    }

    @NotNull
    private static final String formatTime(final long days, final long hours, final long minutes, final long seconds, final long milliseconds) {
        final var stringBuilder = new StringBuilder(16);

        if (0L != days) {
            stringBuilder.append(days).append("d ");
        }

        if (0L != hours) {
            stringBuilder.append(hours).append("h ");
        }

        if (0L != minutes) {
            stringBuilder.append(minutes).append("m ");
        }

        if (0L != seconds) {
            stringBuilder.append(seconds).append("s ");
        }

        if (stringBuilder.toString().isEmpty()) {
            stringBuilder.append(milliseconds).append("ms ");
        }

        final var result = stringBuilder.toString();

        return !result.isEmpty() && ' ' == result.charAt(result.length() - 1) ? StringUtils.removeEnd(result, " ") : result;
    }

    /**
     * Backport of method from Java 19.
     *
     * @param numMappings The initial size.
     * @return The new initial size, taking into account the load factor of 0.75.
     */
    static final int calculateHashMapCapacity(final int numMappings) {
        if (0 > numMappings) {
            throw new IllegalArgumentException("Negative number of mappings: " + numMappings);
        }

        return (int) Math.ceil(numMappings / 0.75D);
    }

    static final boolean checkBossName(@NotNull final int floor, @NotNull final String bossName) {
        final var correctBoss = switch (floor) {
            case 0 -> "The Watcher";
            case 1 -> "Bonzo";
            case 2 -> "Scarf";
            case 3 -> "The Professor";
            case 4 -> "Thorn";
            case 5 -> "Livid";
            case 6 -> "Sadan";
            case 7 -> "Maxor";
            default -> null;
        };

        return null != correctBoss && bossName.endsWith(correctBoss);
    }
}
