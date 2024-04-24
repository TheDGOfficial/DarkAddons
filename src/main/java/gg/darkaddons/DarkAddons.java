package gg.darkaddons;

import gg.darkaddons.annotations.bytecode.Bridge;
import gg.darkaddons.annotations.bytecode.Synthetic;
import gg.essential.elementa.ElementaVersion;
import gg.essential.universal.UChat;
import gg.skytils.skytilsmod.utils.SBInfo;
import gg.skytils.skytilsmod.utils.SkyblockIsland;
import gg.skytils.skytilsmod.features.impl.handlers.MayorInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLLoadCompleteEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * A high-performance mod for the {@link MinecraftForge} platform,
 * providing performance-enhancing, bug-fixing or quality-of-life
 * feature additions, mostly for Hypixel Skyblock.
 */

// TODO make it so contributor names are fetched from UUIDs in mcmod.info, so if they change their name, the updated name will be in the mcmod.info on the next version of the mod, instead of being completely static names that need updating manually if any of the contributors has changed their in-game name.
@Mod(
    modid = Reference.MOD_ID,
    name = "DarkAddons",
    version = Reference.VERSION,
    dependencies = "required-after:skytils",
    useMetadata = true,
    clientSideOnly = true,
    acceptedMinecraftVersions = "[1.8.9]",
    acceptableRemoteVersions = "*",
    acceptableSaveVersions = "*",
    certificateFingerprint = "", // TODO sign & seal jar and add fingerprint here
    modLanguage = "java",
    modLanguageAdapter = "",
    canBeDeactivated = true, // TODO handle disable event and disable features? (it doesn't automatically unregister commands/event listeners)
    guiFactory = "gg.darkaddons.ForgeConfigInterop",
    updateJSON = "",
    customProperties = {}
)
@SuppressWarnings({"ClassNamePrefixedWithPackageName", "DefaultAnnotationParam"})
public final class DarkAddons {
    /**
     * Holds the name of the mod.
     */
    @NotNull
    static final String MOD_NAME = DarkAddons.class.getSimpleName();
    /**
     * Holds the mod id of the mod.
     */
    @NotNull
    public static final String MOD_ID = Reference.MOD_ID;
    /**
     * Holds the version string of the mod.
     */
    @NotNull
    private static final String VERSION = Reference.VERSION;
    /**
     * Holds the compatibility version for the Elementa library.
     */
    @NotNull
    public static final ElementaVersion ELEMENTA_VERSION;
    /**
     * Holds the {@link Config} instance.
     */
    @NotNull
    private static final Config config = new Config();

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

    /**
     * Holds state about if we should register tasks.
     */
    private static boolean shouldRegisterTasks = true;
    /**
     * Holds the queued mod messages.
     */
    @NotNull
    private static final ConcurrentLinkedQueue<String> messageQueue = new ConcurrentLinkedQueue<>();
    /**
     * Holds the shutdown tasks, which are run before exiting the game cleanly, by hitting the "Quit Game" button in the main menu.
     * <p>
     * Any other way to close the game won't run those tasks, i.e., power (electricity) loss, BSOD/kernel panic, game crash,
     * or a process kill request, from command line or task manager.
     */
    @NotNull
    private static final ConcurrentLinkedQueue<Runnable> shutdownTasks = new ConcurrentLinkedQueue<>();
    /**
     * Represents the maximum light level.
     */
    public static final int MAXIMUM_LIGHT_LEVEL = 15;
    /**
     * Becomes true after the load complete event if SkyblockAddons is installed and loaded.
     */
    private static boolean usingSBA;
    /**
     * Holds the prefix, usually added to the start of messages sent by mod; however,
     * not all messages necessarily need to have it.
     */
    @NotNull
    private static final String MESSAGE_PREFIX = "DarkAddons > ";
    /**
     * Becomes true after the load complete event.
     */
    private static boolean initialized;

    public static final boolean isInitialized() {
        return DarkAddons.initialized;
    }

    static {
        ElementaVersion v;

        try {
            v = ElementaVersion.V5;
        } catch (final NoSuchFieldError ignored) {
            // Caused by old Essential version. Automatic updates are opt-in on the latest version, and most users are clueless about this, so warn them.
            DarkAddons.queueWarning("Old version of bundled libraries detected. Please type /essential and enable auto updates & restart your-game to fix this. If this still appears on the latest Essential version, please report this.");

            // Automatically use the latest supported version, which is probably either V3 or V2 since V1 is very ancient. Unless Essential messes up and removes V5 enum value after adding V6 (and we haven't updated to V6 yet above), this latest supported version can't be greater than the above catch block one we use normally. However, that one needs to be manually updated while this one dynamically finds the version.
            v = ElementaVersion.valueOf(Diagnostics.getLatestSupportedRuntimeElementaVersion());
        }

        ELEMENTA_VERSION = v;
    }

    /**
     * Constructor used by Forge, do not use it manually.
     */
    @SuppressWarnings({"WeakerAccess", "PublicConstructor"})
    public DarkAddons() {
        super();
    }

    private static final void flushMessageQueue() {
        DarkAddons.messageQueue.removeIf((@NotNull final String message) -> {
            final var canSend = DarkAddons.canSendMessage();

            if (canSend) {
                DarkAddons.sendMessage(message);
            }

            return canSend;
        });
    }

    private static final boolean canSendMessage() {
        final Minecraft mc;
        //noinspection NestedAssignment
        return null != (mc = Minecraft.getMinecraft()) && null != mc.thePlayer && mc.isCallingFromMinecraftThread();
    }

    private static final void queueMessage(@NotNull final String message) {
        if (DarkAddons.shouldRegisterTasks) {
            DarkAddons.shouldRegisterTasks = false;
            DarkAddons.registerTickTask("flush_message_queue", 20, true, DarkAddons::flushMessageQueue);
        }
        //noinspection StringConcatenationMissingWhitespace
        final var msg = "§3" + DarkAddons.MESSAGE_PREFIX + message;
        DarkAddons.sendOrQueue(msg);
    }

    public static final void addShutdownTask(@NotNull final Runnable task) {
        DarkAddons.shutdownTasks.add(task);
    }

    public static final void runShutdownTasks() {
        DarkAddons.shutdownTasks.removeIf((@NotNull final Runnable task) -> {
            task.run();

            return true;
        });
    }

    public static final boolean isDisablePeriodicConfigSaves() {
        return Config.isDisablePeriodicConfigSaves();
    }

    private static final void sendOrQueue(@NotNull final String msg) {
        if (DarkAddons.canSendMessage()) {
            DarkAddons.sendMessage(msg);
        } else {
            DarkAddons.messageQueue.add(msg);
        }
    }

    static final void echoEmpty() {
        DarkAddons.sendOrQueue("");
    }

    private static final void queueInfo(@NotNull final String description) {
        DarkAddons.queueMessage("§7" + description);
    }

    static final void queueWarning(@NotNull final String description) {
        DarkAddons.queueMessage("§e" + description);
    }

    private static final void queueError(@NotNull final String description) {
        DarkAddons.queueMessage("§c" + description);
    }

    static final void sendMessage(@NotNull final String message) {
        UChat.chat(message);
    }

    private static final void sendMessageWithPrefix(@NotNull final String message, final boolean error) {
        if (error) {
            DarkAddons.queueError(message);
        } else {
            DarkAddons.queueMessage(message);
        }
    }

    private static final void logMessageWithPrefix(@NotNull final String message, final boolean error) {
        //final Minecraft mc;
        //if (null == (mc = Minecraft.getMinecraft()) || null == mc.thePlayer) { // only log it if we cant send it as a chat message
        final var msg = "DarkAddons: " + Utils.removeControlCodes(message);
        if (error) {
            DarkAddons.LoggerHolder.LOGGER.error(msg);
        } else {
            DarkAddons.LoggerHolder.LOGGER.info(msg);
        }
        //}
    }

    /**
     * Even though performance impact is minimal because we don't create a new ProfileSection everytime,
     * the compiler still generates try finally constructs on the generated bytecode, and try-finally isn't entirely
     * free of cost in terms of performance, even though minimal.
     * <p>
     * Therefore, this method can be used to check if startProfiling has any impact before calling it
     * and entering the try-with-resources scope.
     * @return True if Minecraft debug screen profiler is enabled, false otherwise.
     */
    public static final boolean shouldProfile() {
        return McProfilerHelper.shouldProfile();
    }

    static final void handleEvent(@NotNull final String handlerContext, @NotNull final Runnable eventHandler) {
        DarkAddons.handleEvent(handlerContext, null, Utils.runnableToConsumer(eventHandler));
    }

    static final <T extends Event> void handleEvent(@NotNull final String handlerContext, @Nullable final T event, @NotNull final Consumer<T> eventHandler) {
        //if (DarkAddons.shouldProfile()) {
        try (final var profileSection = DarkAddons.startEventProfiling(event, eventHandler)) {
            profileSection.start(handlerContext);
        }
        //} else {
        //    DarkAddons.runEventHandler(event, eventHandler);
        //}
    }

    @NotNull
    private static final <T extends Event> ProfileSection startEventProfiling(@Nullable final T event, @NotNull final Consumer<T> eventHandler) {
        McProfilerHelper.startSection("dark_addons_eventhandler");
        ProfileSection.markDirty();

        final var profileSection = ProfileSection.getInstance();
        DarkAddons.runEventHandler(event, eventHandler);

        return profileSection;
    }

    private static final <T extends Event> void runEventHandler(@Nullable final T event, @NotNull final Consumer<T> eventHandler) {
        if (Config.isUnsafeMode()) {
            eventHandler.accept(event);
        } else {
            try {
                eventHandler.accept(event);
            } catch (final Throwable t) {
                DarkAddons.modError(t);
            }
        }
    }

    /*static final void mixinError(@NotNull final Class<?> mixinClass) {
        if (!mixinClass.isInterface() && !Modifier.isAbstract(mixinClass.getModifiers())) {
            DarkAddons.notifyError("A mixin failure occurred and the given source is not an interface or abstract class. Source class: " + mixinClass.getName());
        }

        DarkAddons.notifyError("Mixin error: Mixin " + mixinClass.getName() + " likely failed to apply");
    }*/

    @NotNull
    private static final Throwable getRootError(@NotNull Throwable error, final Predicate<Throwable> predicate) {
        Throwable parent;
        //noinspection NestedAssignment,MethodCallInLoopCondition
        while (null != (parent = error.getCause())) {
            // We don't want the exceptions with no stack trace. Additionally, test for the predicate requested.
            if (0 == parent.getStackTrace().length || !predicate.test(parent)) {
                return error;
            }

            //noinspection AssignmentToMethodParameter
            error = parent;
        }

        return error;
    }

    static final void modError(@NotNull Throwable error) {
        DarkAddons.LoggerHolder.LOGGER.catching(error);

        final Predicate<String> isOurModule = (@NotNull final String clsName) -> clsName.contains("darkaddons");

        error = DarkAddons.getRootError(error, (@NotNull final Throwable err) -> {
            for (final var ste : err.getStackTrace()) {
                if (!isOurModule.test(ste.getClassName())) {
                    return false;
                }
            }
            return true;
        });

        final var stack = error.getStackTrace();

        var className = "?";
        var methodName = "?";

        var fileName = "SourceFile.java";
        var lineNumber = 0;

        var i = 1;

        final var stackLength = stack.length;

        while (i <= stackLength) {
            final var stackTraceElement = stack[i - 1];

            fileName = stackTraceElement.getFileName();
            lineNumber = stackTraceElement.getLineNumber();

            className = stackTraceElement.getClassName();
            methodName = stackTraceElement.getMethodName();

            if (isOurModule.test(className) && 1 != lineNumber && !className.contains("$$Lambda$")) { // line number 1 is usually used for synthetic accessor methods, $$Lambda$ stack trace elements will have "Unknown Source", so skip those
                break;
            }

            ++i;
        }

        if (!isOurModule.test(className) && 1 <= stack.length) {
            final var topOfStack = stack[0];

            fileName = topOfStack.getFileName();
            lineNumber = topOfStack.getLineNumber();

            //className = topOfStack.getClassName();
            methodName = topOfStack.getMethodName();
        }

        var details = "";
        final String errorMessage;
        //noinspection NestedAssignment
        if (null != (errorMessage = error.getMessage())) {
            details = " Details: " + errorMessage;
        }

        DarkAddons.sendMessageWithPrefix("Encountered " + error.getClass().getSimpleName() + " at " + fileName + " in method " + methodName + " in line " + lineNumber + '.' + details, true);
    }

    static final void handleInterruptedException(@NotNull final InterruptedException ie) {
        DarkAddons.modError(ie);

        Thread.currentThread().interrupt(); // Catching the exception is forced, but catching it stops the interruption process. This will interrupt it again.
    }

    static final void debug(@NotNull final Supplier<String> messageSupplier) {
        if (Config.isDebugMode()) {
            final var message = messageSupplier.get();

            DarkAddons.logMessageWithPrefix(message, false);
            //DarkAddons.sendMessageWithPrefix(message, false);
        }
    }

    static final void runOnceInNextTick(@NotNull final String taskName, @NotNull final Runnable task) {
        DarkAddons.registerTickTask(taskName, 1, false, task);
    }

    static final void registerTickTask(@NotNull final String taskName, final int ticks, final boolean repeat, @NotNull final Runnable task) {
        final var tickTask = new TickTask(taskName, ticks, repeat, () -> {
            if (Config.isUnsafeMode()) {
                task.run();
            } else {
                try {
                    task.run();
                } catch (final Throwable error) {
                    DarkAddons.modError(error);
                }
            }
        });

        tickTask.register();
    }

    static final void openGui(@NotNull final String guiName, @NotNull final GuiScreen gui) {
        DarkAddons.runOnceInNextTick("open_gui_" + guiName, () -> Minecraft.getMinecraft().displayGuiScreen(gui));
    }

    static final void openConfigEditor() {
        DarkAddons.openGui("config", DarkAddons.getConfigEditor());
    }

    private static final void preloadConfig() {
        DarkAddons.config.preload();
        DarkAddons.getConfigEditor();
    }

    @NotNull
    private static final GuiScreen getConfigEditor() {
        return DarkAddons.config.gui();
    }

    static final boolean isInLocationEditingGui() {
        return OptionsScreen.isLocationEditingGui(Minecraft.getMinecraft().currentScreen);
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final void saveGuiPositions(final boolean force) {
        GuiManager.save(force);
    }

    // TODO remove/change implementation of these after split
    static final boolean isInSkyblock() {
        return gg.skytils.skytilsmod.utils.Utils.INSTANCE.getInSkyblock();
    }

    static final boolean isInDungeons() {
        return gg.skytils.skytilsmod.utils.Utils.INSTANCE.getInDungeons();
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final boolean isPlayerInCrystalHollows() {
        return SkyblockIsland.CrystalHollows.getMode().equals(SBInfo.INSTANCE.getMode());
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final boolean isPlayerInDungeonHub() {
        return SkyblockIsland.DungeonHub.getMode().equals(SBInfo.INSTANCE.getMode());
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final boolean isPlayerInGarden() {
        return SkyblockIsland.TheGarden.getMode().equals(SBInfo.INSTANCE.getMode());
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final boolean isPlayerInRift() {
        return SkyblockIsland.TheRift.getMode().equals(SBInfo.INSTANCE.getMode());
    }

    @SuppressWarnings("StaticMethodOnlyUsedInOneClass")
    static final boolean isPlayerInMineshaft() {
        return "mineshaft".equals(SBInfo.INSTANCE.getMode());
    }

    static final boolean isDerpy() {
        return "Derpy".equals(MayorInfo.INSTANCE.getCurrentMayor());
    }

    /**
     * Frees memory (unnecessary objects).
     * <p>
     * It only frees {@link Minecraft#memoryReserve} for now,
     * replacing the 10MB byte with an empty byte array.
     * <p>
     * Making it null instead of the empty array seems safe,
     * although it doesn't save any more memory as an empty
     * array has zero to tiny memory cost with the JIT.
     * <p>
     * This method may free other memory in the future, such
     * as some caches; thus it is not intended to be called
     * under normal operation, only during startup to reset stuff
     * and under very high memory load, perhaps.
     */
    private static final void freeMemory() {
        MemoryLeakFix.freeUnnecessary();
    }

    @Synthetic
    @Bridge
    @NotNull
    private static final String getIntelDriverCmdline() {
        final var x = 'x';
        final var e = 'e';

        final var xe = e + Character.toString(x);
        final var xee = xe + e;

        return "MojangTricksIntelDriversForPerformance_javaw." + xee + "_minecraft." + xee;
    }

    static final void tweakFlags() {
        // Dependency for other args
        //Diagnostics.modifyVMOption("UnlockExperimentalVMOptions", "true");
        //Diagnostics.modifyVMOption("UnlockDiagnosticVMOptions", "true");

        // Backport of better defaults from newer Java versions
        //Diagnostics.modifyVMOption("MaxInlineLevel", "15");
        //Diagnostics.modifyVMOption("InlineSmallCode", "2500");
        //Diagnostics.modifyVMOption("BiasedLockingStartupDelay", "0");
        //Diagnostics.modifyVMOption("ParallelRefProcEnabled", "true");

        // Compile all methods to native code eventually, even those that are huge, since Minecraft is a long-running application
        //Diagnostics.modifyVMOption("DontCompileHugeMethods", "false");

        // Reduces Disk I/O
        //Diagnostics.modifyVMOption("PerfDisableSharedMem", "true");
        //Diagnostics.modifyVMOption("UsePerfData", "false");
        //Diagnostics.modifyVMOption("DisableAttachMechanism", "true");
        DarkAddons.setProperty("sun.rmi.transport.tcp.maxConnectionThreads", 0);
        DarkAddons.setProperty("java.rmi.server.randomIDs", false);
        Diagnostics.modifyVMOption("HeapDumpOnOutOfMemoryError", Boolean.toString(false));

        // For the Log4J Vulnerability
        DarkAddons.setProperty("com.sun.jndi.ldap.object.trustURLCodebase", false);
        DarkAddons.setProperty("com.sun.jndi.ldap.object.trustSerialData", false);

        DarkAddons.setProperty("com.sun.jndi.rmi.object.trustURLCodebase", false);
        DarkAddons.setProperty("com.sun.jndi.rmi.object.trustSerialData", false);

        DarkAddons.setProperty("log4j2.formatMsgNoLookups", true);

        // Fixes "... has a security seal for path ... but that path is defined and not secure" errors in log being spammed
        DarkAddons.setProperty("sun.misc.URLClassPath.disableJarChecking", true);

        // Some good optimizations for constant fields
        //Diagnostics.modifyVMOption("FoldStableValues", "true");
        //Diagnostics.modifyVMOption("TrustFinalNonStaticFields", "true");

        // Old Minecraft Launcher had this so that the game opens with dedicated GPU
        DarkAddons.setProperty("intel.driver.cmdline", DarkAddons.getIntelDriverCmdline());

        // NVIDIA drivers look for the main class string in the process commandline to enable Minecraft specific optimizations
        DarkAddons.setProperty("nvidia.driver.cmdline", "net.minecraft.client.main.Main ");

        // Somehow IntelliJ args has this, so maybe Kotlin coroutines have debug enabled by default?
        DarkAddons.setProperty("kotlinx.coroutines.debug", false);

        // Other performance args
        //Diagnostics.modifyVMOption("UseFastAccessorMethods", "true");
        //Diagnostics.modifyVMOption("RelaxAccessControlCheck", "true");
        //Diagnostics.modifyVMOption("MonitorInUseLists", "false");
        //Diagnostics.modifyVMOption("JavaMonitorsInStackTrace", "false");

        // Unsafe, but will improve performance
        DarkAddons.setProperty("org.lwjgl.util.NoChecks", true);
        DarkAddons.setProperty("org.lwjgl.util.NoFunctionChecks", true);
        DarkAddons.setProperty("org.lwjgl.glfw.checkThread0", false);

        DarkAddons.setProperty("sun.awt.disablegrab", true);
    }

    private static final void setProperty(@NotNull final String propertyName, final boolean value) {
        DarkAddons.setProperty(propertyName, Boolean.toString(value));
    }

    private static final void setProperty(@NotNull final String propertyName, final int value) {
        DarkAddons.setProperty(propertyName, Integer.toString(value));
    }

    private static final void setProperty(@NotNull final String propertyName, @NotNull final String value) {
        System.setProperty(propertyName, value);
    }

    /**
     * Initializes the {@link Config}.
     * <p>
     * This will create the config if it doesn't exist yet,
     * or it will load it if it does.
     * <p>
     * Any value read from {@link Config} will return
     * their default values until this method is called.
     */
    static final void initConfig() {
        DarkAddons.config.init();
    }

    @SuppressWarnings("ArrayCreationWithoutNewKeyword")
    @NotNull
    private static final String[] WELCOME_MESSAGE = {
        "Welcome to " + DarkAddons.MOD_NAME + ", version " + DarkAddons.VERSION + "! Type " + DarkAddonsCommand.COMMAND_PREFIX + DarkAddonsCommand.MAIN_COMMAND_NAME + " to get started!",
        "",
        "If you wish to turn off this message, you can do so from the config, accessible by clicking the Config button after entering the command."
    };

    static final void queueWelcomeMessage() {
        for (final var line : DarkAddons.WELCOME_MESSAGE) {
            if (line.isEmpty()) {
                DarkAddons.echoEmpty();
            } else {
                DarkAddons.queueInfo(line);
            }
        }
    }

    static final void dequeueWelcomeMessage() {
        //noinspection ForLoopWithMissingComponent
        for (final var it = DarkAddons.messageQueue.iterator(); it.hasNext();) {
            final var line = it.next();

            for (final var welcomeMessageLine : DarkAddons.WELCOME_MESSAGE) {
                if (welcomeMessageLine.equals(StringUtils.remove(Utils.removeControlCodes(line), DarkAddons.MESSAGE_PREFIX))) {
                    it.remove();
                }
            }
        }
    }

    /**
     * Register all the {@link net.minecraft.command.CommandBase} extensions
     * of the mod, which will add new commands to the game.
     * <p>
     * Should be called only once and during start-up.
     */
    private static final void registerCommands() {
        ClientCommandHandler.instance.registerCommand(new DarkAddonsCommand());

        if (Config.isWelcomeMessage()) {
            DarkAddons.queueWelcomeMessage();
        }
    }

    private static final void registerSubCommands() {
        SubCommand.registerAll();
    }

    /**
     * Registers all {@link GuiElement}s and {@link Event}s.
     * <p>
     * This method should not be called multiple times
     * and only during the start-up.
     */
    private static final void registerAll() {
        MemoryLeakFix.registerPeriodicClean();
        RemoveBlankArmorStands.registerPeriodicRemoval();

        GhostBlock.registerKeybindings();
        RequeueKey.registerKeybindings();

        DarkAddons.register(
            TickTask.newManager(),
            new GuiManager(),
            new M7Features(),
            new AdditionalM7Features(),
            new M7DragonDisplay(),
            new BlessingDisplay(),
            new EHPDisplay(),
            new RogueSwordTimer(),
            new CenturyRaffleTicketTimer(),
            new ClassAverage50Display(),
            new SlayerRNGDisplay(),
            new RejoinCooldownDisplay(),
            new FPSLimitDisplay(),
            new RemoveBlankArmorStands(),
            new HideXPOrbs(),
            new TablistUtil(),
            new UnopenedChestsDisplay(),
            new FPSDisplay(),
            new GhostBlock(),
            new ScoreFromScoreboard(),
            new RequeueKey(),
            new AutoExtraStats()
        );

        GuiManager.init();

        GuiManager.load();
        DarkAddons.saveGuiPositions(true); // Preloads GsonBuilder classes to prevent a freeze when first-time saving
    }

    private static final void register(@NotNull final Object... all) {
        for (final var object : all) {
            if (object instanceof GuiElement) {
                GuiManager.registerElement((GuiElement) object);
            }
            MinecraftForge.EVENT_BUS.register(object);
        }
    }

    // TODO remove all those bridge access widening methods after split

    /**
     * Called when a mob is spawned.
     *
     * @param entity The mob spawned as an entity.
     */
    /*public static final void onMobSpawned(final Entity entity) {
        M7Features.onMobSpawned(entity);
    }*/

    /**
     * Checks if dragon death animations should be hidden.
     *
     * @return True if dragon death animations should be hidden, false otherwise.
     */
    /*public static final boolean shouldHideDragonDeath() {
        return M7Features.shouldHideDragonDeath();
    }*/

    /**
     * Gets hurt opacity for a dragon.
     *
     * @param renderDragon The dragon renderer that is rendering the dragon.
     * @param lastDrag The dragon to get hurt opacity of.
     * @param value Current hurt opacity.
     * @return The new hurt opacity, if applicable.
     */
    /*public static final float getHurtOpacity(final RenderDragon renderDragon, final EntityDragon lastDrag, final float value) {
        return M7Features.getHurtOpacity(renderDragon, lastDrag, value);
    }*/

    /**
     * Runs after a dragon hurt effect is rendered.
     *
     * @param renderDragon The dragon renderer that is rendering the dragon.
     */
    /*public static final void afterRenderHurtFrame(final RenderDragon renderDragon) {
        M7Features.afterRenderHurtFrame(renderDragon);
    }*/

    /**
     * Gets the texture of a dragon entity.
     * This method returns void because it sets the value in {@link CallbackInfoReturnable}.
     *
     * @param entity The {@link EntityDragon} to get texture of.
     * @param cir The {@link CallbackInfoReturnable} to hold the return value.
     */
    /*public static final void getEntityTexture(final EntityDragon entity, final CallbackInfoReturnable<? super ResourceLocation> cir) {
        M7Features.getEntityTexture(entity, cir);
    }*/
    public static final long getLastGameLoopTime() {
        return Diagnostics.getLastGameLoopTime();
    }

    /**
     * Returns true if delay chunk updates are enabled in the Config, false otherwise.
     *
     * @return True if delay chunk updates are enabled in the Config, false otherwise.
     */
    public static final boolean isDelayChunkUpdates() {
        return Config.isDelayChunkUpdates();
    }

    /**
     * Returns true if always sprint is enabled in the Config, false otherwise.
     *
     * @return True if always sprint is enabled in the Config, false otherwise.
     */
    public static final boolean isAlwaysSprint() {
        return Config.isAlwaysSprint();
    }

    /**
     * Returns true if full bright is enabled in the Config, false otherwise.
     *
     * @return True if full bright is enabled in the Config, false otherwise.
     */
    public static final boolean isFullBright() {
        return Config.isFullBright();
    }

    /**
     * Returns true if disable yield is enabled in the Config, false otherwise.
     *
     * @return True if disable yield is enabled in the Config, false otherwise.
     */
    public static final boolean isDisableYield() {
        return Config.isDisableYield();
    }

    /**
     * Returns true if reduce background threads feauture is enabled in the Config, false otherwise.
     *
     * @return True if reduce background threads feature is enabled in the Config, false otherwise.
     */
    public static final boolean isReduceBackgroundThreads() {
        return Config.isReduceBackgroundThreads();
    }

    /**
     * Gets the main menu frame limit set in Config.
     *
     * @return The main menu frame limit from Config.
     */
    public static final int getMainMenuFrameLimit() {
        return Config.getMainMenuFrameLimit();
    }

    /**
     * Returns true if left-click auto clicker is enabled in Config, false otherwise.
     *
     * @return True if left-click auto clicker is enabled in Config.
     */
    public static final boolean isLeftClickAutoClicker() {
        return Config.isLeftClickAutoClicker();
    }

    /**
     * Returns true if right-click auto clicker is enabled in Config, false otherwise.
     *
     * @return True if right-click auto clicker is enabled in Config.
     */
    public static final boolean isRightClickAutoClicker() {
        return Config.isRightClickAutoClicker();
    }

    /**
     * Returns true if smoothen frames is true, false otherwise.
     *
     * @return True if smoothen frames are enabled in Config.
     */
    public static final boolean isSmoothenFrames() {
        return Config.isSmoothenFrames();
    }

    static final boolean isUsingSBA() {
        return DarkAddons.usingSBA;
    }

    /**
     * Called when a packet is received on the client.
     *
     * @param packet The packet.
     */
    public static final boolean onClientPacketReceive(@NotNull final Packet<?> packet) {
        if (!HideFallingBlocks.handlePacket(packet) || !HideParticles.handlePacket(packet) || !AutoCloseChests.handlePacket(packet)) {
            return false;
        }

        M7Features.handlePacket(packet);
        ChromaScoreboard.handlePacket(packet);

        return true;
    }

    /**
     * Checks if an entity can be rendered.
     *
     * @param entity The entity.
     * @param cir The {@link CallbackInfoReturnable} to set the return value, since this method returns void.
     */
    public static final void checkRender(@NotNull final Entity entity, @NotNull final CallbackInfoReturnable<Boolean> cir) {
        CheckRender.checkRender(entity, cir);
    }

    /**
     * Hook from MixinPlayerControllerMP that overrides vanilla behavior if required.
     *
     * @param blockPos The block position.
     * @param currentItemHittingBlock The current item that is hitting the block.
     * @param currentBlock The current block's position.
     * @return True or false
     */
    public static final boolean isHittingPosition(@NotNull final BlockPos blockPos, @Nullable final ItemStack currentItemHittingBlock, @NotNull final BlockPos currentBlock) {
        return MiningPingFix.isHittingPosition(blockPos, currentItemHittingBlock, currentBlock);
    }

    /**
     * Handles game loop start.
     */
    public static final void handleGameLoopPre() {
        Diagnostics.handleGameLoopPre();
    }

    /**
     * Handles game loop end.
     */
    public static final void handleGameLoopPost() {
        Diagnostics.handleGameLoopPost();
    }

    /**
     * Handles tile entity render.
     *
     * @param tileEntity The tile entity that is going to be rendered.
     */
    public static final void handleRenderTileEntity(@NotNull final TileEntity tileEntity) {
        HideSigns.handleRenderTileEntity(tileEntity);
    }

    /**
     * Handles render living post.
     *
     * @param entity The entity that just got rendered.
     */
    public static final void handleRenderLivingPost(@NotNull final EntityLivingBase entity) {
        M7Features.handleRender(entity);
    }

    /**
     * Handles client tick event.
     *
     * @param event The client tick event.
     */
    public static final void handleClientTick(@NotNull final TickEvent.ClientTickEvent event) {
        AutoMelody.handleTick(event);
        if (TickEvent.Phase.START == event.phase) {
            AutoClassAbilities.tick();
        }
    }

    /**
     * Handles client chat received event.
     *
     * @param event The client chat received event.
     */
    public static final void handleClientChatReceived(@NotNull final ClientChatReceivedEvent event) {
        AdditionalM7Features.handleMessage(event);
        EHPDisplay.doCheckMessage(event);

        RogueSwordTimer.doCheckMessage(event);
        CenturyRaffleTicketTimer.doCheckMessage(event);

        ClassAverage50Display.doCheckMessage(event);
        SlayerRNGDisplay.onReceiveChatMessage(event);

        RejoinCooldownDisplay.onMessage(event);

        MineshaftNotifier.doCheckMessage(event);
    }

    /**
     * Handles title update.
     *
     * @param title A title that's going to be shown on screen.
     */
    public static final void handleSubTitleUpdate(@Nullable final String title) {
        AutoDance.handleSubTitleUpdate(title);
    }

    /**
     * Handles render world last event.
     *
     * @param event The {@link RenderWorldLastEvent}.
     */
    public static final void handleRenderWorldLast(@NotNull final RenderWorldLastEvent event) {
        M7Features.handleRenderWorld(event);
        ArmorStandOptimizer.renderWorld();
    }

    /**
     * Handles gui open event.
     *
     * @param event The {@link GuiOpenEvent}.
     */
    public static final void handleGuiOpen(@NotNull final GuiOpenEvent event) {
        HackingForDummiesSolver.onGuiOpen(event);
    }

    /**
     * Handles player tick event.
     *
     * @param event The {@link TickEvent.PlayerTickEvent}
     */
    public static final void handlePlayerTick(@NotNull final TickEvent.PlayerTickEvent event) {
        HackingForDummiesSolver.onPlayerTick(event);
    }

    /**
     * Handles render tick event.
     *
     * @param event The {@link TickEvent.RenderTickEvent}
     */
    public static final void handleRenderTick(@NotNull final TickEvent.RenderTickEvent event) {
        HackingForDummiesSolver.onRenderTick(event);
    }

    /**
     * Handles world load event.
     */
    public static final void handleWorldLoad() {
        AutoClassAbilities.worldLoad();
        UnopenedChestsDisplay.onWorldLoad();
    }

    /**
     * Called before an entity is being rendered.
     *
     * @param entity The entity.
     */
    public static final void doRenderEntityPre(@NotNull final Entity entity) {
        if (Config.isProfilerMode() && McProfilerHelper.shouldProfile()) {
            McProfilerHelper.startSection("render_entity_" + entity.getClass().getSimpleName());
        }
        RemoveArmorStands.onRenderEntityPre(entity);
    }

    /**
     * Called after an entity is rendered.
     */
    public static final void doRenderEntityPost() {
        if (Config.isProfilerMode() && McProfilerHelper.shouldProfile()) {
            McProfilerHelper.endSection();
        }
    }

    /**
     * Checks if the player is holding a sword.
     *
     * @param mc The Minecraft instance.
     * @return True if the player is holding a sword, false otherwise.
     */
    public static final boolean isHoldingASword(@NotNull final Minecraft mc) {
        return AutoClicker.isHoldingASword(mc);
    }

    /**
     * Checks if the player is holding a Terminator.
     *
     * @param mc The Minecraft instance.
     * @return True if the player is holding a Terminator, false otherwise.
     */
    public static final boolean isHoldingTerm(@NotNull final Minecraft mc) {
        return AutoClicker.isHoldingTerm(mc);
    }

    /**
     * Resets AutoClicker state.
     * <p>
     * Called at the start of each tick.
     */
    public static final void resetShouldClick() {
        AutoClicker.resetShouldClick();
    }

    /**
     * Checks if given {@link KeyBinding#isKeyDown()} call should succeed in the current tick.
     *
     * @param keyBinding The key binding to check if it is pressed.
     */
    public static final boolean isKeyDownHook(@NotNull final KeyBinding keyBinding) {
        return CancelItemUses.shouldAllowKeyPress(keyBinding);
    }

    /**
     * Checks if given {@link KeyBinding#isPressed()} call should succeed in the current tick.
     * With more than 20 CPS limit also calls the given left and right click functions manually.
     *
     * @param keyBinding The key binding to check if it is pressed.
     * @param leftClick The left click function to use in situations with more than 20 CPS limit.
     * @param rightClick The right click function to use in situations with more than 20 CPS limit.
     * @return True if given {@link KeyBinding#isPressed()} call should succeed in the current tick, false otherwise.
     */
    public static final boolean isPressedStatic(@NotNull final KeyBinding keyBinding, @NotNull final Runnable leftClick, @NotNull final Runnable rightClick) {
        return AutoClicker.isPressedStatic(keyBinding, leftClick, rightClick) && CancelItemUses.shouldAllowKeyPress(keyBinding);
    }

    /**
     * Gets the CPS limit for the specific AutoClicker.
     *
     * @param left Whether to get CPS limit of the left AutoClicker or right AutoClicker.
     * @return The CPS limit for the specific AutoClicker.
     */
    /*public static final int getCpsLimit(final boolean left) {
        return AutoClicker.getCpsLimit(left);
    }*/

    /**
     * Returns true if Catchup AutoClicker is enabled in config, false otherwise.
     *
     * @return True if Catchup AutoClicker is enabled in config, false otherwise.
     */
    /*public static final boolean isCatchupAutoClicker() {
        return Config.isCatchupAutoClicker();
    }*/

    /**
     * Emulates auto clicker tick.
     * <p>
     * This can be used to tick AutoClicker in full even in very low TPS conditions, to keep
     * up its CPS.
     *
     * @param left If the emulation is done for the left click auto clicker or not.
     */
    /*public static final void emulateACTick(final boolean left) {
        AutoClicker.emulateACTick(left);
    }*/

    /**
     * Returns true if Profiler Mode is enabled in config, false otherwise.
     *
     * @return True if Profiler Mode is enabled in config, false otherwise.
     */
    public static final boolean isProfilerMode() {
        return Config.isProfilerMode();
    }

    /**
     * Returns true if Optimize Exceptions is enabled in config, false otherwise.
     *
     * @return True if Optimize Exceptions is enabled in config, false otherwise.
     */
    public static final boolean isOptimizeExceptions() {
        return Config.isOptimizeExceptions();
    }

    /**
     * Gets Minecraft main client thread ID.
     *
     * @return The Minecraft main client thread ID.
     */
    public static final long getMcThreadId() {
        return Diagnostics.MC_THREAD_ID;
    }

    /**
     * Checks if the currently calling thread is the client main thread.
     *
     * @return True if the currently calling thread is NOT the client main thread.
     */
    static final boolean checkClientEvent() {
        final var mc = Minecraft.getMinecraft();
        final var isNotCallingFromClientThread = !mc.isCallingFromMinecraftThread();
        //noinspection IfCanBeAssertion
        if (isNotCallingFromClientThread && (!mc.isIntegratedServerRunning() || !mc.getIntegratedServer().isCallingFromMinecraftThread())) {
            // Throw for events that get called from outside the client and server threads
            throw new IllegalStateException("event called from outside client and server threads, from thread " + Thread.currentThread().getName());
        } // Internal server thread or the client thread, fall-thru to return false or true depending on which one it is.

        return isNotCallingFromClientThread;
    }

    private static final void checkForSBA() {
        DarkAddons.usingSBA = Loader.isModLoaded("skyblockaddons");
    }

    /*private static final void preloadMixinClasses() {
        DarkAddons.blackholeConsume(
            Chunk.class,
            EntityPlayerSP.class,
            EventBus.class,
            FMLHandshakeMessage.class,
            FMLHandshakeMessage.ModList.class,
            Minecraft.class,
            NetHandlerPlayClient.class,
            NetworkManager.class,
            NetworkPlayerInfo.class,
            RenderManager.class,
            ScorePlayerTeam.class,
            TileEntityRendererDispatcher.class,
            World.class,
            ChannelInitializer.class,
            DefaultChannelConfig.class,
            Timer.class
        );

        DarkAddons.blackholeConsumeNonConstant(false,
            NetworkManager.class.getName() + "$5",
            NetworkManager.class.getName() + "$6",
            OldServerPinger.class.getName() + "$2"
        );

        DarkAddons.blackholeConsumeNonConstant(true,
            "gg.essential.api.utils.Multithreading",
            "gg.essential.util.Multithreading",
            "gg.skytils.skytilsmod.Skytils"
        );
    }

    @SuppressWarnings("EmptyMethod")
    private static final void blackholeConsume(@NotNull final Class<?>... ignoredClasses) {
        // do nothing
    }

    private static final void blackholeConsumeNonConstant(final boolean ignoreIfNotFound, @NotNull final String... fullyQualifiedClassNames) {
        for (final var clazz : fullyQualifiedClassNames) {
            try {
                DarkAddons.blackholeConsume(Class.forName(clazz));
            } catch (final ClassNotFoundException e) {
                if (!ignoreIfNotFound) {
                    DarkAddons.modError(e);
                }
            }
        }
    }*/

    private static final void reducePatcherBackgroundThreads() {
        if (DarkAddons.isReduceBackgroundThreads()) {
            try {
                final var threadPoolExecutor = (ThreadPoolExecutor) Class.forName("club.sk1er.patcher.util.enhancement.Enhancement").getField("POOL").get(null);
                final var cpuTotalThreadsAmount = Runtime.getRuntime().availableProcessors();

                threadPoolExecutor.setCorePoolSize(Math.min(50, cpuTotalThreadsAmount));
                threadPoolExecutor.setMaximumPoolSize(Math.min(50, cpuTotalThreadsAmount << 1));
            } catch (final ClassNotFoundException e) {
                // Do nothing, patcher not installed, no threads to reduce
            } catch (final IllegalAccessException | NoSuchFieldException e) {
                // Shouldn't happen
                DarkAddons.modError(e);
            }
        }
    }

    /*private static final void runPreDisplayHooks() {
        if (Config.isModifyWindowTitle()) {
            Display.setTitle(Display.getTitle() + " | DarkAddons " + DarkAddons.VERSION);
        }
    }*/

    /*private static final void checkElementaVersion() {
        final ElementaVersion maxSupported = ElementaVersion.valueOf(Diagnostics.getLatestSupportedRuntimeElementaVersion());

        if (DarkAddons.ELEMENTA_VERSION != maxSupported) {
            DarkAddons.queueWarning("Elementa Version " + maxSupported.name() + " is available, but DarkAddons is still on " + DarkAddons.ELEMENTA_VERSION + ". This shouldn't cause any issues and is just a reminder for the developers. Ignore this if you are an end user of the mod.");
        }
    }*/

    /**
     * Runs when the mod is being initialized.
     *
     * @param ignoredEvent The initialization event.
     */
    @Mod.EventHandler
    public final void init(@NotNull final FMLInitializationEvent ignoredEvent) {
        ThrowingRunnable.of(() -> {
            DarkAddons.initConfig();
            DarkAddons.freeMemory();
            if (Config.isTweakJavaFlags()) {
                DarkAddons.tweakFlags();
            }
            //DarkAddons.runPreDisplayHooks();
            DarkAddons.registerAll();
        }).runHandling(DarkAddons::modError);
    }

    /**
     * Runs when loading is complete.
     *
     * @param ignoredEvent Load complete event.
     */
    @Mod.EventHandler
    public final void loadComplete(@NotNull final FMLLoadCompleteEvent ignoredEvent) {
        ThrowingRunnable.of(() -> {
            DarkAddons.registerCommands();
            DarkAddons.registerSubCommands();

            DarkAddons.LoggerHolder.LOGGER.info("Preloading config, this might take some time...");
            DarkAddons.preloadConfig(); // Preloads config & vigilant class and gui screen, otherwise it causes multiple second delay before first opening
            DarkAddons.LoggerHolder.LOGGER.info("Done preloading config.");

            ThreadPriorityTweaker.init();

            DarkAddons.checkForSBA();
            /*if (Config.isDebugMode() && !Config.isUnsafeMode()) {
                DarkAddons.preloadMixinClasses();
            }*/

            DarkAddons.reducePatcherBackgroundThreads();
            //DarkAddons.checkElementaVersion();

            RunsTillCA50.init();

            if (Config.isClassAverage50Display()) {
                ClassAverage50Display.syncClassXP();
            }

            if (Config.isUpdateChecker()) {
                UpdateChecker.checkInBackground(Function.identity()::apply);
            }
        }).runHandling(DarkAddons::modError);

        DarkAddons.initialized = true;
    }
}
