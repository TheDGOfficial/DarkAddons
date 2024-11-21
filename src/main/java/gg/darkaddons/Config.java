package gg.darkaddons;

import gg.essential.vigilance.Vigilant;
import gg.essential.vigilance.data.Category;
import gg.essential.vigilance.data.JVMAnnotationPropertyCollector;
import gg.essential.vigilance.data.Property;
import gg.essential.vigilance.data.PropertyData;
import gg.essential.vigilance.data.PropertyType;
import gg.essential.vigilance.data.SortingBehavior;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// TODO OneConfig support (not migration, should just redirect to vigilance config)
// TODO if a config option is renamed or removed, the option is still kept in the config file and needs manual cleanup.
@SuppressWarnings({"CanBeFinal", "ClassWithTooManyMethods", "ClassWithTooManyFields"})
final class Config extends Vigilant {
    @NotNull
    private static final String BLESSING_HUD = "blessingHud";
    @NotNull
    private static final String SHOW_STATUE_BOX = "showStatueBox";

    @Property(
        type = PropertyType.SWITCH, name = "Welcome Message",
        description = "Sends a message when you first join, telling you the main command of the mod and how to get started.",
        category = "Misc", subcategory = "General", triggerActionOnInitialization = false
    )
    private static boolean welcomeMessage = true;

    @Property(
        type = PropertyType.SWITCH, name = "Update Checker",
        description = "Checks for mod updates automatically on game start-up and notifies you if there is a newer version available. This does not automatically update or install anything on your system. The update checking process is done on background with low priority so it will not slow down game start-up. If disabled, you can still manually check for updates by typing /darkaddons checkupdates in-game.",
        category = "Misc", subcategory = "General", triggerActionOnInitialization = false
    )
    private static boolean updateChecker = true;

    @Property(
        type = PropertyType.SWITCH, name = "Class Average 50 Display",
        description = "Shows a HUD element with runs needed to get Class Average 50, along with other information. Updated live as you do runs.",
        category = "Dungeons", subcategory = "HUD", triggerActionOnInitialization = false
    )
    private static boolean classAverage50Display;

    @Property(
        type = PropertyType.SELECTOR, name = "Class Average 50 Display Floor",
        description = "Select the floor used for the runs needed calculations; usually and by default M7, or you can change it to M6 if you are insane and plan to get Class Average 50 from M6 instead. There is also an adaptive mode where it automatically changes to the one you did last, or defaulting to M7 if unknown. The last done floor is stored on the RAM but saved to disk when you close the game via Quit Game from Main Menu and so will persist accross restarts if possible.",
        category = "Dungeons", subcategory = "HUD", triggerActionOnInitialization = false,
        options = {"M7", "M6", "Adaptive"}
    )
    private static int classAverage50DisplayFloor;

    @Property(
        type = PropertyType.SELECTOR, name = "Class Average 50 Display Visibility",
        description = "Select when to show Class Average 50 Display on the screen.",
        category = "Dungeons", subcategory = "HUD", triggerActionOnInitialization = false,
        options = {"Only in Dungeons", "Only after Run End", "Only after Run End or in Dungeon Hub", "When in Dungeons or Dungeon Hub", "When in Skyblock", "Always"}
    )
    private static int classAverage50DisplayVisibility;

    @Property(
        type = PropertyType.SELECTOR, name = "Class Average 50 Display Compactness",
        description = "Select compactness mode to use when rendering text in Class Average 50 Display. The last option that only shows 1 class, shows the class you are currently playing, last played (stored in RAM only), or if both unknown, the class requiring least amount of runs.",
        category = "Dungeons", subcategory = "HUD", triggerActionOnInitialization = false,
        options = {"Show Everything", "Hide Header & Footer", "Hide Header, Footer & Classes Done", "Hide Header, Footer & Only Show 1 Class"}
    )
    private static int classAverage50DisplayCompactness;

    @Property(
        type = PropertyType.SELECTOR, name = "Class Average 50 Display Shadow",
        description = "Select shadow to use when rendering text in Class Average 50 Display.",
        category = "Dungeons", subcategory = "HUD",
        options = {"No Shadow", "Default Shadow", "Outline Shadow"}
    )
    private static int classAverage50DisplayShadow;

    @Property(
        type = PropertyType.SWITCH, name = "Unopened Chests Display",
        description = "Shows a HUD element that shows unopened chests at Croesus, along with a warning when you are at the chest limit. Requires the necessary tab list widget (which is enabled by default) to be enabled.",
        category = "Dungeons", subcategory = "HUD", triggerActionOnInitialization = false
    )
    private static boolean unopenedChestsDisplay;

    @Property(
        type = PropertyType.SWITCH, name = "Show Maxor Health Percentage",
        description = "Shows a HUD element with Maxor's health percentage, which doesn't disappear at times unlike the Skytils showing Maxor's HP in the bossbar, which disappears at times and makes you fail enrage skip. It is also more up-to-date in health values compared to Skytils.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean maxorHPDisplay;

    @Property(
        type = PropertyType.SWITCH, name = "Send Enrage Skip Helper Message",
        description = "Sends a message to party chat when Maxor has enough damage dealt for the first DPS phase (You only have to deal 25% damage, a.k.a lower it to 75% hp in the first DPS phase).",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean sendEnrageSkipHelperMessage;

    @Property(
        type = PropertyType.SWITCH, name = "Blessing on Screen",
        description = "Shows Blessing levels on screen with colors based on the level.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean blessingHud = true;

    @Property(
        type = PropertyType.SWITCH, name = "Send Detailed Blessings Message",
        description = "Sends a message to party chat at Master Mode Floor 7 Phase 5 about detailed blessings, including wisdom blessing (for mages) and base weapon damage gained from blessing of stone, which dragprio chattriggers module doesn't take into account.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean sendDetailedBlessingsMessage;

    @Property(
        type = PropertyType.SWITCH, name = "Hide Blessing if Level 0",
        description = "Hides blessings if they are Level 0.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean hideBlessingWhenZero;

    @Property(
        type = PropertyType.SWITCH, name = "Blessing of Power",
        description = "Enable or disable Blessing of Power on Blessing Hud.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean powerBlessing = true;

    @Property(
        type = PropertyType.SWITCH, name = "Blessing of Time",
        description = "Enable or disable Blessing of Time on Blessing Hud.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean timeBlessing = true;

    @Property(
        type = PropertyType.SWITCH, name = "Blessing of Life",
        description = "Enable or disable Blessing of Life on Blessing Hud.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean lifeBlessing = true;

    @Property(
        type = PropertyType.SWITCH, name = "Blessing of Stone",
        description = "Enable or disable Blessing of Stone on Blessing Hud.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean stoneBlessing;

    @Property(
        type = PropertyType.SWITCH, name = "Blessing of Wisdom",
        description = "Enable or disable Blessing of Wisdom on Blessing Hud.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean wisdomBlessing;

    @Property(
        type = PropertyType.SWITCH, name = "EHP on Screen",
        description = "Shows your Effective Health on screen while in Dungeons. Takes Absorption into account, changes color based on the EHP and floor. Also shows damage or healing received in parenthesis.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean ehpHud = true;

    @Property(
        type = PropertyType.SWITCH, name = "EHP on Screen out of Dungeons",
        description = "Makes EHP on Screen feature work out of dungeons, basing off of 40k EHP being safe and scaling the coloring based on that.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean ehpHudOutOfDungeons;

    @Property(
        type = PropertyType.SWITCH, name = "Rogue Sword Timer on Screen",
        description = "Shows time left for Rogue Sword speed boost in screen. Note: This timer is client side so it might be off by a few milliseconds depending on your ping.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean rogueSwordTimer;

    @Property(
        type = PropertyType.SWITCH, name = "Rogue Sword Timer on Screen out of Dungeons",
        description = "Makes Rogue Sword Timer feature work out of dungeons, useful for Bingo probably?",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean rogueSwordTimerOutOfDungeons;

    @Property(
        type = PropertyType.SWITCH, name = "Hide Rogue Sword Timer if Zero",
        description = "Hides the Rogue Sword Timer if the timer is at zero, a.k.a the speed boost got expired. Maybe useful if you want less clutter on screen.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean hideRogueSwordTimerOnceZero;

    @Property(
        type = PropertyType.SWITCH, name = "Golden Fish Timer",
        description = "Shows a timer for Golden Fish along with how much time left to throw your rod before you miss the chance of catching one. Shows as Ready after 15 minutes and Ready (Guaranteed) at 20 minutes, according to information from the official wiki. There's also a despawn timer that counts down time for your spawned Golden Fish to despawn (it despawns 60 seconds after the last interaction with it).",
        category = "Misc", subcategory = "HUD"
    )
    private static boolean goldenFishTimer;

    @Property(
        type = PropertyType.SWITCH, name = "SB Rejoin Cooldown After Kick Display",
        description = "Shows time left before you can rejoin SkyBlock after you get kicked in your screen.",
        category = "Misc", subcategory = "HUD"
    )
    private static boolean sbRejoinCooldownAfterKickDisplay;

    @Property(
        type = PropertyType.SWITCH, name = "FPS Limit Display",
        description = "Shows your FPS limit on the screen, unless it is set to unlimited. This is useful when you limit your FPS to save power when you are AFK but then you forget to set it to unlimited back when you return and you wonder why you are lagging and have 30 FPS. With this you will know since it will display it in your screen.",
        category = "Misc", subcategory = "HUD"
    )
    private static boolean fpsLimitDisplay;

    @Property(
        type = PropertyType.SWITCH, name = "FPS Display",
        description = "Shows your FPS on the screen, updating every second. Accurate to the millisecond precision. Shows in green if equal or above 60 FPS, yellow if equal or above 30 FPS, and red otherwise. Note: The displayed FPS will be different than what's shown on F3 because that takes average FPS, and this is the FPS according to time took to render last frame.",
        category = "Misc", subcategory = "HUD"
    )
    private static boolean fpsDisplay;

    /*@Property(
        type = PropertyType.SWITCH, name = "Dimensional Slash Alert",
        description = "Creates a title when you are in range of dimensional slash.",
        category = "Dungeons", subcategory = "Wither King Fight"
    )
    private static boolean dimensionalSlashAlert;*/

    /*@Property(
        type = PropertyType.SWITCH, name = "Show Dragon HP",
        description = "Shows dragon HPs on their body.",
        category = "Dungeons", subcategory = "Wither King Fight"
    )
    private static boolean showDragonHP;*/

    @Property(
        type = PropertyType.SWITCH, name = "Show Statue Box",
        description = "Shows statue boxes to help locate where to kill the dragons. Turn off any other statue box features in other mods to avoid conflicts.",
        category = "Dungeons", subcategory = "Wither King Fight"
    )
    private static boolean showStatueBox;

    @Property(
        type = PropertyType.SWITCH, name = "Sharper Dragon Bounding Box Lines",
        description = "Changes width of the lines in dragon statue boxes in M7 to be sharper.",
        category = "Dungeons", subcategory = "Wither King Fight"
    )
    private static boolean sharperDragonBoundingBox = true;

    @Property(
        type = PropertyType.DECIMAL_SLIDER, name = "Width of Lines in Dragon Bounding Boxes",
        description = "Width of lines in dragon bounding boxes to use. Skytils uses 3.69, DarkAddons default is 1.69. Change to your liking.",
        category = "Dungeons", subcategory = "Wither King Fight",
        minF = 0.01F,
        maxF = 5.00F,
        decimalPlaces = 2
    )
    private static float dragonBoundingBoxWidth = 1.69F;

    @Property(
        type = PropertyType.SWITCH, name = "More Accurate Dragon Bounding Boxes",
        description = "Changes dragon bounding boxes to be more accurate.",
        category = "Dungeons", subcategory = "Wither King Fight"
    )
    private static boolean moreAccurateDragonBoundingBoxes = true;

    @Property(
        type = PropertyType.SWITCH, name = "Hide Statue Box for Destroyed Statues",
        description = "If enabled, will skip rendering the boxes for already destroyed statues.",
        category = "Dungeons", subcategory = "Wither King Fight"
    )
    private static boolean hideStatueBoxForDestroyedStatues = true;

    /*@Property(
        type = PropertyType.SWITCH, name = "Show Dragon Spawn Timer",
        description = "Shows dragon spawn timer.",
        category = "Dungeons", subcategory = "Wither King Fight"
    )
    private static boolean showDragonSpawnTimer;*/

    /*@Property(
        type = PropertyType.SWITCH, name = "Change Hurt Color",
        description = "Changes hurt color on dragons to reduce tinting.",
        category = "Dungeons", subcategory = "Wither King Fight"
    )
    private static boolean changeHurtColorOnDragons;*/

    /*@Property(
        type = PropertyType.SWITCH, name = "Retexture Dragons",
        description = "Changes texture of dragons to their respective color.",
        category = "Dungeons", subcategory = "Wither King Fight"
    )
    private static boolean retextureDragons;*/

    /*@Property(
        type = PropertyType.SWITCH, name = "Skip Retexturing Irrelevant M7 Wither King Dragons",
        description = "Skips retexturing (replacing their texture by their color) dragons that you already destroyed the statues of. NOTE: Might cause issues or confusion.",
        category = "Dungeons", subcategory = "Wither King Fight"
    )
    private static boolean skipRetexturingIrrelevantDrags;*/

    /*@Property(
        type = PropertyType.SWITCH, name = "Hide Dragon Death",
        description = "Hides the death animation of dragons.",
        category = "Dungeons", subcategory = "Wither King Fight"
    )
    private static boolean hideDragonDeath;*/

    @Property(
        type = PropertyType.SWITCH, name = "Armor Stand Optimizer",
        description = "Optimizes armor stands by limiting the render-able armor stands on screen to the N closest to the player. NOTE: Might make stuff invisible in a weird way. If anything is invisible try turning this off and if its not invisible anymore, you know why.",
        category = "Performance", subcategory = "Experimental"
    )
    private static boolean armorStandOptimizer;

    @Property(
        type = PropertyType.SLIDER, name = "Armor Stand Limit",
        description = "Limit of armor stands rendered on the screen if the Armor Stand Optimizer is enabled. Too low values will cause stuff to be invisible. Too high values will hurt performance." + Utils.UNIX_NEW_LINE + Utils.UNIX_NEW_LINE + "If you are using 0 as a value consider disabling Armor Stand Optimizer and enabling \"Disable Armorstands\" in Patcher. If you want to use a limit above 500 you should disable the optimizer all together as the code required to filter the armor stands will likely hurt performance.",
        category = "Performance", subcategory = "Experimental",
        max = 500
    )
    private static int armorStandLimit = 50;

    @Property(
        type = PropertyType.SWITCH, name = "Delay Chunk Updates",
        description = "Delays chunk updates the more the lower your FPS is, this will help get your FPS up when a lot of chunks are queued to be loaded. This is different than Patcher's Chunk Update Limit and is recommended to use alongside with that.",
        category = "Performance", subcategory = "Experimental"
    )
    private static boolean delayChunkUpdates;

    @Property(
        type = PropertyType.SWITCH, name = "Hide Particles",
        description = "Hides Heart particles everywhere except in The Rift (since Splatters there steal your hearts and you need to pickup heart particles in the floor to regain your hearts), and all particles except important ones while in the Wither King fight, to get more FPS. Important particles are the ones that are cruicial, such as the ones that appear when a dragon is spawning, which is used by mods to display timers and notifications.",
        category = "Performance", subcategory = "Experimental"
    )
    private static boolean hideParticles;

    @Property(
        type = PropertyType.SWITCH, name = "Hide Falling Blocks",
        description = "Hides falling blocks for better performance. Might make you not see some animations made via falling blocks.",
        category = "Performance", subcategory = "Tweaks"
    )
    private static boolean hideFallingBlocks;

    @Property(
        type = PropertyType.SWITCH, name = "Hide Signs",
        description = "Hides signs while in Dungeons and Crystal Hollows. The signs are used as map placeholders by Hypixel and there is a lot of them at different places, so hiding will improve performance.",
        category = "Performance", subcategory = "Tweaks"
    )
    private static boolean hideSigns;

    @Property(
        type = PropertyType.SWITCH, name = "Disable Yield",
        description = "Removes a call to Thread#yield in Minecraft's game loop code, which prevents the game from reaching the maximum potential of the hardware. Although, this might make other operations that run on background slower.",
        category = "Performance", subcategory = "Tweaks"
    )
    private static boolean disableYield;

    @Property(
        type = PropertyType.SWITCH, name = "Thread Priority Tweaker",
        description = "Tweaks priorities of running threads (tasks) to provide better performance. This might make background tasks slower but should result in more FPS.",
        category = "Performance", subcategory = "Tweaks", triggerActionOnInitialization = false
    )
    private static boolean threadPriorityTweaker;

    @Property(
        type = PropertyType.SWITCH, name = "Reduce Background Threads",
        description = "Reduces background threads. Some mods create too many threads but not utilize them to the full, and those threads waste memory and CPU, since even if a thread is idle the Java runtime has to manage the thread and monitor its stack.",
        category = "Performance", subcategory = "Tweaks"
    )
    private static boolean reduceBackgroundThreads;

    @Property(
        type = PropertyType.SLIDER, name = "Main Menu Frame Limit",
        description = "Minecraft limits your frame rate to 30 by default whilst in main menu. This option overrides that if you set it to anything else than 30. If you have a high refresh rate monitor you might set this value to a higher value. And if you have a weak computer and you let the game run on background while on main menu a lot, you might lower it to save resources. Setting it to the maximum (260) disables the frame limit, running it with maximum FPS.",
        category = "Performance", subcategory = "Tweaks",
        min = 5,
        max = 260
    )
    private static int mainMenuFrameLimit = 30;

    @Property(
        type = PropertyType.SWITCH, name = "Tweak Java Flags",
        description = "Tweaks some Java Flags to make your game run smoother. Do note that, the CPU or memory usage may increase, because Java will spend more time optimizing Minecraft code, but it will run faster.",
        category = "Performance", subcategory = "Tweaks", triggerActionOnInitialization = false
    )
    private static boolean tweakJavaFlags;

    @Property(
        type = PropertyType.SWITCH, name = "Optimize Latency",
        description = "Optimizes latency/ping by using various TCP optimizations, like TCP_NODELAY, IP_TOS, TCP_FASTOPEN_CONNECT, etc and switching to PooledByteBufAllocator (the default in later versions of Minecraft) instead of the unpooled one.",
        category = "Performance", subcategory = "Tweaks"
    )
    private static boolean optimizeLatency;

    @Property(
        type = PropertyType.SWITCH, name = "Optimize Exceptions",
        description = "Optimizes some exceptions that happen inside Minecraft by caching their stacktraces, mostly on Hypixel related to duplicate teams.",
        category = "Performance", subcategory = "Tweaks"
    )
    private static boolean optimizeExceptions = true;

    @Property(
        type = PropertyType.SWITCH, name = "Smoothen Frames",
        description = "Improves your worst-case FPS by limiting number of (local) ticks that can happen in each frame to 1. The vanilla default is 10. This might make your jump animation seem like youre staying in the air for more time when your FPS is very low (i.e when you first open your game before Java Virtual Machine optimizes the code), but its purely client-side and has no disadvantages, other than improving your worst-case FPS.",
        category = "Performance", subcategory = "Tweaks"
    )
    private static boolean smoothenFrames;

    @Property(
        type = PropertyType.SWITCH, name = "Remove Armor Stands on Wither King and Sadan Fight",
        description = "Removes Armor Stands on Wither King and Sadan Fight to improve performance. This different than Armor Stand Optimizer! NOTE: Might cause stuff to be invisible!",
        category = "Performance", subcategory = "Experimental"
    )
    private static boolean removeArmorStandsOnWitherKingAndSadanFight;

    @Property(
        type = PropertyType.SWITCH, name = "Hide XP Orbs",
        description = "Hides XP Orbs to improve FPS. As obvious, this will make experience orbs, for example, from the Grand Experience Bottle and Titanic Experience Bottle, along with experience dropped from mobs invisible, to be more specific, non-existent, since it removes the whole entity instead of just not rendering it, resulting in it being not ticked anymore as well.",
        category = "Performance", subcategory = "Experimental"
    )
    private static boolean hideExperienceOrbs;

    /*@Property(
        type = PropertyType.SELECTOR, name = "Hide Wither King",
        description = "Hides Wither King during Phase 5 on Master Mode Floor 7 to improve FPS, with the specified mode. Hide just doesn't render it but keeps it as an entity in the world which will still tick it. Remove will completely remove it from the world as if it didn't exist at all, and disabled will not change anything.",
        category = "Performance", subcategory = "Experimental",
        options = {"Disabled", "Hide", "Remove"}
    )
    private static int hideWitherKing;*/

    @Property(
        type = PropertyType.SELECTOR, name = "Hide Wither Skeletons on Maxor Fight",
        description = "Hides Wither Skeletons on Maxor Fight, with the specified mode. Hide just doesn't render it but keeps it as an entity on the world which will still tick them. Remove will completely remove it from the world as if it didn't exist at all, and disabled will not change anything.",
        category = "Performance", subcategory = "Experimental",
        options = {"Disabled", "Hide", "Remove"}
    )
    private static int hideWitherSkeletonsOnMaxor;

    @Property(
        type = PropertyType.SWITCH, name = "Item Model Transparency Fix",
        description = "Fixes transparency issues in item models by completely overwriting the necessary vanilla methods. This option should work with most mods but if you got some exotic mods it might cause a conflict. Often also speeds up the game opening because of our code being more optimized, even if that wasn't the main intent. Requires restart to be effective.",
        category = "Performance", subcategory = "Patches"
    )
    private static boolean itemModelTransparencyFix;

    @Property(
        type = PropertyType.SWITCH, name = "Patch Memory Leaks",
        description = "Patches Memory Leaks, usually in Crimson Isle. Also removes a unused 10MB allocation created by vanilla Minecraft and stored forever, freeing memory. Every MB counts, W (Note: This of course doesn't fix all memory leaks.)",
        category = "Performance", subcategory = "Patches", triggerActionOnInitialization = false
    )
    private static boolean patchMemoryLeaks = true;

    @Property(
        type = PropertyType.SWITCH, name = "Remove Blank Armor Stands",
        description = "Removes blank armor stands used in various areas of Hypixel to improve performance. Warning: This might make some stuff invisible and uninteractable, if you have anything broken test with this disabled. If it fixes it you know why.",
        category = "Performance", subcategory = "Experimental"
    )
    private static boolean removeBlankArmorStands;

    @Property(
        type = PropertyType.SWITCH, name = "Disable Periodic Config Saves",
        description = "OneConfig and all mods directly or indirectly utilizing OneConfig (NEU included) does a save of all the config files every 30 seconds. This also why NEU config sometimes resets/corrupts. This behaviour is unhealthy because it does a force-save of all the config files every 30 seconds even if they are completely unchanged since the last save, it will increase I/O load and reduce SSD lifespan/increase SSD wear." + Utils.UNIX_NEW_LINE + Utils.UNIX_NEW_LINE + "This option will disable the periodic saves. A save will still be done when you quit the game cleanly via the Quit Game button on the Main Menu, but other ways to exit, i.e, power (electricity) loss, BSOD/kernel panic, game crash, or a process kill request, from command line or task manager, will not save the config files, so the settings you changed will be lost, although your configs will not be corrupted.",
        category = "Performance", subcategory = "Experimental"
    )
    private static boolean disablePeriodicConfigSaves;

    @Property(
        type = PropertyType.SWITCH, name = "BlockPos Optimizer",
        description = "Optimizes BlockPos by marking classes, fields and methods as final and recuding their visibilities, plus removing unused fields and parameters. Compatible with Patcher and Optifine, but might be problematic if you got some exotic mods that override BlockPos. Requires a restart once turned on to be effective since it needs to modify code of BlockPos before it gets loaded when the game first opens.",
        category = "Performance", subcategory = "Experimental"
    )
    private static boolean blockPosOptimizer;

    @Property(
        type = PropertyType.SWITCH, name = "NullStream Optimizer",
        description = "Optimizes NullStream, which is used when no twitch stream is detected (in the Twitch Integration of Minecraft), since Minecraft calls methods on this NullStream every frame, it will help performance. Requires restart to be effective.",
        category = "Performance", subcategory = "Experimental"
    )
    private static boolean nullStreamOptimizer;

    @Property(
        type = PropertyType.SWITCH, name = "Render Skytils Waypoints only when holding AOTV",
        description = "Renders Skytils waypoints in the world only if you are holding Aspect of the Void, which will improve performance if you have many waypoints.",
        category = "Performance", subcategory = "Experimental"
    )
    private static boolean renderSkytilsWaypointsOnlyWhenHoldingAOTV;

    /*@Property(
        type = PropertyType.SWITCH, name = "Fail Notification",
        description = "Shows a title message on screen when the 4th dragon is spawned telling you there are 4 dragons alive (usually you'll have 35s left before 5th spawns) and also when the 5th dragon is spawning, but that one will be late since the dragons usually spawn 5 seconds after the spawning notification.",
        category = "Dungeons", subcategory = "Notifiers"
    )
    private static boolean witherKingFightFailNotification = true;*/

    @Property(
        type = PropertyType.SWITCH, name = "M7 Dragon Hud",
        description = "Shows dragon list and status on screen.",
        category = "Dungeons", subcategory = "HUD"
    )
    private static boolean dragonHud = true;

    @Property(
        type = PropertyType.SELECTOR, name = "Dragon Hud Shadow",
        description = "Select shadow to use when rendering text in M7 Dragon Hud.",
        category = "Dungeons", subcategory = "HUD",
        options = {"No Shadow", "Default Shadow", "Outline Shadow"}
    )
    private static int dragonHudShadow;

    @Property(
        type = PropertyType.SWITCH, name = "Dragon Spawning Notification",
        description = "Shows a title message on screen when a dragon is spawning.",
        category = "Dungeons", subcategory = "Notifiers"
    )
    private static boolean spawningNotification = true;

    @Property(
        type = PropertyType.SWITCH, name = "Dragon In Statue Notification",
        description = "Shows a title message on screen when a dragon is in its statue, telling you to kill it.",
        category = "Dungeons", subcategory = "Notifiers"
    )
    private static boolean killNotification = true;

    @Property(
        type = PropertyType.SWITCH, name = "Statue Destroyed Notification",
        description = "Shows a title message on screen after you successfully kill a dragon in its statue and destroy the statue.",
        category = "Dungeons", subcategory = "Notifiers"
    )
    private static boolean statueDestroyedNotification = true;

    @Property(
        type = PropertyType.SWITCH, name = "Statue Missed Notification",
        description = "Shows a title message on screen if you kill a dragon out of its statue.",
        category = "Dungeons", subcategory = "Notifiers"
    )
    private static boolean statueMissedNotification = true;

    @Property(
        type = PropertyType.SWITCH, name = "Ultimate Reminder",
        description = "Shows a title message on screen after the first laser in Maxor and after terminals are done (for the Goldor fight), to remind you about using your ultimate class ability.",
        category = "Dungeons", subcategory = "Reminders"
    )
    private static boolean ultReminder = true;

    @Property(
        type = PropertyType.SWITCH, name = "Send Message for Wish",
        description = "Additionally sends a message to party chat requesting wish from healer when Maxor enrages or when Goldor fight starts.",
        category = "Dungeons", subcategory = "Reminders"
    )
    private static boolean sendMessageForWish;

    @Property(
        type = PropertyType.SWITCH, name = "Edrag Reminder",
        description = "Shows a title message on screen when Phase 4 ends to remind you about equipping Ender Dragon pet.",
        category = "Dungeons", subcategory = "Reminders"
    )
    private static boolean edragReminder = true;

    @Property(
        type = PropertyType.SWITCH, name = "Phase 3 Starting Notification",
        description = "Shows a title message on screen when Phase 2 ends to remind you about Phase 3 starting.",
        category = "Dungeons", subcategory = "Reminders"
    )
    private static boolean phase3StartingNotification = true;

    @Property(
        type = PropertyType.SWITCH, name = "Phase 5 Starting Notification",
        description = "Shows a title message on screen when Phase 5 is starting.",
        category = "Dungeons", subcategory = "Reminders"
    )
    private static boolean phase5StartingNotification = true;

    @Property(
        type = PropertyType.SWITCH, name = "Send Message on 270 Score",
        description = "Uses score from scoreboard to determine the real score and send the 270 Score message. Doesn't send duplicate messages if Skytils message is enabled. Since this is works with scoreboard score, it will always have the correct mimic and spirit pet scores, and has logic to add score from the Blaze Puzzle if a teammate (or you) sends Blaze Done message and Blaze puzzle is not completed yet. It is recommended to have both Skytils and DarkAddons score messages enabled for best results.",
        category = "Dungeons", subcategory = "Helpers"
    )
    private static boolean sendMessageOn270Score;

    @Property(
        type = PropertyType.SWITCH, name = "Send Message on 300 Score",
        description = "Uses score from scoreboard to determine the real score and send the 300 Score message. Doesn't send duplicate messages if Skytils message is enabled. Since this is works with scoreboard score, it will always have the correct mimic and spirit pet scores, and has logic to add score from the Blaze Puzzle if a teammate (or you) sends Blaze Done message and Blaze puzzle is not completed yet. It is recommended to have both Skytils and DarkAddons score messages enabled for best results.",
        category = "Dungeons", subcategory = "Helpers"
    )
    private static boolean sendMessageOn300Score;

    @Property(
        type = PropertyType.SWITCH, name = "Send Message for Score At Boss Entry and Affordable Deaths for S+",
        description = "Sends a message on boss entry showing the score at boss entry and affordable deaths to still have S+ rank. This assumes first death will have spirit pet.",
        category = "Dungeons", subcategory = "Helpers"
    )
    private static boolean sendMessageForScoreAtBossEntry;

    @Property(
        type = PropertyType.SWITCH, name = "Send Message on Melody Terminal",
        description = "Sends message when opening melody terminal or when progressing on it.",
        category = "Dungeons", subcategory = "Helpers"
    )
    private static boolean sendMessageOnMelodyTerminal;

    @Property(
        type = PropertyType.SWITCH, name = "Send Message for Ragnarock Axe Strength Gained",
        description = "Sends message after using ragnarock axe to party chat about strength gained.",
        category = "Dungeons", subcategory = "Helpers"
    )
    private static boolean sendMessageOnRagAxe;

    @Property(
        type = PropertyType.SWITCH, name = "Dialogue Skip Helper",
        description = "Shows a title and plays a sound when you should start killing blood mobs.",
        category = "Dungeons", subcategory = "Helpers"
    )
    private static boolean dialogueSkipHelper;

    @Property(
        type = PropertyType.SWITCH, name = "Chroma/Rainbow Toggle",
        description = "If enabled, will use chroma/rainbow color in various places. (Requires SkyblockAddons)",
        category = "Misc", subcategory = "Chroma"
    )
    private static boolean chromaToggle = true;

    @Property(
        type = PropertyType.SWITCH, name = "Chroma Skyblock in Scoreboard",
        description = "Changes Skyblock text in scoreboard to be chroma color (Requires SkyblockAddons)" + Utils.UNIX_NEW_LINE + "NOTE: May break Cowlection, turn on work outside skyblock in cowlection settings.",
        category = "Misc", subcategory = "Chroma"
    )
    private static boolean chromaSkyblock;

    @Property(
        type = PropertyType.SWITCH, name = "Always Sprint",
        description = "Makes it so you always sprint, regardless of if you toggled sprint via other mods, or if you were holding CTRL or double tapping W (with default controls). Remove any other toggle sprint mod to avoid conflicts.",
        category = "QOL", subcategory = "General"
    )
    private static boolean alwaysSprint;

    @Property(
        type = PropertyType.SWITCH, name = "Full Bright",
        description = "Removes lightning updates and always uses light level 15, a.k.a everything will be as bright as it can, even without any light sources. This will improve performance in addition to providing a clean/better/brighter view. Disable any other Fullbright mod or feature to avoid conflicts.",
        category = "QOL", subcategory = "General", triggerActionOnInitialization = false
    )
    private static boolean fullBright;

    @Property(
        type = PropertyType.SWITCH, name = "Auto Melody",
        description = "Automatically clicks on Melody notes. This is a cheat. Use it as a last resort and in your own risk. This will still fail on high ping, low tps or fps conditions, but you can't do any better than it manually.",
        category = "QOL", subcategory = "General"
    )
    private static boolean autoMelody;

    @Property(
        type = PropertyType.SWITCH, name = "Auto Dance",
        description = "Automatically jumps, sneaks or punches when told on the Dance minigame on the Mirrorverse, on Rift. You still have to move into the glass blocks manually. This is a cheat. Use it as a last resort and in your own risk. This will still fail on high ping, low tps or fps conditions.",
        category = "QOL", subcategory = "General"
    )
    private static boolean autoDance;

    @Property(
        type = PropertyType.SWITCH, name = "Auto Fishing Rod",
        description = "While fishing, automatically pulls the rod when a catch is hooked and then re-throws it. You need to manually throw it the first time for it to start working. You need SkyBlock Menu -> Settings -> Personal -> Fishing Settings -> Fishing Status Holograms and Fishing Timer enabled for it to work. This does not automatically kill any sea creatures. Should work well for Trophy Fishing. The mod will move your head every 20 seconds to bypass anti-AFK system of Hypixel.",
        category = "QOL", subcategory = "General"
    )
    private static boolean autoFishingRod;

    @Property(
        type = PropertyType.NUMBER, name = "Auto Fishing Rod Starting Delay Ticks",
        description = "Starting delay in ticks before hooking/re-throwing the rod. The actual delay will be random amount of ticks in the range of starting and maximum delay ticks. Note: Too high delay value will make you miss the catch.",
        category = "QOL", subcategory = "General",
        min = 0,
        max = 10
    )
    private static int autoFishingRodStartingDelay = 4;

    @Property(
        type = PropertyType.NUMBER, name = "Auto Fishing Rod Maximum Delay Ticks",
        description = "Maximum delay in ticks before hooking/re-throwing the rod. The actual delay will be random amount of ticks in the range of starting and maximum delay ticks. Note: Too high delay value will make you miss the catch.",
        category = "QOL", subcategory = "General",
        min = 0,
        max = 10
    )
    private static int autoFishingRodMaximumDelay = 5;

    @Property(
        type = PropertyType.SWITCH, name = "Auto Fishing Rod Slugfish Mode",
        description = "Only pulls the rod for a catch if the bobber was inside the lava for more than 10 seconds. You need a Level 100 Legendary Slug pet equipped to actually get the Slugfish since it doesn't support 20 seconds.",
        category = "QOL", subcategory = "General"
    )
    private static boolean autoFishingRodSlugfishMode;

    @Property(
        type = PropertyType.SWITCH, name = "Auto Fishing Rod Golden Fish Mode",
        description = "Will attempt to automatically catch Golden Fish. You need to be fishing on a small lava pool (preferably with just 1 block of lava, but keep distance between you and your bobber so that when the Golden Fish spawns, right clicking will throw your rod instead of trying to right click to the Golden Fish entity) for the Golden Fish to not move away, otherwise the mod will not be able to auto catch the Golden Fish. Recommended fishing spot: Stand at -481 178 -501 and throw your rod to lava block at -485 176 -501. When this mode is enabled, if a catch takes more than 10 seconds and slug fish mode is not enabled, it will re-throw the rod. This because, while fishing at a 1 block lava spot, sometimes when you throw your rod it goes into a block instead of lava, re-throwing the rod fixes this. For this reason, if you are going to fish at that spot and use Golden Fish Mode, disable Slugfish Mode first.",
        category = "QOL", subcategory = "General"
    )
    private static boolean autoFishingRodGoldenFishMode;

    @Property(
        type = PropertyType.SWITCH, name = "Aggressive Jump",
        description = "Causes jumping in Auto Dance to be done more aggressively. May help if you are failing a lot with \"You weren't mid-air!\", but it might also make you get off of the glass block, because this makes the mod able to jump while you are not standing still, which causes your momentum to change.",
        category = "QOL", subcategory = "General"
    )
    private static boolean aggressiveJump;

    @Property(
        type = PropertyType.SWITCH, name = "Mining Ping Fix",
        description = "Prevents lore updates on your mining tool (drill, gauntlet, pickaxes) from resetting your breaking progress, which reduces your money from mining the more ping you have. Without the fix, money from mining is less, because you lose an additional tick (50 milliseconds) per block broken. With maxed Mining Speed from maxed Mining Setup, you are able to mine 1 block in about 10-11 ticks, and losing 1 tick means -10%% less profits, and this is gets higher the higher your ping is. Not to mention the need to also re-click LC and aim again; it's probably more than 10%%.",
        category = "QOL", subcategory = "General"
    )
    private static boolean miningPingFix;

    @Property(
        type = PropertyType.SWITCH, name = "Create Ghost Block With Key",
        description = "Creates ghost block at the block you are looking when you press the configured key, which is G by default. To change it go to standard Minecraft vanilla Controls menu in Settings.",
        category = "QOL", subcategory = "General"
    )
    private static boolean createGhostBlockWithKey;

    @Property(
        type = PropertyType.SWITCH, name = "Create More Ghost Blocks when Holding the Key",
        description = "Makes it so you don't have to press the key 1 time per block and keeps creating ghost blocks while you are holding down the key. Might make it generate more ghost blocks than necessary even if you just press the key 1 time if enabled.",
        category = "QOL", subcategory = "General"
    )
    private static boolean keepHoldingToCreateMoreGhostBlocks;

    @Property(
        type = PropertyType.SWITCH, name = "Allow Ghost Blocking Bedrock and Barrier",
        description = "Makes it so you can ghost block bedrock and barrier as well, which is normally blacklisted for safety. Just ghost blocking them should be safe but interacting with i.e chests behind barriers or passing through a ghost blocked barrier or bedrock wall might be unsafe.",
        category = "QOL", subcategory = "General"
    )
    private static boolean allowGhostBlockingBedrockAndBarrier;

    @Property(
        type = PropertyType.SWITCH, name = "Press Key to Requeue",
        description = "Requeues into another instanced game when you press the configured key (by default R, go to standard Minecraft vanilla Controls menu in Settings if you want to change it). Instanced in this context means Dungeons or Kuudra in the context of Skyblock, but any game that supports the /instancerequeue command will work.",
        category = "QOL", subcategory = "General"
    )
    private static boolean pressKeyToRequeue;

    @Property(
        type = PropertyType.SWITCH, name = "Auto Extra Stats",
        description = "Automatically sends the command required to view the extra stats after the end of a dungeon run, allowing you to see additional stats such as secrets found.",
        category = "QOL", subcategory = "General"
    )
    private static boolean autoExtraStats;

    @Property(
        type = PropertyType.SWITCH, name = "Auto Close Chests",
        description = "Automatically closes secret item chests instantaneously after opening while inside Dungeons, just like if you were holding a bow.",
        category = "QOL", subcategory = "General"
    )
    private static boolean autoCloseChests;

    @Property(
        type = PropertyType.SWITCH, name = "Hacking For Dummies Solver",
        description = "Solver for the hacking for dummies quest given by the Kloon in The Rift, in the area where normally the Guy is, in the overworld. This not a cheat, just a solver, you still need to click manually but it's very easy with the solver unlike melody, so I didn't felt the need to make it click automatically. NOTE: This only helps with knowing what buttons to click on the hacking GUI, for what color each term should be after hacking, figure it yourself or watch a guide.",
        category = "QOL", subcategory = "General"
    )
    private static boolean hackingForDummiesSolver;

    @Property(
        type = PropertyType.SWITCH, name = "Auto Salvation",
        description = "Automatically left clicks to use the salvation ability every 0.25s while you are holding down right click, while holding a bow whose name contains \"Terminator\". This is a cheat. Use at your own risk.",
        category = "QOL", subcategory = "General"
    )

    private static boolean autoSalvation;

    @Property(
        type = PropertyType.SELECTOR, name = "Auto Salvation CPS Limit",
        description = "Limit of CPS in Auto Salvation. Does not limit CPS of clicks that doesn't originate from auto salvation. This only a upper limit and if your client-side TPS is low, the actual CPS will be lower than this value. The maximum is 15 because you can't leftclick and rightclick at the same time in Minecraft, and to shoot Terminator at max speed you need to hold down Right Click, which sends a click every 200 ms (vanilla mechanic), which is 4 ticks (or 5 cps), therefore leaving us 16 ticks (or 15 cps) to left click on. The admins have said the salvation cooldown is 0.25s on patch notes, meaning 4 cps should be enough for maximum DPS in practice, but this wasn't the case with right click before so higher values might increase DPS. Usually not worth the extra risk though.",
        category = "QOL", subcategory = "General",
        options = {"4", "4/8 (Random)", "8", "8/10 (Random)", "10", "10/15 (Random)", "15"}
    )
    private static int autoSalvationCpsLimit;

    @Property(
        type = PropertyType.SWITCH, name = "Disable Auto Salvation in Thorn Boss",
        description = "Automatically disables the Auto Salvation while in the Thorn Boss fight. Useful if you need to term stun in case things go wrong.",
        category = "QOL", subcategory = "General"
    )

    private static boolean disableAutoSalvationInThornBoss;

    @Property(
        type = PropertyType.SWITCH, name = "LeftClick AutoClicker",
        description = "Automatically left clicks while you are holding down left click, while holding a sword. This is a cheat. Use at your own risk.",
        category = "QOL", subcategory = "General"
    )
    private static boolean leftClickAutoClicker;

    @Property(
        type = PropertyType.SELECTOR, name = "LeftClick CPS Limit",
        description = "Limit of CPS in LeftClick AutoClicker. This affects only Left clicks. Does not limit CPS of clicks that doesn't originate from auto clicker. This only a upper limit and if your client-side TPS is low, the actual CPS will be lower than this value. Support for above 20 or more customizable CPS values will be added later.",
        category = "QOL", subcategory = "General",
        options = {"45", "40", "40/20 (Random)", "20", "20/10 (Random)", "15", "10", "5", "4", "2", "1"}
    )
    private static int leftClickCpsLimit = 5;

    @Property(
        type = PropertyType.SWITCH, name = "RightClick AutoClicker",
        description = "Automatically right clicks while you are holding down right click, while holding Hyperion, Astraea or Terminator. This is a cheat. Use at your own risk.",
        category = "QOL", subcategory = "General"
    )
    private static boolean rightClickAutoClicker;

    @Property(
        type = PropertyType.SELECTOR, name = "RightClick CPS Limit",
        description = "Limit of CPS in RightClick AutoClicker. This affects only Right clicks. Does not limit CPS of clicks that doesn't originate from auto clicker. This only a upper limit and if your client-side TPS is low, the actual CPS will be lower than this value. Support for above 20 or more customizable CPS values will be added later.",
        category = "QOL", subcategory = "General",
        options = {"45", "40", "40/20 (Random)", "20", "20/10 (Random)", "15", "10", "5", "4", "2", "1"}
    )
    private static int rightClickCpsLimit = 3;

    /*@Property(
        type = PropertyType.SWITCH, name = "Catchup AutoClicker",
        description = "Tries to keep the CPS up to the limit even in low TPS conditions by emulating AutoClicker ticks for the dropped ticks. Recommended to enable if you have enabled \"Smoothen Frames\" option.",
        category = "QOL", subcategory = "General"
    )
    private static boolean catchupAutoClicker;*/

    @Property(
        type = PropertyType.SWITCH, name = "Auto Regular Ability",
        description = "Uses regular ability once the cooldown expires, while holding down RC while holding a Terminator (or LC while holding Dark Claymore/Midas/GS/Hype), inside Dungeons if the boss is not defeated yet. This uses Shift + Drop for all classes. Healer will use Healing Circle (2s cd), Mage will use Guided Sheep (30s cd), Berserk will use Throwing Axe (10s cd), Archer will use Explosive Shot (40s cd), Tank will use Seismic Wave (15s cd).",
        category = "QOL", subcategory = "General"
    )
    private static boolean autoRegularAbility;

    @Property(
        type = PropertyType.SWITCH, name = "Auto Ultimate Ability",
        description = "Uses ultimate ability once the cooldown expires, while holding down RC while holding a Terminator (or LC while holding Dark Claymore/Midas/GS/Hype), inside Dungeons if the boss is not defeated yet. This uses Drop key for all classes. Healer will use Wish (2m cd), Mage will use Thunderstorm (8.3m cd), Berserk will use Ragnarok (1m cd), Archer will use Rapid Fire (1.6m cd), Tank will use Seismic Wave (2.5m cd).",
        category = "QOL", subcategory = "General"
    )
    private static boolean autoUltimateAbility;

    /*@Property(
        type = PropertyType.SWITCH, name = "Modify Window Title",
        description = "Modifies Window title and adds DarkAddons version to it. Purely cosmetic.",
        category = "Misc", subcategory = "Window Options"
    )
    private static boolean modifyWindowTitle;*/

    @Property(
        type = PropertyType.SWITCH, name = "Unsafe Mode",
        description = "Enables Unsafe Mode, causing errors to be not caught. This usually improves performance at the cost of safety. If you get errors spammed in chat, enabling this will make it so the error is either ignored silently or it crashes your game.",
        category = "Misc", subcategory = "Unsafe Options"
    )
    private static boolean unsafeMode;

    @Property(
        type = PropertyType.SWITCH, name = "Debug Mode",
        description = "Enables Debug Mode, that prints extra information for some features known to have bugs. Do not enable this unless you know what you are doing. Might decrease performance.",
        category = "Misc", subcategory = "Developer Options"
    )
    private static boolean debugMode;

    @Property(
        type = PropertyType.SWITCH, name = "Profiler Mode",
        description = "Enables extra profiling information in Debug Pie Chart, such as letting all Forge Event Listeners have an entry in the Pie Chart with the corresponding mod's modid plus the listener method name. Since we have to store the method name, this setting might increase memory usage a little. Requires restart to be fully effective, otherwise only the event listeners registered after changing the option to true will be shown on the Pie Chart.",
        category = "Misc", subcategory = "Developer Options"
    )
    private static boolean profilerMode;

    @Property(
        type = PropertyType.SWITCH, name = "Extra Luck",
        description = "If you are a regular user of the mod, gives you extra luck (haters will say it's fake). Otherwise, enables some developer-only features.",
        category = "Misc", subcategory = "Developer Options"
    )
    private static boolean extraLuck;

    @Property(
        type = PropertyType.SWITCH, name = "Century Raffle Ticket Timer",
        description = "Shows time till you can get a Raffle Ticket in the Century Raffle event. If unknown or negative value it will show as 20min. This depends on the chat messages to know when you get a ticket. Note: This was a feature dedicated to Year 300 Raffle Event, unless another Raffle Event happens in SkyBlock you probably shouldn't enable this feature.",
        category = "Misc", subcategory = "Events"
    )
    private static boolean centuryRaffleTicketTimer;

    @Property(
        type = PropertyType.SWITCH, name = "Slayer RNG Display",
        description = "Shows a HUD element with your Odds to drop certain RNG drops, an ETA time for when you will logically drop it, your money per hour from that drop along with that drop's live price on your screen, and more! This with your own Magic Find and RNG Meter Progress! For example, if you have 300 Magic Find and your Judgement Core meter is %%50, then you have 1/221 Odds for it!" + Utils.UNIX_NEW_LINE + Utils.UNIX_NEW_LINE + "After enabling you have to do some bosses to drop a rare drop for the mod to detect your Magic Find, RNG Meter Progress and AVG Boss Kill Time. The Odds change if your Magic Find changes, so don't be suprised when the odds goes higher when you fail to magic find swap, for example. Highering your RNG Meter also reduces (increases the chances) the Odds live, for example from 1/221 into 1/220." + Utils.UNIX_NEW_LINE + Utils.UNIX_NEW_LINE + "Disclaimer: The Money per Hour is just from that RNG drop, ignoring everything else. Your Money per Hour will likely be more since you will often get your drop before the ETA along with other drops. Although, it ignores taxes and slayer quest starting costs; so if the slayer you're doing only has 1 good drop, your Money per Hour will be less than what's shown, cause of the taxes and quest starting costs.",
        category = "Slayers", subcategory = "HUD", triggerActionOnInitialization = false
    )
    private static boolean slayerRngDisplay = true;

    @Property(
        type = PropertyType.SWITCH, name = "Burgers Done",
        description = "Turn on to mark McGrubber's Burgers eaten as 5 out of 5, which will make the Slayer RNG Display show The One IV book if doing Tier 5 Vampires, or Unfanged Vampire Part otherwise for coin profit in overworld. When disabled, The Burger will override The One IV and Vampire Part drops as the RNG Meter goal.",
        category = "Slayers", subcategory = "HUD", triggerActionOnInitialization = false
    )
    private static boolean burgersDone;

    private static boolean initialized;

    @NotNull
    private static final File CONFIG_FILE = new File(new File(new File("config"), "darkaddons"), "config.toml");

    private static final void reloadChunks(final boolean ignoredValue) {
        Utils.reloadChunks();
    }

    private static final void switchThreadPriorityTweaker(final boolean state) {
        if (state) {
            // Delay needed because the listener runs before the value is actually changed, resulting in tweakPriorities method
            // doing nothing.
            DarkAddons.runOnceInNextTick("config_hook_tweak_thread_priorities", ThreadPriorityTweaker::tweakPriorities);
        } else {
            ThreadPriorityTweaker.restorePriorities();
        }
    }

    private static final void updateMemoryReserve(final boolean state) {
        if (state) {
            MemoryLeakFix.freeUnnecessary();
        } else {
            MemoryLeakFix.restoreReservedMemory();
        }
    }

    private static final void updateWelcomeMessage(final boolean state) {
        if (state) {
            DarkAddons.queueWelcomeMessage();
        } else {
            DarkAddons.dequeueWelcomeMessage();
        }
    }

    private static final void reTweakJavaFlags(final boolean state) {
        if (state) {
            DarkAddons.tweakFlags();
        }
    }

    private static final void reSyncClassXP(final boolean state) {
        if (state) {
            ClassAverage50Display.syncClassXP();
        }
    }

    private static final void reSyncClassXPUnconditionally(final int state) {
        ClassAverage50Display.syncClassXP();
    }

    private static final void updateBurgersDone(final boolean state) {
        // Delay needed because the listener runs before the value is actually changed.
        DarkAddons.runOnceInNextTick("config_hook_update_burgers_done", SlayerRNGDisplay::markUpdateNeeded);
    }

    private static final void updateBlockPosOptimizer(final boolean state) {
        TinyConfig.setBoolean("blockPosOptimizer", state);
    }

    private static final void updateNullStreamOptimizer(final boolean state) {
        TinyConfig.setBoolean("nullStreamOptimizer", state);
    }

    private static final void updateItemModelTransparencyFix(final boolean state) {
        TinyConfig.setBoolean("itemModelTransparencyFix", state);
    }

    private final void addDependencies() {
        this.addDependency("sharperDragonBoundingBox", Config.SHOW_STATUE_BOX);
        this.addDependency("dragonBoundingBoxWidth", Config.SHOW_STATUE_BOX);
        this.addDependency("moreAccurateDragonBoundingBoxes", Config.SHOW_STATUE_BOX);

        //this.addDependency("skipRetexturingIrrelevantDrags", "retextureDragons");

        // Elementa doesn't support more than one dependency on the same property, so we have to use the predicate.
        //this.addDependency("dragonBoundingBoxWidth", Config.SHOW_STATUE_BOX);
        this.addDependency("dragonBoundingBoxWidth", "sharperDragonBoundingBox", (@NotNull final Boolean state) -> state && Config.showStatueBox);

        this.addDependency("hideStatueBoxForDestroyedStatues", Config.SHOW_STATUE_BOX);
        this.addDependency("armorStandLimit", "armorStandOptimizer");
        this.addDependency("dragonHudShadow", "dragonHud");

        this.addDependency("hideBlessingWhenZero", Config.BLESSING_HUD);
        this.addDependency("powerBlessing", Config.BLESSING_HUD);
        this.addDependency("timeBlessing", Config.BLESSING_HUD);
        this.addDependency("lifeBlessing", Config.BLESSING_HUD);
        this.addDependency("stoneBlessing", Config.BLESSING_HUD);
        this.addDependency("wisdomBlessing", Config.BLESSING_HUD);

        this.addDependency("ehpHudOutOfDungeons", "ehpHud");

        this.addDependency("chromaSkyblock", "chromaToggle");
        this.addDependency("sendMessageForScoreAtBossEntry", "sendMessageOn300Score");
        this.addDependency("sendEnrageSkipHelperMessage", "maxorHPDisplay");
        this.addDependency("sendMessageForWish", "ultReminder");

        this.addDependencies2();
    }

    private final void addDependencies2() {
        this.addDependency("autoSalvationCpsLimit", "autoSalvation");
        this.addDependency("disableAutoSalvationInThornBoss", "autoSalvation");

        this.addDependency("leftClickCpsLimit", "leftClickAutoClicker");
        this.addDependency("rightClickCpsLimit", "rightClickAutoClicker");

        this.addDependency("rogueSwordTimerOutOfDungeons", "rogueSwordTimer");
        this.addDependency("hideRogueSwordTimerOnceZero", "rogueSwordTimer");

        this.addDependency("aggressiveJump", "autoDance");

        this.addDependency("classAverage50DisplayFloor", "classAverage50Display");
        this.addDependency("classAverage50DisplayVisibility", "classAverage50Display");
        this.addDependency("classAverage50DisplayShadow", "classAverage50Display");
        this.addDependency("classAverage50DisplayCompactness", "classAverage50Display");

        this.addDependency("burgersDone", "slayerRngDisplay");

        this.addDependency("keepHoldingToCreateMoreGhostBlocks", "createGhostBlockWithKey");
        this.addDependency("allowGhostBlockingBedrockAndBarrier", "createGhostBlockWithKey");

        this.addDependency("autoFishingRodStartingDelay", "autoFishingRod");
        this.addDependency("autoFishingRodMaximumDelay", "autoFishingRod");
        this.addDependency("autoFishingRodSlugfishMode", "autoFishingRod");
        this.addDependency("autoFishingRodGoldenFishMode", "autoFishingRod");
    }

    private final void addListeners() {
        this.registerListener("fullBright", Config::reloadChunks);
        this.registerListener("threadPriorityTweaker", Config::switchThreadPriorityTweaker);

        this.registerListener("patchMemoryLeaks", Config::updateMemoryReserve);
        this.registerListener("welcomeMessage", Config::updateWelcomeMessage);

        this.registerListener("tweakJavaFlags", Config::reTweakJavaFlags);

        this.registerListener("classAverage50Display", Config::reSyncClassXP);
        this.registerListener("classAverage50DisplayFloor", Config::reSyncClassXPUnconditionally);
        this.registerListener("classAverage50DisplayCompactness", Config::reSyncClassXPUnconditionally);

        this.registerListener("burgersDone", Config::updateBurgersDone);
        this.registerListener("blockPosOptimizer", Config::updateBlockPosOptimizer);
        this.registerListener("nullStreamOptimizer", Config::updateNullStreamOptimizer);

        this.registerListener("itemModelTransparencyFix", Config::updateItemModelTransparencyFix);
    }

    private final void addDependencyAndListeners() {
        this.addDependencies();
        this.addListeners();
    }

    // TODO use this.hidePropertyIf to hide chroma related settings if SBA is not installed
    Config() {
        // TODO this super call calls the super on WindowScreen with ElementaVersion.V2 instead of V5
        super(Config.CONFIG_FILE, "DarkAddons v" + Reference.VERSION, new JVMAnnotationPropertyCollector(), new Config.ConfigSorting());

        this.addDependencyAndListeners();
    }

    static final boolean isWelcomeMessage() {
        Config.checkUninit();

        return Config.welcomeMessage;
    }

    static final boolean isUpdateChecker() {
        Config.checkUninit();

        return Config.updateChecker;
    }

    static final boolean isClassAverage50Display() {
        Config.checkUninit();

        return Config.classAverage50Display;
    }

    static final int getClassAverage50DisplayFloor() {
        Config.checkUninit();

        return Config.classAverage50DisplayFloor;
    }

    static final int getClassAverage50DisplayVisibility() {
        Config.checkUninit();

        return Config.classAverage50DisplayVisibility;
    }

    static final int getClassAverage50DisplayShadow() {
        Config.checkUninit();

        return Config.classAverage50DisplayShadow;
    }

    static final int getClassAverage50DisplayCompactness() {
        Config.checkUninit();

        return Config.classAverage50DisplayCompactness;
    }

    static final boolean isUnopenedChestsDisplay() {
        Config.checkUninit();

        return Config.unopenedChestsDisplay;
    }

    static final boolean isMaxorHPDisplay() {
        Config.checkUninit();

        return Config.maxorHPDisplay;
    }

    static final boolean isSendEnrageSkipHelperMessage() {
        Config.checkUninit();

        return Config.sendEnrageSkipHelperMessage;
    }

    static final boolean isBlessingHud() {
        Config.checkUninit();

        return Config.blessingHud;
    }

    static final boolean isSendDetailedBlessingsMessage() {
        Config.checkUninit();

        return Config.sendDetailedBlessingsMessage;
    }

    static final boolean isHideBlessingWhenZero() {
        Config.checkUninit();

        return Config.hideBlessingWhenZero;
    }

    static final boolean isPowerBlessing() {
        Config.checkUninit();

        return Config.powerBlessing;
    }

    static final boolean isTimeBlessing() {
        Config.checkUninit();

        return Config.timeBlessing;
    }

    static final boolean isLifeBlessing() {
        Config.checkUninit();

        return Config.lifeBlessing;
    }

    static final boolean isStoneBlessing() {
        Config.checkUninit();

        return Config.stoneBlessing;
    }

    static final boolean isWisdomBlessing() {
        Config.checkUninit();

        return Config.wisdomBlessing;
    }

    static final boolean isEhpHud() {
        Config.checkUninit();

        return Config.ehpHud;
    }

    static final boolean isEhpHudOutOfDungeons() {
        Config.checkUninit();

        return Config.ehpHudOutOfDungeons;
    }

    static final boolean isRogueSwordTimer() {
        Config.checkUninit();

        return Config.rogueSwordTimer;
    }

    static final boolean isRogueSwordTimerOutOfDungeons() {
        Config.checkUninit();

        return Config.rogueSwordTimerOutOfDungeons;
    }

    static final boolean isHideRogueSwordTimerOnceZero() {
        Config.checkUninit();

        return Config.hideRogueSwordTimerOnceZero;
    }

    static final boolean isGoldenFishTimer() {
        Config.checkUninit();

        return Config.goldenFishTimer;
    }

    static final boolean isSendMessageOn270Score() {
        Config.checkUninit();

        return Config.sendMessageOn270Score;
    }

    static final boolean isSendMessageOn300Score() {
        Config.checkUninit();

        return Config.sendMessageOn300Score;
    }

    static final boolean isSendMessageForScoreAtBossEntry() {
        Config.checkUninit();

        return Config.sendMessageForScoreAtBossEntry;
    }

    static final boolean isSendMessageOnMelodyTerminal() {
        Config.checkUninit();

        return Config.sendMessageOnMelodyTerminal;
    }

    static final boolean isSendMessageOnRagAxe() {
        Config.checkUninit();

        return Config.sendMessageOnRagAxe;
    }

    static final boolean isDialogueSkipHelper() {
        Config.checkUninit();

        return Config.dialogueSkipHelper;
    }

    static final boolean isSbRejoinCooldownAfterKickDisplay() {
        Config.checkUninit();

        return Config.sbRejoinCooldownAfterKickDisplay;
    }

    static final boolean isFpsLimitDisplay() {
        Config.checkUninit();

        return Config.fpsLimitDisplay;
    }

    static final boolean isFpsDisplay() {
        Config.checkUninit();

        return Config.fpsDisplay;
    }

    static final boolean isSharperDragonBoundingBox() {
        Config.checkUninit();

        return Config.sharperDragonBoundingBox;
    }

    static final float getDragonBoundingBoxWidth() {
        Config.checkUninit();

        return Config.dragonBoundingBoxWidth;
    }

    static final boolean isMoreAccurateDragonBoundingBoxes() {
        Config.checkUninit();

        return Config.moreAccurateDragonBoundingBoxes;
    }

    static final boolean isArmorStandOptimizer() {
        Config.checkUninit();

        return Config.armorStandOptimizer;
    }

    static final int getArmorStandLimit() {
        Config.checkUninit();

        return Config.armorStandLimit;
    }

    static final boolean isDelayChunkUpdates() {
        Config.checkUninit();

        return Config.delayChunkUpdates;
    }

    static final boolean isHideParticles() {
        Config.checkUninit();

        return Config.hideParticles;
    }

    static final boolean isHideFallingBlocks() {
        Config.checkUninit();

        return Config.hideFallingBlocks;
    }

    static final boolean isHideSigns() {
        Config.checkUninit();

        return Config.hideSigns;
    }

    static final boolean isDisableYield() {
        Config.checkUninit();

        return Config.disableYield;
    }

    static final boolean isThreadPriorityTweaker() {
        Config.checkUninit();

        return Config.threadPriorityTweaker;
    }

    static final boolean isReduceBackgroundThreads() {
        Config.checkUninit();

        return Config.reduceBackgroundThreads;
    }

    static final int getMainMenuFrameLimit() {
        Config.checkUninit();

        return Config.mainMenuFrameLimit;
    }

    static final boolean isTweakJavaFlags() {
        Config.checkUninit();

        return Config.tweakJavaFlags;
    }

    static final boolean isOptimizeLatency() {
        Config.checkUninit();

        return Config.optimizeLatency;
    }

    static final boolean isOptimizeExceptions() {
        Config.checkUninit();

        return Config.optimizeExceptions;
    }

    static final boolean isSmoothenFrames() {
        Config.checkUninit();

        return Config.smoothenFrames;
    }

    static final boolean isRemoveArmorStandsOnWitherKingAndSadanFight() {
        Config.checkUninit();

        return Config.removeArmorStandsOnWitherKingAndSadanFight;
    }

    static final boolean isHideExperienceOrbs() {
        Config.checkUninit();

        return Config.hideExperienceOrbs;
    }

    /*static final int getHideWitherKing() {
        Config.checkUninit();

        return Config.hideWitherKing;
    }*/

    static final int getHideWitherSkeletonsOnMaxor() {
        Config.checkUninit();

        return Config.hideWitherSkeletonsOnMaxor;
    }

    static final boolean isPatchMemoryLeaks() {
        Config.checkUninit();

        return Config.patchMemoryLeaks;
    }

    static final boolean isRemoveBlankArmorStands() {
        Config.checkUninit();

        return Config.removeBlankArmorStands;
    }

    static final boolean isDisablePeriodicConfigSaves() {
        Config.checkUninit();

        return Config.disablePeriodicConfigSaves;
    }

    static final boolean isRenderSkytilsWaypointsOnlyWhenHoldingAOTV() {
        Config.checkUninit();

        return Config.renderSkytilsWaypointsOnlyWhenHoldingAOTV;
    }

    /*static final boolean isBlockPosOptimizer() {
        Config.checkUninit();

        return Config.blockPosOptimizer;
    }*/

    /*static final boolean isSkipRetexturingIrrelevantDrags() {
        Config.checkUninit();

        return Config.skipRetexturingIrrelevantDrags;
    }*/

    /*static final boolean isWitherKingFightFailNotification() {
        Config.checkUninit();

        return Config.witherKingFightFailNotification;
    }*/

    static final boolean isDragonHud() {
        Config.checkUninit();

        return Config.dragonHud;
    }

    static final int getDragonHudShadow() {
        Config.checkUninit();

        return Config.dragonHudShadow;
    }

    static final boolean isSpawningNotification() {
        Config.checkUninit();

        return Config.spawningNotification;
    }

    static final boolean isKillNotification() {
        Config.checkUninit();

        return Config.killNotification;
    }

    static final boolean isStatueDestroyedNotification() {
        Config.checkUninit();

        return Config.statueDestroyedNotification;
    }

    static final boolean isStatueMissedNotification() {
        Config.checkUninit();

        return Config.statueMissedNotification;
    }

    static final boolean isUltReminder() {
        Config.checkUninit();

        return Config.ultReminder;
    }

    static final boolean isSendMessageForWish() {
        Config.checkUninit();

        return Config.sendMessageForWish;
    }

    static final boolean isEdragReminder() {
        Config.checkUninit();

        return Config.edragReminder;
    }

    static final boolean isPhase5StartingNotification() {
        Config.checkUninit();

        return Config.phase5StartingNotification;
    }

    static final boolean isPhase3StartingNotification() {
        Config.checkUninit();

        return Config.phase3StartingNotification;
    }

    static final boolean isChromaToggle() {
        Config.checkUninit();

        return Config.chromaToggle;
    }

    static final boolean isChromaSkyblock() {
        Config.checkUninit();

        return Config.chromaSkyblock;
    }

    static final boolean isAlwaysSprint() {
        Config.checkUninit();

        return Config.alwaysSprint;
    }

    static final boolean isFullBright() {
        Config.checkUninit();

        return Config.fullBright;
    }

    static final boolean isAutoMelody() {
        Config.checkUninit();

        return Config.autoMelody;
    }

    static final boolean isAutoDance() {
        Config.checkUninit();

        return Config.autoDance;
    }

    static final boolean isAutoFishingRod() {
        Config.checkUninit();

        return Config.autoFishingRod;
    }

    static final int getAutoFishingRodStartingDelay() {
        Config.checkUninit();

        return Config.autoFishingRodStartingDelay;
    }

    static final int getAutoFishingRodMaximumDelay() {
        Config.checkUninit();

        return Config.autoFishingRodMaximumDelay;
    }

    static final boolean isAutoFishingRodSlugfishMode() {
        Config.checkUninit();

        return Config.autoFishingRodSlugfishMode;
    }

    static final boolean isAutoFishingRodGoldenFishMode() {
        Config.checkUninit();

        return Config.autoFishingRodGoldenFishMode;
    }

    static final boolean isAggressiveJump() {
        Config.checkUninit();

        return Config.aggressiveJump;
    }

    static final boolean isMiningPingFix() {
        Config.checkUninit();

        return Config.miningPingFix;
    }

    static final boolean isCreateGhostBlockWithKey() {
        Config.checkUninit();

        return Config.createGhostBlockWithKey;
    }

    static final boolean isKeepHoldingToCreateMoreGhostBlocks() {
        Config.checkUninit();

        return Config.keepHoldingToCreateMoreGhostBlocks;
    }

    static final boolean isAllowGhostBlockingBedrockAndBarrier() {
        Config.checkUninit();

        return Config.allowGhostBlockingBedrockAndBarrier;
    }

    static final boolean isPressKeyToRequeue() {
        Config.checkUninit();

        return Config.pressKeyToRequeue;
    }

    static final boolean isAutoExtraStats() {
        Config.checkUninit();

        return Config.autoExtraStats;
    }

    static final boolean isAutoCloseChests() {
        Config.checkUninit();

        return Config.autoCloseChests;
    }

    static final boolean isHackingForDummiesSolver() {
        Config.checkUninit();

        return Config.hackingForDummiesSolver;
    }

    static final boolean isLeftClickAutoClicker() {
        Config.checkUninit();

        return Config.leftClickAutoClicker;
    }

    static final boolean isRightClickAutoClicker() {
        Config.checkUninit();

        return Config.rightClickAutoClicker;
    }

    static final int getLeftClickCpsLimit() {
        Config.checkUninit();

        return Config.leftClickCpsLimit;
    }

    static final int getRightClickCpsLimit() {
        Config.checkUninit();

        return Config.rightClickCpsLimit;
    }

    /*static final boolean isCatchupAutoClicker() {
        Config.checkUninit();

        return Config.catchupAutoClicker;
    }*/

    static final boolean isAutoRegularAbility() {
        Config.checkUninit();

        return Config.autoRegularAbility;
    }

    static final boolean isAutoUltimateAbility() {
        Config.checkUninit();

        return Config.autoUltimateAbility;
    }

    /*static final boolean isModifyWindowTitle() {
        Config.checkUninit();

        return Config.modifyWindowTitle;
    }*/

    static final boolean isUnsafeMode() {
        Config.checkUninit();

        return Config.unsafeMode;
    }

    static final boolean isDebugMode() {
        Config.checkUninit();

        return Config.debugMode;
    }

    static final boolean isExtraLuck() {
        Config.checkUninit();

        return Config.extraLuck;
    }

    static final boolean isProfilerMode() {
        Config.checkUninit();

        return Config.profilerMode;
    }

    /*static final boolean isDimensionalSlashAlert() {
        Config.checkUninit();

        return Config.dimensionalSlashAlert;
    }*/

    /*static final boolean isShowDragonHP() {
        Config.checkUninit();

        return Config.showDragonHP;
    }*/

    static final boolean isShowStatueBox() {
        Config.checkUninit();

        return Config.showStatueBox;
    }

    static final boolean isHideStatueBoxForDestroyedStatues() {
        Config.checkUninit();

        return Config.hideStatueBoxForDestroyedStatues;
    }

    /*static final boolean isShowDragonSpawnTimer() {
        Config.checkUninit();

        return Config.showDragonSpawnTimer;
    }*/

    /*static final boolean isChangeHurtColorOnDragons() {
        Config.checkUninit();

        return Config.changeHurtColorOnDragons;
    }*/

    /*static final boolean isRetextureDragons() {
        Config.checkUninit();

        return Config.retextureDragons;
    }*/

    /*static final boolean isHideDragonDeath() {
        Config.checkUninit();

        return Config.hideDragonDeath;
    }*/

    static final boolean isCenturyRaffleTicketTimer() {
        Config.checkUninit();

        return Config.centuryRaffleTicketTimer;
    }

    static final boolean isSlayerRngDisplay() {
        Config.checkUninit();

        return Config.slayerRngDisplay;
    }

    static final boolean isBurgersDone() {
        Config.checkUninit();

        return Config.burgersDone;
    }

    static final boolean isAutoSalvation() {
        Config.checkUninit();

        return Config.autoSalvation;
    }

    static final int getAutoSalvationCpsLimit() {
        Config.checkUninit();

        return Config.autoSalvationCpsLimit;
    }

    static final boolean isDisableAutoSalvationInThornBoss() {
        Config.checkUninit();

        return Config.disableAutoSalvationInThornBoss;
    }

    private static final void checkUninit() {
        if (!Config.initialized) {
            DarkAddons.initConfig();
        }
    }

    final void init() {
        if (!Config.initialized) {
            Config.initialized = true;

            try {
                Files.createDirectories(new File(Config.CONFIG_FILE.getParent()).toPath());
            } catch (final IOException e) {
                DarkAddons.modError(e);
            }

            try {
                this.initialize();
            } catch (final IllegalArgumentException illegalArgumentException) {
                DarkAddons.queueWarning("Error when initializing the config; type of a config property likely changed. The config will be partly reset or else will be in a suboptimal state, please reconfigure your settings and report this error: " + illegalArgumentException.getClass().getName() + ": " + illegalArgumentException.getMessage());
                throw illegalArgumentException;
            }

            this.markDirty();
        }
    }

    private static final class ConfigSorting extends SortingBehavior {
        private ConfigSorting() {
            super();
        }

        @SuppressWarnings("TypeMayBeWeakened")
        private static final int compare(@NotNull final String name1, @NotNull final String name2) {
            if ("General".equals(name1)) {
                return -1;
            }

            if ("General".equals(name2)) {
                return 1;
            }

            return name1.equals(name2) ? 0 : name1.compareTo(name2);
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        @NotNull
        public final Comparator<? super Category> getCategoryComparator() {
            return (@NotNull final Category o1, @NotNull final Category o2) -> Config.ConfigSorting.compare(o1.getName(), o2.getName());
        }

        @SuppressWarnings("MethodDoesntCallSuperMethod")
        @Override
        @NotNull
        public final Comparator<? super Map.Entry<String, ? extends List<PropertyData>>> getSubcategoryComparator() {
            return (@NotNull final Map.Entry<String, ? extends List<PropertyData>> o1, @NotNull final Map.Entry<String, ? extends List<PropertyData>> o2) -> Config.ConfigSorting.compare(o1.getKey(), o2.getKey());
        }
    }
}
