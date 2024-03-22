package gg.darkaddons;

import gg.essential.universal.UChat;
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonFeatures;
import gg.skytils.skytilsmod.features.impl.dungeons.DungeonTimer;
import gg.skytils.skytilsmod.mixins.extensions.ExtensionEntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class AdditionalM7Features {
    private static final int MAX_RETRY_TICKS_WAITING_FOR_DEATH_EVENT = 60;
    private static final int TITLE_TICKS = 60;

    private static boolean firstLaserNotDone = true;
    @Nullable
    private static WitherKingDragons lastKilledDragon;
    private static boolean witherKingDefeated;
    private static boolean firstGolemWoken;
    @SuppressWarnings("NegativelyNamedBooleanVariable")
    private static boolean notSaidFinalDialogue = true;
    private static boolean phase5NotStarted = true;
    private static boolean giantsFalling;
    private static boolean lividsSpawned;
    private static boolean playingTank;
    static boolean phase5Started;
    private static boolean canRemoveBlankArmorStands = true;

    AdditionalM7Features() {
        super();
    }

    static final boolean isGiantsFalling() {
        return AdditionalM7Features.giantsFalling;
    }

    static final boolean didLividsSpawn() {
        return AdditionalM7Features.lividsSpawned;
    }

    static final boolean isInM6OrF6Boss(final long bossEntryTime) {
        final var dungeonFloor = DungeonFeatures.INSTANCE.getDungeonFloorNumber();

        return null != dungeonFloor && 6 == dungeonFloor && -1L != bossEntryTime;
    }

    static final boolean isInM7OrF7() {
        final var dungeonFloor = DungeonFeatures.INSTANCE.getDungeonFloorNumber();

        return null != dungeonFloor && 7 == dungeonFloor;
    }

    static final boolean canHideArmorstands() {
        final var dungeonTimerInstance = DungeonTimer.INSTANCE;
        return AdditionalM7Features.canHideArmorstands(dungeonTimerInstance, dungeonTimerInstance.getBossEntryTime());
    }

    static final boolean canHideArmorstands(@NotNull final DungeonTimer dungeonTimerInstance, final long bossEntryTime) {
        return (AdditionalM7Features.phase5NotStarted && -1L == dungeonTimerInstance.getPhase3ClearTime() && -1L == dungeonTimerInstance.getPhase4ClearTime() || AdditionalM7Features.phase5Started) && AdditionalM7Features.notSaidFinalDialogue && -1L == dungeonTimerInstance.getBossClearTime() && (AdditionalM7Features.firstGolemWoken || -1L == bossEntryTime || !AdditionalM7Features.isInM6OrF6Boss(bossEntryTime)) && !DarkAddons.isPlayerInGarden();
    }

    static final boolean canRemoveBlankArmorStands() {
        return AdditionalM7Features.canRemoveBlankArmorStands;
    }

    static final boolean isInM7() {
        return "M7".equals(DungeonFeatures.INSTANCE.getDungeonFloor());
    }

    static final boolean isWitherKingDefeated() {
        return AdditionalM7Features.witherKingDefeated;
    }

    static final boolean isPlayingTank() {
        return AdditionalM7Features.playingTank;
    }

    private static final void onStatueDestroyed(@NotNull final WitherKingDragons dragon) {
        if (Config.isStatueDestroyedNotification()) {
            final var color = dragon.getChatColor();
            final var name = dragon.getEnumName();

            UChat.chat("§bThe " + color + "§l" + name + " §r§bdragon's statue has been destroyed! §a§lGood job!");
            GuiManager.createTitle("§a✔ Good Job!", color + "§l" + name + " §astatue destroyed!", AdditionalM7Features.TITLE_TICKS, AdditionalM7Features.TITLE_TICKS, true, GuiManager.Sound.LEVEL_UP);
        }
    }

    private static final void onStatueMissed(@NotNull final WitherKingDragons dragon) {
        if (Config.isStatueMissedNotification()) {
            final var color = dragon.getChatColor();
            final var name = dragon.getEnumName();

            UChat.chat("§cThe " + color + "§l" + name + " §r§cdragon's statue has been §4missed! §cYou need to kill it again!");
            GuiManager.createTitle("§c✖ Missed!", color + "§l" + name + "§r§ckilled out of statue!", AdditionalM7Features.TITLE_TICKS, AdditionalM7Features.TITLE_TICKS, true, GuiManager.Sound.ANVIL_LAND);
        }
    }

    private static final void destroyAllStatues() {
        AdditionalM7Features.lastKilledDragon = null;
        AdditionalM7Features.witherKingDefeated = true;

        for (@NotNull final WitherKingDragons dragon : WitherKingDragons.getValues()) {
            if (!dragon.isDestroyed()) {
                dragon.setDestroyed(true);

                AdditionalM7Features.onStatueDestroyed(dragon);
            }
        }
    }

    private static final void processStatueMessage(final boolean destroy) {
        AdditionalM7Features.processStatueMessage(destroy, 1);
    }

    private static final void processStatueMessage(final boolean destroy, final int ticks) {
        if (!Config.isStatueDestroyedNotification() && !Config.isStatueMissedNotification() && (!Config.isShowStatueBox() || !Config.isHideStatueBoxForDestroyedStatues())) {
            return;
        }

        final var lastDragon = AdditionalM7Features.lastKilledDragon;

        if (null == lastDragon) {
            // TODO sometimes causes weird bugs, i.e., shows for the wrong dragon, or shows killed when its actually missed, or doesn't show at all, etc.
            if (AdditionalM7Features.MAX_RETRY_TICKS_WAITING_FOR_DEATH_EVENT > ticks) {
                DarkAddons.runOnceInNextTick("process_statue_message_recheck", () -> AdditionalM7Features.processStatueMessage(destroy, ticks + 1));
                if (Config.isDebugMode() && 1 == ticks) {
                    DarkAddons.debug(() -> "Scheduled re-check of determining last killed dragon to show statue " + (destroy ? "destroyed" : "missed") + " notification to the next tick. Will keep rescheduling till we find it or give up after " + AdditionalM7Features.MAX_RETRY_TICKS_WAITING_FOR_DEATH_EVENT + " ticks.");
                }
            } else if (Config.isDebugMode()) {
                DarkAddons.debug(() -> "Gave up trying to find last killed dragon to show statue " + (destroy ? "destroyed" : "missed") + " notification");
            }
        } else {
            if (destroy) {
                if (lastDragon.isDestroyed()) {
                    if (Config.isDebugMode()) {
                        DarkAddons.debug(() -> "Skipping statue destroyed notification for " + lastDragon.getEnumName() + " because it's statue is already destroyed");
                    }
                } else {
                    lastDragon.setDestroyed(true);

                    AdditionalM7Features.onStatueDestroyed(lastDragon);
                }
            } else {
                if (lastDragon.isDestroyed()) {
                    if (Config.isDebugMode()) {
                        DarkAddons.debug(() -> "Skipping statue missed notification for " + lastDragon.getEnumName() + " because it's statue is already destroyed");
                    }
                } else {
                    AdditionalM7Features.onStatueMissed(lastDragon);
                }
            }
            AdditionalM7Features.lastKilledDragon = null;
        }
    }

    private static final void handleDeath(@NotNull final LivingDeathEvent event) {
        if (!Config.isStatueDestroyedNotification() && !Config.isStatueMissedNotification() && (!Config.isShowStatueBox() || !Config.isHideStatueBoxForDestroyedStatues())) {
            return;
        }

        final var entity = event.entityLiving;
        if (entity instanceof EntityDragon) {
            final var type = WitherKingDragons.from(((ExtensionEntityLivingBase) entity).getSkytilsHook().getMasterDragonType());
            if (null == type) {
                if (Config.isDebugMode()) {
                    DarkAddons.debug(() -> "Can't find last killed dragon type for entity with name " + entity.getName() + "§r§e at x=" + entity.posX + ",y=" + entity.posY + ",z=" + entity.posZ);
                }
            } else {
                AdditionalM7Features.lastKilledDragon = type;
                if (Config.isDebugMode()) {
                    DarkAddons.debug(() -> "Set last killed dragon type to " + type.getEnumName());
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onDeath(@NotNull final LivingDeathEvent event) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        if (DarkAddons.shouldProfile()) {
            DarkAddons.handleEvent("additionalm7features_handle_death", event, AdditionalM7Features::handleDeath);
        } else {
            AdditionalM7Features.handleDeath(event);
        }
    }

    private static final void handleLoad() {
        AdditionalM7Features.firstLaserNotDone = true;
        AdditionalM7Features.witherKingDefeated = false;
        AdditionalM7Features.firstGolemWoken = false;
        AdditionalM7Features.notSaidFinalDialogue = true;
        AdditionalM7Features.phase5NotStarted = true;
        AdditionalM7Features.lividsSpawned = false;
        AdditionalM7Features.giantsFalling = false;
        AdditionalM7Features.playingTank = false;
        if (Config.isDebugMode() && null != AdditionalM7Features.lastKilledDragon) {
            DarkAddons.debug(() -> "Reset data since dimension changed.");
        }
        AdditionalM7Features.lastKilledDragon = null;
        AdditionalM7Features.phase5Started = false;
        AdditionalM7Features.canRemoveBlankArmorStands = true;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public final void onWorldLoad(@NotNull final WorldEvent.Unload event) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        if (DarkAddons.shouldProfile()) {
            DarkAddons.handleEvent("additionalm7features_handle_worldload", AdditionalM7Features::handleLoad);
        } else {
            AdditionalM7Features.handleLoad();
        }
    }

    static final void handleMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("additionalm7features_handle_message");

        if (MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            AdditionalM7Features.handleMessage2(Utils.removeControlCodes(event.message.getFormattedText()));
        }

        McProfilerHelper.endSection();
    }

    private static final void handleMessage2(@NotNull final String message) {
        switch (message) {
            case "[BOSS] Maxor: THAT BEAM! IT HURTS! IT HURTS!!", "[BOSS] Maxor: YOU TRICKED ME!" -> {
                if (Config.isUltReminder() && AdditionalM7Features.firstLaserNotDone) {
                    DarkAddons.sendMessage(Utils.chromaIfEnabledOrAqua() + "Maxor is enraged. Use your ultimate ability!");
                    GuiManager.createTitle("§bUse ult!", AdditionalM7Features.TITLE_TICKS, true, GuiManager.Sound.PLING);
                    AdditionalM7Features.firstLaserNotDone = false;
                }
            }
            case "[BOSS] Storm: At least my son died by your hands." -> {
                if (Config.isPhase3StartingNotification()) {
                    final var msg = Utils.chromaIfEnabledOrAqua() + "Phase 3 starting";

                    DarkAddons.sendMessage(msg);
                    GuiManager.createTitle(msg, AdditionalM7Features.TITLE_TICKS, true, GuiManager.Sound.PLING);
                }
            }
            case "[BOSS] Goldor: You have done it, you destroyed the factory…" -> {
                AdditionalM7Features.phase5NotStarted = false; // Have to do it here instead of Necron final dialogue because that's too late
                if (Config.isUltReminder()) {
                    DarkAddons.sendMessage(Utils.chromaIfEnabledOrAqua() + "Goldor fight starting. Use your ultimate ability!");
                    GuiManager.createTitle("§bUse ult!", AdditionalM7Features.TITLE_TICKS, true, GuiManager.Sound.PLING);
                }
            }
            default -> AdditionalM7Features.handleMessage3(message);
        }
    }

    private static final void handleMessage3(@NotNull final String message) {
        switch (message) {
            case "[BOSS] Necron: All this, for nothing..." -> {
                if (Config.isEdragReminder() && AdditionalM7Features.isInM7()) {
                    DarkAddons.sendMessage(Utils.chromaIfEnabledOrAqua() + "Phase 4 done. Equip your Ender Dragon!");
                    GuiManager.createTitle("§bSwap to edrag!", AdditionalM7Features.TITLE_TICKS, true, GuiManager.Sound.PLING);
                }
            }
            case "[BOSS] Wither King: You.. again?", "[BOSS] Wither King: Ohhh?" -> {
                AdditionalM7Features.phase5Started = true;
                if (Config.isPhase5StartingNotification()) {
                    final var msg = Utils.chromaIfEnabledOrAqua() + "Phase 5 starting";

                    DarkAddons.sendMessage(msg);
                    GuiManager.createTitle(msg, AdditionalM7Features.TITLE_TICKS, true, GuiManager.Sound.PLING);
                }
            }
            case "[BOSS] Wither King: Oh, this one hurts!", "[BOSS] Wither King: I have more of those.", "[BOSS] Wither King: My soul is disposable." ->
                AdditionalM7Features.processStatueMessage(true);
            default -> AdditionalM7Features.handleMessage4(message);
        }
    }

    private static final void handleMessage4(@NotNull final String message) {
        switch (message) {
            case "[BOSS] Wither King: Futile.", "[BOSS] Wither King: You just made a terrible mistake!", "[BOSS] Wither King: I am not impressed.", "[BOSS] Wither King: Your skills have faded humans." ->
                AdditionalM7Features.processStatueMessage(false);
            case "[BOSS] Wither King: Incredible. You did what I couldn't do myself.", "[BOSS] Wither King: Thank you for coming all the way here." -> {
                AdditionalM7Features.notSaidFinalDialogue = false;
                AdditionalM7Features.destroyAllStatues();
            }
            case "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing." ->
                AdditionalM7Features.lividsSpawned = true;
            default -> AdditionalM7Features.handleMessage5(message);
        }
    }

    private static final void handleMessage5(@NotNull final String message) {
        switch (message) {
            case "[BOSS] Sadan: My giants! Unleashed!" -> AdditionalM7Features.giantsFalling = true;
            case "[BOSS] Sadan: I'm sorry, but I need to concentrate. I wish it didn't have to come to this." ->
                AdditionalM7Features.notSaidFinalDialogue = false;
            case "Your Tank stats are doubled because you are the only player using this class!" ->
                AdditionalM7Features.playingTank = true;
            case "[STATUE] Oruo the Omniscient: I am Oruo the Omniscient. I have lived many lives. I have learned all there is to know." ->
                AdditionalM7Features.canRemoveBlankArmorStands = false;
            case "[STATUE] Oruo the Omniscient: I bestow upon you all the power of a hundred years!" ->
                AdditionalM7Features.canRemoveBlankArmorStands = true;
            default -> {
                if (message.startsWith("[BOSS] Sadan: Interesting strategy, waking up my Golems. Or was that unintentional, ") || message.startsWith("[BOSS] Sadan: You weren't supposed to wake up that Golem, ") || message.startsWith("[BOSS] Sadan: My Terracotta Army wasn't enough? You had to awaken a Golem on top, ") || message.startsWith("[BOSS] Sadan: Those Golems will be your undoing, ")) {
                    AdditionalM7Features.firstGolemWoken = true;
                }
            }
        }
    }
}
