package gg.darkaddons;

import gg.essential.universal.UChat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;

import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.HashMap;
import java.util.function.BooleanSupplier;

final class AdditionalM7Features {
    private static final int TITLE_TICKS = 60;

    private static boolean firstLaserNotDone = true;
    @Nullable
    static WitherKingDragons lastKilledDragon;
    private static boolean witherKingDefeated;
    private static boolean firstGolemWoken;
    @SuppressWarnings("NegativelyNamedBooleanVariable")
    private static boolean notSaidFinalDialogue = true;
    private static boolean phase5NotStarted = true;
    private static boolean giantsFalling;
    private static boolean lividsSpawned;
    private static boolean playingTank;
    static boolean phase5Started;

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
        final var dungeonFloor = DungeonFeatures.getDungeonFloorNumber();

        return null != dungeonFloor && 6 == dungeonFloor && -1L != bossEntryTime;
    }

    static final boolean isAtPhase1() {
        return -1L == DungeonTimer.getPhase1ClearTime() && -1L != DungeonTimer.getBossEntryTime() && AdditionalM7Features.isInM7OrF7();
    }

    static final boolean isInM7OrF7() {
        final var dungeonFloor = DungeonFeatures.getDungeonFloorNumber();

        return null != dungeonFloor && 7 == dungeonFloor;
    }

    static final boolean canHideArmorstands() {
        return AdditionalM7Features.canHideArmorstands(DungeonTimer.getBossEntryTime());
    }

    static final boolean canHideArmorstands(final long bossEntryTime) {
        return (AdditionalM7Features.phase5NotStarted && -1L == DungeonTimer.getPhase3ClearTime() && -1L == DungeonTimer.getPhase4ClearTime() || AdditionalM7Features.phase5Started) && AdditionalM7Features.notSaidFinalDialogue && -1L == DungeonTimer.getBossClearTime() && (AdditionalM7Features.firstGolemWoken || -1L == bossEntryTime || !AdditionalM7Features.isInM6OrF6Boss(bossEntryTime));
    }

    static final boolean isInM7() {
        return "M7".equals(DungeonFeatures.getDungeonFloor());
    }

    static final boolean isWitherKingDefeated() {
        return AdditionalM7Features.witherKingDefeated;
    }

    static final boolean isPlayingTank() {
        return AdditionalM7Features.playingTank;
    }

    private static final String getExtraInfo(@NotNull final WitherKingDragons dragon) {
        final var sprayed = dragon.isIceSprayed();
        final var ticks = dragon.getIceSprayedInTicks();

        dragon.setIceSprayed(false);
        dragon.setIceSprayedInTicks(-1);

        final var classes = new HashMap<String, DungeonListener.DungeonClass>(Utils.calculateHashMapCapacity(5));

        for (final var teammate : DungeonListener.getTeam()) {
            classes.put(teammate.getPlayer().getName(), teammate.getDungeonClass());
        }

        final int[] lbHitsGuess = {0};
        final int[] dpsArrows = {0};

        dragon.getArrowsHit().forEach((player, amount) -> {
            final var dungeonClass = classes.get(player);

            if (DungeonListener.DungeonClass.TANK == dungeonClass || DungeonListener.DungeonClass.HEALER == dungeonClass || DungeonListener.DungeonClass.MAGE == dungeonClass) {
                lbHitsGuess[0] += amount;
            } else if (DungeonListener.DungeonClass.ARCHER == dungeonClass || DungeonListener.DungeonClass.BERSERK == dungeonClass) {
                dpsArrows[0] += amount;
            }
        });

        final var lb = lbHitsGuess[0];
        final var color = lb >= 5 ? "§a" : lb == 4 ? "§2" : lb == 3 ? "§6" : lb == 2 ? "§e" : lb == 1 ? "§c" : "§4";

        final var dps = dpsArrows[0];
        final var dpsColor = dps >= 100 ? "§a" : dps >= 60 ? "§2" : dps >= 50 ? "§6" : dps >= 40 ? "§c" : "§4";

        return " §b(Spray: " + (sprayed ? "§aYes [" + ticks + "t]" : "§cNo") + "§b, LB: " + color + Integer.toString(lb) + "§b, A+B: " + dpsColor + Integer.toString(dps) + ')';
    }

    private static final void echoArrowsHit(@NotNull final WitherKingDragons dragon) {
        final var builder = new StringBuilder();
        final boolean[] first = {false};

        dragon.getArrowsHit().forEach((player, amount) -> {
            if (!first[0]) {
                first[0] = true;
            } else {
                builder.append(", ");
            }

            builder.append(player + " " + amount);
        });

        final var arrowsHit = builder.toString();

        UChat.chat("§bArrows hit to " + dragon.getTextColor().toUpperCase(Locale.ROOT) + ": " + (arrowsHit.isEmpty() ? "None" : arrowsHit));
    }

    private static final void onStatueDestroyed(@NotNull final WitherKingDragons dragon) {
        if (Config.isStatueDestroyedNotification()) {
            final var color = dragon.getChatColor();
            final var name = dragon.getEnumName();
            final var extra = AdditionalM7Features.getExtraInfo(dragon);

            UChat.chat("§bThe " + color + "§l" + name + " §r§bdragon's statue has been destroyed! §a§lGood job!" + extra);
            GuiManager.createTitle("§a✔ Good Job!", color + "§l" + name + " §astatue destroyed!" + extra, AdditionalM7Features.TITLE_TICKS, AdditionalM7Features.TITLE_TICKS, true, GuiManager.Sound.LEVEL_UP);

            AdditionalM7Features.echoArrowsHit(dragon);
        }
    }

    private static final void onStatueMissed(@NotNull final WitherKingDragons dragon) {
        if (Config.isStatueMissedNotification()) {
            final var color = dragon.getChatColor();
            final var name = dragon.getEnumName();
            final var extra = AdditionalM7Features.getExtraInfo(dragon);

            UChat.chat("§cThe " + color + "§l" + name + " §r§cdragon's statue has been §4missed! §cYou need to kill it again!" + extra);
            GuiManager.createTitle("§c✖ Missed!", color + "§l" + name + " §r§ckilled out of statue!" + extra, AdditionalM7Features.TITLE_TICKS, AdditionalM7Features.TITLE_TICKS, true, GuiManager.Sound.ANVIL_LAND);

            AdditionalM7Features.echoArrowsHit(dragon);
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
        if (!Config.isStatueDestroyedNotification() && !Config.isStatueMissedNotification() && (!Config.isShowStatueBox() || !Config.isHideStatueBoxForDestroyedStatues())) {
            return;
        }

        final var lastDragon = AdditionalM7Features.lastKilledDragon;

        if (null != lastDragon) {
            if (destroy) {
                if (!lastDragon.isDestroyed()) {
                    lastDragon.setDestroyed(true);

                    AdditionalM7Features.onStatueDestroyed(lastDragon);
                }
            } else if (!lastDragon.isDestroyed()) {
                AdditionalM7Features.onStatueMissed(lastDragon);
            }

            AdditionalM7Features.lastKilledDragon = null;
        }
    }

    private static final void handleWorldUnload() {
        AdditionalM7Features.firstLaserNotDone = true;
        AdditionalM7Features.witherKingDefeated = false;
        AdditionalM7Features.firstGolemWoken = false;
        AdditionalM7Features.notSaidFinalDialogue = true;
        AdditionalM7Features.phase5NotStarted = true;
        AdditionalM7Features.lividsSpawned = false;
        AdditionalM7Features.giantsFalling = false;
        AdditionalM7Features.playingTank = false;
        AdditionalM7Features.lastKilledDragon = null;
        AdditionalM7Features.phase5Started = false;
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public final void onWorldUnload(@NotNull final WorldEvent.Unload event) {
        if (DarkAddons.checkClientEvent()) {
            return;
        }

        if (DarkAddons.shouldProfile()) {
            DarkAddons.handleEvent("additionalm7features_handle_worldload", AdditionalM7Features::handleWorldUnload);
        } else {
            AdditionalM7Features.handleWorldUnload();
        }
    }

    static final void handleMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("additionalm7features_handle_message");

        if (MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            AdditionalM7Features.handleMessage2(Utils.removeControlCodes(event.message.getFormattedText()));
        }

        McProfilerHelper.endSection();
    }

    private static final void sendUseUltTitle() {
        GuiManager.createTitle("§bUse ult!", AdditionalM7Features.TITLE_TICKS, true, GuiManager.Sound.PLING);
    }

    private static final void handleMessage2(@NotNull final String message) {
        switch (message) {
            case "⚠ Maxor is enraged! ⚠" -> {
                AutoClassAbilities.ultReminderToAutoClassAbilitiesHook();
                if (AdditionalM7Features.firstLaserNotDone) {
                    var flag = false;
                    if (Config.isUltReminder()) {
                        DarkAddons.sendMessage(Utils.chromaIfEnabledOrAqua() + "Maxor is enraged. Use your ultimate ability!");
                        AdditionalM7Features.sendUseUltTitle();
                        flag = true;
                    }
                    if (Config.isSendMessageForWishAndCastleOfStone()) {
                        DarkAddons.queueUserSentMessageOrCommand("/pc Wish and castle of stone!");
                        flag = true;
                    }
                    if (flag) {
                        AdditionalM7Features.firstLaserNotDone = false;
                    }
                }
            }
            case "[BOSS] Goldor: You have done it, you destroyed the factory…" -> {
                AdditionalM7Features.phase5NotStarted = false; // Have to do it here instead of Necron final dialogue because that's too late
                AutoClassAbilities.ultReminderToAutoClassAbilitiesHook();
                if (Config.isUltReminder()) {
                    DarkAddons.sendMessage(Utils.chromaIfEnabledOrAqua() + "Goldor fight starting. Use your ultimate ability!");
                    AdditionalM7Features.sendUseUltTitle();
                }
                if (Config.isSendMessageForWishAndCastleOfStone()) {
                    DarkAddons.queueUserSentMessageOrCommand("/pc Wish!");
                }
            }
            default -> AdditionalM7Features.handleMessage3(message);
        }
    }

    private static final void handleMessage3(@NotNull final String message) {
        switch (message) {
            case "[BOSS] Wither King: You... again?", "[BOSS] Wither King: Ohhh?" -> {
                AdditionalM7Features.phase5Started = true;
                if (Config.isPhase5StartingNotification()) {
                    final var msg = Utils.chromaIfEnabledOrAqua() + "Phase 5 starting";

                    DarkAddons.sendMessage(msg);
                    GuiManager.createTitle(msg, AdditionalM7Features.TITLE_TICKS, true, GuiManager.Sound.PLING);
                }
            }
            case "[BOSS] Wither King: Oh, this one hurts!", "[BOSS] Wither King: I have more of those.",
                 "[BOSS] Wither King: My soul is disposable." -> AdditionalM7Features.processStatueMessage(true);
            default -> AdditionalM7Features.handleMessage4(message);
        }
    }

    private static final void notifyToStartCastingRagnarockIfNecessary() {
         if (Config.isRagnarockUseNotifier()) {
             GuiManager.createTitle("§5Cast Ragnarock!", 60, true, GuiManager.Sound.PLING);
         }
    }

    private static final void handleMessage4(@NotNull final String message) {
        switch (message) {
            case "[BOSS] Wither King: Futile.", "[BOSS] Wither King: You just made a terrible mistake!",
                 "[BOSS] Wither King: I am not impressed.", "[BOSS] Wither King: Your skills have faded humans." ->
                AdditionalM7Features.processStatueMessage(false);
            case "[BOSS] Wither King: Incredible. You did what I couldn't do myself.",
                 "[BOSS] Wither King: Thank you for coming all the way here." -> {
                AdditionalM7Features.notSaidFinalDialogue = false;
                AdditionalM7Features.destroyAllStatues();
            }
            case "[BOSS] Wither King: I no longer wish to fight, but I know that will not stop you." ->
                AdditionalM7Features.notifyToStartCastingRagnarockIfNecessary();
            case "[BOSS] Livid: I can now turn those Spirits into shadows of myself, identical to their creator." ->
                AdditionalM7Features.notifyToStartCastingRagnarockIfNecessary();
            case "[BOSS] Livid: I respect you for making it to here, but I'll be your undoing." -> {
                AdditionalM7Features.lividsSpawned = true;
                AutoClassAbilities.ultReminderToAutoClassAbilitiesHook();
            }
            default -> AdditionalM7Features.handleMessage5(message);
        }
    }

    private static final void handleMessage5(@NotNull final String message) {
        switch (message) {
            case "[BOSS] Sadan: My giants! Unleashed!" -> {
                AdditionalM7Features.giantsFalling = true;
                AutoClassAbilities.ultReminderToAutoClassAbilitiesHook();
            }
            case "[BOSS] Sadan: I'm sorry, but I need to concentrate. I wish it didn't have to come to this." ->
                AdditionalM7Features.notSaidFinalDialogue = false;
            case "Your Tank stats are doubled because you are the only player using this class!" ->
                AdditionalM7Features.playingTank = true;
            default -> {
                if (message.startsWith("[BOSS] Sadan: Interesting strategy, waking up my Golems. Or was that unintentional, ") || message.startsWith("[BOSS] Sadan: You weren't supposed to wake up that Golem, ") || message.startsWith("[BOSS] Sadan: My Terracotta Army wasn't enough? You had to awaken a Golem on top, ") || message.startsWith("[BOSS] Sadan: Those Golems will be your undoing, ")) {
                    AdditionalM7Features.firstGolemWoken = true;
                }
            }
        }
    }
}
