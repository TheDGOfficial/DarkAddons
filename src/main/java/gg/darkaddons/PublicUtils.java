package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraftforge.client.event.ClientChatReceivedEvent;

import java.io.File;

/**
 * Exports public utility methods that must be accessible outside, redirecting them to {@link Utils}.
 */
public final class PublicUtils {
    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private PublicUtils() {
        super();

        throw Utils.staticClassException();
    }

    /**
     * Calls {@link Throwable#printStackTrace()} on the given {@link Throwable}.
     *
     * @param throwable The {@link Throwable}.
     */
    public static final void printStackTrace(@NotNull final Throwable throwable) {
        Utils.printStackTrace(throwable);
    }

    /**
     * Calls {@link java.io.PrintStream#println(String)} on the {@link System#err}
     * with the given argument.
     *
     * @param err The error to print to the standard error stream.
     */
    public static final void printErr(@NotNull final String err) {
        Utils.printErr(err);
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
    @NotNull
    public static final String removeControlCodes(@Nullable final String text) {
        return Utils.removeControlCodes(text);
    }

    /**
     * Parses the given string as a primitive int. This method doesn't throw
     * and is fast unlike {@link Integer#parseInt(String)}.
     *
     * @param text The text to parse.
     * @return The parsed int.
     */
    public static final int safeParseIntFast(@NotNull final String text) {
        return Utils.safeParseIntFast(text);
    }

    /**
     * Tests if the given event's type is a standard text message.
     *
     * @param event The event to check if its type is a standard text message.
     * @return True if the event's type is a standard text message; false otherwise.
     */
    public static final boolean isStandardTextMessage(@NotNull final ClientChatReceivedEvent event) {
        return MessageType.STANDARD_TEXT_MESSAGE.matches(event.type);
    }

    /**
     * Starts a Minecraft debug profiler section (in the Debug Pie Chart).
     *
     * @param sectionName The section name to use.
     */
    public static final void startProfilingSection(@NotNull final String sectionName) {
        McProfilerHelper.startSection(sectionName);
    }

    /**
     * Ends the last Minecraft debug profiler section.
     */
    public static final void endProfilingSection() {
        McProfilerHelper.endSection();
    }

    /**
     * Reads the given file and returns the contents as a string.
     *
     * @param file The file to read.
     * @return A new {@link String} of the file contents.
     */
    @NotNull
    public static final String read(@NotNull final File file) {
        return Utils.read(file);
    }

    /**
     * Backport of method from Java 19.
     *
     * @param numMappings The initial size.
     * @return The new initial size, taking into account the load factor of 0.75.
     */
    public static final int calculateHashMapCapacity(final int numMappings) {
        return Utils.calculateHashMapCapacity(numMappings);
    }

    /**
     * Backport of method from Java 19.
     *
     * @param thread The thread.
     * @return The ID of the thread, ensuring the getId method isn't overridden.
     */
    public static final long threadId(@NotNull final Thread thread) {
        return Utils.threadId(thread);
    }
}
