package gg.darkaddons;

import com.sun.management.GarbageCollectionNotificationInfo;
import com.sun.management.HotSpotDiagnosticMXBean;
import com.sun.management.ThreadMXBean;
import gg.darkaddons.mixin.MixinUtils;
import gg.skytils.skytilsmod.Skytils;
import gg.essential.elementa.ElementaVersion;
import kotlin.KotlinVersion;
import net.minecraft.client.Minecraft;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.Sys;
import org.spongepowered.asm.mixin.MixinEnvironment;

import javax.management.ListenerNotFoundException;
import javax.management.Notification;
import javax.management.NotificationBroadcaster;
import javax.management.NotificationListener;
import javax.management.openmbean.CompositeData;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.IBossDisplayData;

import net.minecraft.tileentity.TileEntitySkull;

import net.minecraft.client.gui.GuiNewChat;

final class Diagnostics {
    @NotNull
    private static final Logger LOGGER = LogManager.getLogger();

    /**
     * Private constructor since this class only contains static members.
     * <p>
     * Always throws {@link UnsupportedOperationException} (for when
     * constructed via reflection).
     */
    private Diagnostics() {
        super();

        throw Utils.staticClassException();
    }

    @SuppressWarnings({"CollectionDeclaredAsConcreteClass", "TypeMayBeWeakened"})
    @NotNull
    private static final LinkedHashMap<String, String> diagnosticData = new LinkedHashMap<>(Utils.calculateHashMapCapacity(0));

    @SuppressWarnings("LambdaCanBeReplacedWithAnonymous")
    @NotNull
    private static final ExecutorService diagnosticsExecutor = Executors.newSingleThreadExecutor((@NotNull final Runnable r) -> Utils.newThread(r, "DarkAddons Diagnostics Thread"));

    static final void init() {
        if (!Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            throw new IllegalStateException("not allowed to run static initializer from thread " + Thread.currentThread().getName() + " which is not the client thread");
        }
    }

    static final long MC_THREAD_ID = Diagnostics.getMcThreadId();

    private static final long getMcThreadId() {
        if (Minecraft.getMinecraft().isCallingFromMinecraftThread()) {
            return Utils.threadId(Thread.currentThread());
        }
        throw new IllegalStateException("not allowed to run static initializer from thread " + Thread.currentThread().getName() + " which is not the client thread");
    }

    static {
        Utils.newThread(Diagnostics::watchdogLoop, "DarkAddons Watchdog Thread").start();
    }

    private static final void diag(@NotNull final String name, @NotNull final String value) {
        Diagnostics.diagnosticData.put(name, value);
    }

    @NotNull
    private static final String getSkytilsVersion() {
        // TODO remove or update this to catch NoClassDefFoundError once no longer depending on Skytils
        try {
            // Since the field is a static final, if we use it directly without reflection, it gets inlined and the version
            // is always the version we use as a dependency on DarkAddons. To get the actual runtime version the user is using,
            // we have to access the field via reflection. Thankfully, even after inlining the string value of the field, the Java
            // compiler still keeps the field around, so we can do this.
            return Skytils.class.getField("VERSION").get(null).toString();
        } catch (final NoSuchFieldException | IllegalAccessException ignored) {
            // The field is public and exists at nearly all versions, if this happens we are probably in a future where Skytils devs changed
            // the visibility or name of the field, return unknown.
            return "unknown";
        }
    }

    @Nullable
    private static final GuiNewChat getGuiNewChat() {
        return Minecraft.getMinecraft().ingameGUI.getChatGUI();
    }

    @SuppressWarnings("unchecked")
    @Nullable
    private static final <T, V> V getPrivateFieldValue(@NotNull final Class<? extends T> clazz, @NotNull final T instance, @NotNull final String name) {
        Field field = null;
        try {
            field = clazz.getDeclaredField(name);
        } catch (final NoSuchFieldException nsfe) {
            DarkAddons.modError(nsfe);
        }
        if (null == field) {
            return null;
        }
        field.setAccessible(true);
        V value = null;
        try {
            value = (V) field.get(instance);
        } catch (final IllegalAccessException iae) {
            DarkAddons.modError(iae);
        }
        field.setAccessible(false);
        return value;
    }

    @Nullable
    private static final List<?> getChatLines(@NotNull final GuiNewChat chat) {
        if ("net.labymod.core_implementation.mc18.gui.GuiChatAdapter".equals(chat.getClass().getName())) {
            // LabyMod has a second chat so this gets the main chat instance
            final var mainChatRenderer = Diagnostics.getPrivateFieldValue(chat.getClass(), chat, "chatMain");

            // Return type is ArrayList<ChatLine> but use List<?> since we don't care about specific list implementation and content type, we only need it for calling .size() - NOTE: The ChatLine class in the ArrayList<ChatLine> is different from vanillas when the chat implementation is LabyMod.
            return Diagnostics.getPrivateFieldValue(Objects.requireNonNull(mainChatRenderer, "var mainChatRenderer from field chatMain should not be null").getClass().getSuperclass() /* This returns the base chat class which both the main and second chats extend from */, mainChatRenderer, "chatLines" /* And that base class has this field that stores the lines */);
        }

        // Return type is ArrayList<ChatLine> but use List<?> since we don't care about the specific list implementation and the content type, we only need it for calling .size()
        return Diagnostics.getPrivateFieldValue(GuiNewChat.class, chat, "field_146252_h");
    }

    @Nullable
    private static final List<?> getDrawnChatLines(@NotNull final GuiNewChat chat) {
        // Return type is ArrayList<ChatLine> but use List<?> since we don't care about the specific list implementation and the content type, we only need it for calling .size()
        return "net.labymod.core_implementation.mc18.gui.GuiChatAdapter".equals(chat.getClass().getName()) ? Diagnostics.getChatLines(chat) : Diagnostics.getPrivateFieldValue(GuiNewChat.class, chat, "field_146253_i");
    }

    private static final void diagChat() {
        final var chat = Diagnostics.getGuiNewChat();

        final var sentMessages = null == chat ? -1 : chat.getSentMessages().size();

        final var chatLines = null == chat ? null : Diagnostics.getChatLines(chat);
        final var drawnChatLines = null == chat ? null : Diagnostics.getDrawnChatLines(chat);

        final var chatLinesSize = null == chatLines ? -1 : chatLines.size();
        final var drawnChatLinesSize = null == drawnChatLines ? -1 : drawnChatLines.size();

        Diagnostics.diag("Chat Implementation", null == chat ? "could not detect" : chat.getClass().getName());
        Diagnostics.diag("Chat Stats", "Sent Message History Size: " + sentMessages + ", Chat Lines: " + chatLinesSize + ", Drawn Chat Lines: " + drawnChatLinesSize);
    }

    private static final void loadDiagnosticData() {
        Diagnostics.diag("DarkAddons Version", Reference.VERSION);

        Diagnostics.diag("OS Details", System.getProperty("os.name") + ' ' + StringUtils.remove(System.getProperty("os.version"), '-' + System.getProperty("os.arch")) + ' ' + System.getProperty("os.arch"));
        Diagnostics.diag("Java Version", System.getProperty("java.version"));
        Diagnostics.diag("LWJGL Version", Sys.getVersion());
        Diagnostics.diag("Mixin Version", MixinEnvironment.getCurrentEnvironment().getVersion()); // TODO essential bundles old mixin version
        Diagnostics.diag("Kotlin Version", KotlinVersion.CURRENT.toString()); // TODO essential bundles old kotlin version
        Diagnostics.diag("Skytils Version", Diagnostics.getSkytilsVersion());
        Diagnostics.diag("Elementa Version", Diagnostics.getLatestSupportedRuntimeElementaVersion());

        final var world = Minecraft.getMinecraft().theWorld;
        Diagnostics.diag("Loaded Entity Amount", Integer.toString(world.loadedEntityList.size()));
        Diagnostics.diag("Loaded Tile Entity Amount", Integer.toString(world.loadedTileEntityList.size()));
        Diagnostics.diag("Loaded Player Entity Amount", Integer.toString(world.playerEntities.size()));

        Diagnostics.diag("Thread Count", "Total: " + ThreadPriorityTweaker.getThreadCount(false) + " - Daemon: " + ThreadPriorityTweaker.getThreadCount(true));

        Diagnostics.diagChat();

        Diagnostics.diag("Last Game Loop Time", Diagnostics.getLastGameLoopTimeString() + " (" + Diagnostics.getFPSWithNanosecondPrecision() + " fps) [" + MixinUtils.getLastTicksRan() + " ticks ran, taking " + Diagnostics.getLastFrameTickTimeString() + ']');
    }

    @NotNull
    static final long getFPSWithNanosecondPrecision() {
        return TimeUnit.SECONDS.toNanos(1L) / Math.max(1L, Diagnostics.getLastGameLoopTimeNs());
    }

    @NotNull
    static final String getLatestSupportedRuntimeElementaVersion() {
        final var value = Utils.lastValue(ElementaVersion.values());

        return null == value ? "Unknown" : value.name();
    }

    static final void dumpDiagnosticData(@SuppressWarnings("BoundedWildcard") @NotNull final Consumer<String> outputConsumer) {
        Diagnostics.diagnosticData.clear();
        Diagnostics.loadDiagnosticData();

        //noinspection StreamToLoop,LambdaCanBeReplacedWithAnonymous
        Diagnostics.diagnosticData.forEach((@NotNull final String key, @NotNull final String value) -> outputConsumer.accept(key + ": " + value));
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void dumpEntities(@SuppressWarnings("BoundedWildcard") @NotNull final Consumer<String> outputConsumer) {
        for (final var entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            var extra = "";

            if (entity instanceof final EntityLivingBase living) {
                final var hp = living.getHealth();
                final var maxHp = living.getMaxHealth();

                final var hpPerc = hp / maxHp * 100.0F;

                extra += " with HP " + hp + '/' + maxHp + " (%" + hpPerc + ')';
            }

            if (entity instanceof final IBossDisplayData boss) {
                final var hp = boss.getHealth();
                final var maxHp = boss.getMaxHealth();

                final var hpPerc = hp / maxHp * 100.0F;

                extra += " and with boss HP " + hp + '/' + maxHp + " (%" + hpPerc + ')';
            }

            outputConsumer.accept(entity.getClass().getSimpleName() + " with name " + entity.getName() + "§r§e" + extra);
        }
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void dumpSkulls(@SuppressWarnings("BoundedWildcard") @NotNull final Consumer<String> outputConsumer) {
        final var skullType = new HashMap<Integer, Integer>(Utils.calculateHashMapCapacity(100));
        final var texturesValue = new HashMap<String, Integer>(Utils.calculateHashMapCapacity(100));
        final var texturesSignature = new HashMap<String, Integer>(Utils.calculateHashMapCapacity(100));
        for (final var tileEntity : Minecraft.getMinecraft().theWorld.loadedTileEntityList) {
            if (tileEntity instanceof final TileEntitySkull skull) {
                final var gameProfile = skull.getPlayerProfile();
                if (null != gameProfile) {
                    final var texturesProperty = gameProfile.getProperties().get("textures").iterator().next();
                    //noinspection HttpUrlsUsage
                    texturesValue.merge(StringUtils.replace(StringUtils.substringBefore(StringUtils.substringAfter(new String(Base64.getDecoder().decode(texturesProperty.getValue()), StandardCharsets.UTF_8), "\"url\":\""), "\""), "http://", "https://"), 1, Integer::sum);
                    texturesSignature.merge(texturesProperty.hasSignature() ? texturesProperty.getSignature() : "null", 1, Integer::sum);
                }
                skullType.merge(skull.getSkullType(), 1, Integer::sum);
            }
        }
        // This will be the type of the skull, i.e., player (0), skeleton (3), etc.
        skullType.forEach((key, value) -> outputConsumer.accept("Skull Type " + key + " appeared " + value + " times."));
        // This will be a link to the png image of the skin from the official Minecraft site.
        texturesValue.forEach((key, value) -> outputConsumer.accept("Skull Owner Textures Value " + key + " appeared " + value + " times."));
        // Most of the time this null, but sometimes it has helpful markers like BEACH_BALL for furniture cosmetics.
        texturesSignature.forEach((key, value) -> outputConsumer.accept("Skull Owner Textures Signature " + key + " appeared " + value + " times."));
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void dumpEntityTypes(@SuppressWarnings("BoundedWildcard") @NotNull final Consumer<String> outputConsumer) {
        final var nonDuplicateTypes = new HashMap<String, Integer>(Utils.calculateHashMapCapacity(16));
        for (final var entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            final var type = entity.getClass().getSimpleName();
            nonDuplicateTypes.merge(type, 1, Integer::sum);
        }
        nonDuplicateTypes.forEach((key, value) -> outputConsumer.accept(key + " (" + value + ')'));
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void dumpTileEntityTypes(@SuppressWarnings("BoundedWildcard") @NotNull final Consumer<String> outputConsumer) {
        final var nonDuplicateTypes = new HashMap<String, Integer>(Utils.calculateHashMapCapacity(16));
        for (final var tileEntity : Minecraft.getMinecraft().theWorld.loadedTileEntityList) {
            final var type = tileEntity.getClass().getSimpleName();
            nonDuplicateTypes.merge(type, 1, Integer::sum);
        }
        nonDuplicateTypes.forEach((key, value) -> outputConsumer.accept(key + " (" + value + ')'));
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void dumpPlayerEntities(@SuppressWarnings("BoundedWildcard") @NotNull final Consumer<String> outputConsumer) {
        for (final var playerEntity : Minecraft.getMinecraft().theWorld.playerEntities) {
            outputConsumer.accept(playerEntity.getClass().getSimpleName() + " with name " + playerEntity.getName() + "§r§e");
        }
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void dumpThreadNames(@SuppressWarnings("BoundedWildcard") @NotNull final Consumer<String> outputConsumer) {
        final var threadNameDupes = new HashMap<String, Integer>(Utils.calculateHashMapCapacity(100));
        for (final var thread : Utils.getAllThreads()) {
            threadNameDupes.merge(thread.getName(), 1, Integer::sum);
        }
        threadNameDupes.forEach((key, value) -> outputConsumer.accept(key + " (" + value + ')'));
    }

    private static final void dumpThread0(@NotNull final ThreadInfo threadInfo, @NotNull final Consumer<String> outputConsumer) {
        Diagnostics.dumpGeneralInfo(threadInfo, outputConsumer);
        final var lockedMonitors = threadInfo.getLockedMonitors();
        final var indent = "    ";
        if (0 < lockedMonitors.length) {
            outputConsumer.accept("Thread is waiting on monitor(s):");
            for (final var monitor : lockedMonitors) {
                outputConsumer.accept(indent + "Locked on: " + monitor.getLockedStackFrame());
            }
        }
        final var lockOwner = threadInfo.getLockOwnerId();
        outputConsumer.accept("Lock Owner Thread ID: " + lockOwner);
        final var stackTraceElements = threadInfo.getStackTrace();
        if (0 < stackTraceElements.length) {
            outputConsumer.accept("Stacktrace:");
            for (final var stackTraceElement : stackTraceElements) {
                outputConsumer.accept(indent + stackTraceElement);
            }
        } else {
            outputConsumer.accept("No stacktrace");
        }
    }

    private static final void dumpGeneralInfo(@NotNull final ThreadInfo threadInfo, @SuppressWarnings("BoundedWildcard") @NotNull final Consumer<String> outputConsumer) {
        outputConsumer.accept("-------------------------------");
        outputConsumer.accept("Thread Name: " + threadInfo.getThreadName());
        outputConsumer.accept("Thread PID: " + threadInfo.getThreadId());
        outputConsumer.accept("Suspended State: " + threadInfo.isSuspended());
        outputConsumer.accept("JNI State: " + threadInfo.isInNative());
        outputConsumer.accept("Thread State: " + threadInfo.getThreadState());
        outputConsumer.accept("Blocked Time: " + threadInfo.getBlockedTime());
        outputConsumer.accept("Blocked Count: " + threadInfo.getBlockedCount());
    }

    private static final long SLEEP_MILLIS = 401L; // Magic number, 200L is default MaxGCPauseMillis and 201L is default MaxGCPauseIntervalMillis, so use 401L.

    private static final void sleep() {
        Diagnostics.sleep0(Diagnostics.SLEEP_MILLIS);
    }

    private static final void sleep0(final long millis) {
        try {
            Thread.sleep(millis);
        } catch (final InterruptedException ie) {
            DarkAddons.handleInterruptedException(ie);
        }
    }

    static final void dumpThread(@NotNull final Thread thread, @NotNull final Consumer<String> outputConsumer) {
        Diagnostics.diagnosticsExecutor.execute(() -> {
            // Wait a bit for dumpThread and ExecutorService#execute methods to go away from stack trace since we just got out of client thread.
            Diagnostics.sleep();

            Diagnostics.dumpThread0(ManagementFactory.getThreadMXBean().getThreadInfo(Utils.threadId(thread), Integer.MAX_VALUE), outputConsumer);
        });
    }

    static final void dumpAllThreads(@NotNull final Consumer<String> outputConsumer) {
        Diagnostics.diagnosticsExecutor.execute(() -> {
            // Wait a bit for dumpThread and ExecutorService#execute methods to go away from stack trace since we just got out of client thread.
            Diagnostics.sleep();

            final var threadInfos = Arrays.asList(ManagementFactory.getThreadMXBean().dumpAllThreads(true, true));
            Collections.reverse(threadInfos);

            for (final var threadInfo : threadInfos) {
                Diagnostics.dumpThread0(threadInfo, outputConsumer);
            }
        });
    }

    private static final class HotSpotDiagnosticMXBeanHolder {
        private HotSpotDiagnosticMXBeanHolder() {
            super();

            throw Utils.staticClassException();
        }

        @Nullable
        private static final HotSpotDiagnosticMXBean HOT_SPOT_DIAGNOSTIC_MX_BEAN = Diagnostics.HotSpotDiagnosticMXBeanHolder.getHotSpotDiagnosticMXBean();

        @Nullable
        private static final HotSpotDiagnosticMXBean getHotSpotDiagnosticMXBean() {
            try {
                return ManagementFactory.newPlatformMXBeanProxy(ManagementFactory.getPlatformMBeanServer(), "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
            } catch (final IOException e) {
                DarkAddons.modError(e);
                return null;
            }
        }
    }

    static final void dumpHeap(final boolean liveOnly, @NotNull final Runnable callback) {
        Diagnostics.diagnosticsExecutor.execute(() -> {
            // Wait a bit for dumpThread and ExecutorService#execute methods to go away from stack trace since we just got out of client thread.
            Diagnostics.sleep();

            try {
                final var hotSpotDiagnosticMXBean = Diagnostics.HotSpotDiagnosticMXBeanHolder.HOT_SPOT_DIAGNOSTIC_MX_BEAN;
                //noinspection IfCanBeAssertion
                if (null == hotSpotDiagnosticMXBean) {
                    //noinspection ThrowCaughtLocally
                    throw new IOException("no hot spot diagnostic mx bean found");
                }

                final var heapdumpsFolder = Paths.get("heapdumps");

                if (!Files.exists(heapdumpsFolder)) {
                    Files.createDirectory(heapdumpsFolder);
                }

                final var dumpFilePath = Paths.get("heapdumps", "heapdump." + (liveOnly ? "live." : "full.") + "hprof");

                if (Files.exists(dumpFilePath)) {
                    Files.delete(dumpFilePath);
                }

                hotSpotDiagnosticMXBean.dumpHeap(dumpFilePath.toString(), liveOnly);
                DarkAddons.runOnceInNextTick("dump_heap_callback", callback);
            } catch (final IOException e) {
                DarkAddons.modError(e);
            }
        });
    }

    static final void modifyVMOption(@NotNull final String optionName, @NotNull final String optionValue) {
        Diagnostics.diagnosticsExecutor.execute(() -> {
            // Wait a bit for dumpThread and ExecutorService#execute methods to go away from stack trace since we just got out of client thread.
            Diagnostics.sleep();

            try {
                final var hotSpotDiagnosticMXBean = Diagnostics.HotSpotDiagnosticMXBeanHolder.HOT_SPOT_DIAGNOSTIC_MX_BEAN;
                //noinspection IfCanBeAssertion
                if (null == hotSpotDiagnosticMXBean) {
                    //noinspection ThrowCaughtLocally
                    throw new IOException("no hot spot diagnostic mx bean found");
                }

                try {
                    hotSpotDiagnosticMXBean.setVMOption(optionName, optionValue); // Throws IllegalArgumentException if the option is not writeable in runtime
                } catch (final IllegalArgumentException illegalArgumentException) {
                    throw new IllegalArgumentException("can't set " + optionName + " to " + optionValue + " as it is read-only");
                }
            } catch (final IOException e) {
                DarkAddons.modError(e);
            }
        });
    }

    private static long gameLoopStart;
    private static long gameLoopEnd;

    private static long lastGameLoopTime;
    private static long lastGameLoopTimeNs;

    private static long lagThreshold;

    static final void enableLagTracking(final long threshold) {
        Diagnostics.lagThreshold = threshold;
    }

    static final void disableLagTracking() {
        Diagnostics.lagThreshold = 0L;
    }

    static final boolean isLagTracking() {
        return 0L != Diagnostics.lagThreshold;
    }

    static final void handleGameLoopPre() {
        Diagnostics.gameLoopStart = System.nanoTime();
    }

    private static final void watchdogLoop() {
        final var currentThread = Thread.currentThread();
        while (!currentThread.isInterrupted()) {
            try {
                // noinspection BusyWait
                Thread.sleep(Math.max(1_000L, Diagnostics.lagThreshold - 1L));
            } catch (final InterruptedException ie) {
                DarkAddons.handleInterruptedException(ie);
            }

            final var lastGameLoop = Diagnostics.gameLoopEnd;
            final var currentTime = System.currentTimeMillis();

            if (0L != lastGameLoop && Diagnostics.isLagTracking() && currentTime >= lastGameLoop + Diagnostics.lagThreshold) {
                final var freezeTime = currentTime - lastGameLoop;

                Diagnostics.LOGGER.warn("Client thread froze for {}ms. Dumping thread...", freezeTime);
                Diagnostics.dumpThread0(ManagementFactory.getThreadMXBean().getThreadInfo(Diagnostics.MC_THREAD_ID, Integer.MAX_VALUE), Diagnostics.LOGGER::warn);
            }
        }
    }

    static final void handleGameLoopPost() {
        Diagnostics.gameLoopEnd = System.nanoTime();

        // Maths.abs technically not necessary anymore after the switch from System#currentTimeMillis into System#nanoTime,
        // but leave it as a hardening measure or in case we swap back to System#currentTimeMillis again.

        // The reason it was necessary with System#currentTimeMillis is that it uses system time which can return a result that is smaller than the value from the previous calls because of system time changes (NTP sync, daylight saving adjustments, manual change by user or timezone change, etc.)
        Diagnostics.lastGameLoopTimeNs = Math.abs(Diagnostics.gameLoopEnd - Diagnostics.gameLoopStart);

        Diagnostics.lastGameLoopTime = TimeUnit.NANOSECONDS.toMillis(Diagnostics.lastGameLoopTimeNs);

        if (Diagnostics.isLagTracking() && Diagnostics.lagThreshold <= Diagnostics.lastGameLoopTime) {
            DarkAddons.queueWarning("Game loop took " + Diagnostics.getLastGameLoopTimeString() + " (" + 1_000L / Diagnostics.getLastGameLoopTime() + " fps) [" + MixinUtils.getLastTicksRan() + " ticks ran, taking " + Diagnostics.getLastFrameTickTimeString() + ']');
        }
    }

    static final long getLastGameLoopTime() {
        return Diagnostics.lastGameLoopTime;
    }

    static final long getLastGameLoopTimeNs() {
        return Diagnostics.lastGameLoopTimeNs;
    }

    @NotNull
    private static final String getLastGameLoopTimeString() {
        return Diagnostics.formatMsTimeWithColor(Diagnostics.getLastGameLoopTime());
    }

    private static final String getLastFrameTickTimeString() {
        return Diagnostics.formatMsTimeWithColor(Math.abs(MixinUtils.getElapsedTicksEnd() - MixinUtils.getElapsedTicksStart()));
    }

    private static final String formatMsTimeWithColor(final long time) {
        var color = 50L < time ? 'e' : 'a';

        if (75L < time) {
            color = '6';
        }

        if (100L < time) {
            color = 'c';
        }

        if (200L < time) {
            color = '4';
        }

        //noinspection StringConcatenationMissingWhitespace
        return "§" + color + time + "ms";
    }

    static final void dumpLastGameLoopTime(@SuppressWarnings("BoundedWildcard") @NotNull final Consumer<String> outputConsumer) {
        outputConsumer.accept("Last Game Loop Time: " + Diagnostics.getLastGameLoopTimeString());
    }

    @NotNull
    private static final FastDateFormat DATE_FORMAT = FastDateFormat.getInstance("dd.MM.yyyy HH:mm:ss", Locale.ROOT);

    @Nullable
    private static NotificationListener notificationListener;

    static final void registerGCTracker(final boolean detailed) {
        Diagnostics.unregisterGCTracker();
        Diagnostics.notificationListener = (@NotNull final Notification notification, @NotNull final Object handback) -> {
            final var gcni = GarbageCollectionNotificationInfo.from((CompositeData) notification.getUserData());
            final var info = gcni.getGcInfo();
            //noinspection StringConcatenationMissingWhitespace
            DarkAddons.queueWarning(detailed ? '[' + Diagnostics.DATE_FORMAT.format(new Date()) + "] " + gcni.getGcName() + " lasted for " + info.getDuration() + "ms. GC cause: " + gcni.getGcCause() + ", GC state: at " + gcni.getGcAction() : gcni.getGcName() + " lasted for " + info.getDuration() + "ms");
            if (detailed) {
                final var before = info.getMemoryUsageBeforeGc();
                //noinspection StreamToLoop
                info.getMemoryUsageAfterGc().forEach((@NotNull final String gcSection, @NotNull final MemoryUsage memoryUsage) -> {
                    final var beforeUsage = before.get(gcSection).getUsed();
                    final var afterUsage = memoryUsage.getUsed();

                    final var diff = afterUsage - beforeUsage;
                    if (0L != diff) {
                        final var indent = "    ";
                        DarkAddons.queueWarning(0L < diff ? indent + "Moved " + Utils.bytesToString(diff) + " into " + gcSection : indent + "Freed " + Utils.bytesToString(-diff) + " in " + gcSection);
                    }
                });
            }
        };

        final var garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        for (final var garbageCollectorMXBean : garbageCollectorMXBeans) {
            ((NotificationBroadcaster) garbageCollectorMXBean).addNotificationListener(Diagnostics.notificationListener, null, garbageCollectorMXBean);
        }
    }

    static final boolean isGCTrackerRegistered() {
        return null != Diagnostics.notificationListener;
    }

    static final void unregisterGCTracker() {
        if (Diagnostics.isGCTrackerRegistered()) {
            final var garbageCollectorMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
            for (final var garbageCollectorMXBean : garbageCollectorMXBeans) {
                try {
                    ((NotificationBroadcaster) garbageCollectorMXBean).removeNotificationListener(Diagnostics.notificationListener);
                } catch (final ListenerNotFoundException ignored) {
                    // ignored
                }
            }
            Diagnostics.notificationListener = null;
        }
    }

    static final class ThreadData {
        @NotNull
        private final String name;
        private final int priority;
        private final long mem;
        private final long cpu;
        private final boolean deadlocked;

        private ThreadData(@NotNull final String nameIn, final int priorityIn, final long memIn, final long cpuIn, final boolean deadlockedIn) {
            super();

            this.name = nameIn;
            this.priority = priorityIn;
            this.mem = memIn;
            this.cpu = cpuIn;
            this.deadlocked = deadlockedIn;
        }

        @NotNull
        final String getName() {
            return this.name;
        }

        final int getPriority() {
            return this.priority;
        }

        final long getMem() {
            return this.mem;
        }

        final long getCpu() {
            return this.cpu;
        }

        final boolean isDeadlocked() {
            return this.deadlocked;
        }

        @Override
        public final String toString() {
            return "ThreadData{" +
                "name='" + this.name + '\'' +
                ", priority=" + this.priority +
                ", mem=" + this.mem +
                ", cpu=" + this.cpu +
                ", deadlocked=" + this.deadlocked +
                '}';
        }
    }

    private static final boolean threadMXBeanIsWellSupported(@NotNull final ThreadMXBean threadMXBean) {
        return threadMXBean.isThreadAllocatedMemorySupported() && threadMXBean.isCurrentThreadCpuTimeSupported() && threadMXBean.isThreadCpuTimeSupported() && threadMXBean.isObjectMonitorUsageSupported() && threadMXBean.isSynchronizerUsageSupported();
    }

    private static final void threadMXBeanToggle(@NotNull final ThreadMXBean threadMXBean, final boolean toggle) {
        if (toggle) {
            if (!threadMXBean.isThreadCpuTimeEnabled()) {
                threadMXBean.setThreadCpuTimeEnabled(true);
            }
            if (!threadMXBean.isThreadAllocatedMemoryEnabled()) {
                threadMXBean.setThreadAllocatedMemoryEnabled(true);
            }
        } else {
            if (threadMXBean.isThreadCpuTimeEnabled()) {
                threadMXBean.setThreadCpuTimeEnabled(false);
            }
            if (threadMXBean.isThreadAllocatedMemoryEnabled()) {
                threadMXBean.setThreadAllocatedMemoryEnabled(false);
            }
        }
    }

    @NotNull
    private static final ArrayList<Long> getDeadlockedThreads(@NotNull final ThreadMXBean threadMXBean, final int sizeHint) {
        final var deadlockedThreads = new ArrayList<Long>(sizeHint);
        final var deadlockedThreadsArray = threadMXBean.findDeadlockedThreads();

        if (null != deadlockedThreadsArray) {
            for (final var id : deadlockedThreadsArray) {
                deadlockedThreads.add(id);
            }
        }

        return deadlockedThreads;
    }

    static final long getJVMUptime() {
        return ManagementFactory.getRuntimeMXBean().getUptime();
    }

    private static final void groupAsClientAndOther(@NotNull final ArrayList<Diagnostics.ThreadData> threadDatas) {
        final var grouped = new ArrayList<Diagnostics.ThreadData>(2);
        Diagnostics.ThreadData client = null;

        for (final var threadData : threadDatas) {
            if ("Client thread".equals(threadData.name)) {
                client = threadData;
                grouped.add(threadData);

                break;
            }
        }

        //noinspection IfCanBeAssertion
        if (null == client) {
            throw new IllegalStateException("could not find client thread in thread data list");
        }

        for (final var threadData : threadDatas) {
            if (client != threadData) {
                Diagnostics.ThreadData other = null;
                for (final var td : grouped) {
                    if (client != td) {
                        other = td;
                        break;
                    }
                }

                if (null == other) {
                    other = new Diagnostics.ThreadData("Other", threadData.priority, threadData.mem, threadData.cpu, threadData.deadlocked);
                } else {
                    grouped.remove(other);

                    other = new Diagnostics.ThreadData("Other", (other.priority > threadData.priority ? other : threadData).priority, other.mem + threadData.mem, other.cpu + threadData.cpu, other.deadlocked || threadData.deadlocked);
                }

                grouped.add(other);
            }
        }

        threadDatas.clear();
        threadDatas.addAll(grouped);
    }

    static final void generateThreadDatas(final boolean groupAsClientAndOther, @SuppressWarnings("BoundedWildcard") @NotNull final Consumer<ArrayList<Diagnostics.ThreadData>> callback) {
        Diagnostics.diagnosticsExecutor.execute(() -> {
            final var threads = Utils.getAllThreads();
            final var threadsLength = threads.length;

            final var list = new ArrayList<Diagnostics.ThreadData>(threads.length);

            final var threadMXBean = (ThreadMXBean) ManagementFactory.getThreadMXBean();

            // Make sure the JVM supports the necessary features needed to proceed
            final var supported = Diagnostics.threadMXBeanIsWellSupported(threadMXBean);

            //noinspection IfCanBeAssertion
            if (!supported) {
                throw new UnsupportedOperationException("This JVM doesn't support the necessary features needed to proceed.");
            }

            // Enable to proceed
            Diagnostics.threadMXBeanToggle(threadMXBean, true);

            // Collect data
            final var deadlockedThreads = Diagnostics.getDeadlockedThreads(threadMXBean, threadsLength);

            final var ids = new ArrayList<Long>(threadsLength);
            final var threadFromId = new HashMap<Long, Thread>(Utils.calculateHashMapCapacity(threadsLength));

            for (final var thread : threads) {
                if (null != thread) {
                    final var id = Utils.threadId(thread);

                    ids.add(id);
                    threadFromId.put(id, thread);
                }
            }

            final var idsArray = new long[ids.size()];
            Arrays.setAll(idsArray, ids::get);

            final var threadInfos = threadMXBean.getThreadInfo(idsArray);

            for (final var threadInfo : threadInfos) {
                if (null != threadInfo) {
                    final var id = threadInfo.getThreadId();
                    final var thread = threadFromId.get(id);
                    list.add(new Diagnostics.ThreadData(thread.getName(), thread.getPriority(), threadMXBean.getThreadAllocatedBytes(id), threadMXBean.getThreadCpuTime(id), deadlockedThreads.contains(id)));
                }
            }

            // Disable after done
            Diagnostics.threadMXBeanToggle(threadMXBean, false);

            // Group results if requested
            if (groupAsClientAndOther) {
                Diagnostics.groupAsClientAndOther(list);
            }

            callback.accept(list);
        });
    }
}
