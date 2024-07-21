package gg.darkaddons;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import java.util.concurrent.TimeUnit;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import net.minecraftforge.event.world.WorldEvent;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import net.minecraft.entity.projectile.EntityFishHook;

import net.minecraft.init.Items;

import net.minecraft.client.Minecraft;

final class GoldenFishTimer extends SimpleGuiElement {
    private static long nextRodThrowThreshold;

    private static long nextGoldenFish;
    private static long goldenFishDespawnTime;

    GoldenFishTimer() {
        super("Golden Fish Timer", Config::isGoldenFishTimer, DarkAddons::isPlayerInCrimsonIsle, () -> 0);

        DarkAddons.registerTickTask("golden_fish_timer_update_golden_fish_timer", 20, true, this::update);
    }

    @Override
    final void clear() {
        GoldenFishTimer.nextRodThrowThreshold = 0;

        GoldenFishTimer.nextGoldenFish = 0;
        GoldenFishTimer.goldenFishDespawnTime = 0;

        super.clear();
    }

    @Override
    final void update() {
        if (!this.isEnabled()) {
            return;
        }

        super.update();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void onEntityJoinWorld(@NotNull final EntityJoinWorldEvent event) {
        if (Config.isGoldenFishTimer() && event.entity instanceof EntityFishHook bobber && Minecraft.getMinecraft().thePlayer == bobber.angler && Items.fishing_rod == Utils.getHeldItem(Minecraft.getMinecraft()) && DarkAddons.isPlayerInCrimsonIsle()) {
            final var now = System.currentTimeMillis();
            nextRodThrowThreshold = now + TimeUnit.MINUTES.toMillis(3);
            if (0 == nextGoldenFish) {
                nextGoldenFish = now + TimeUnit.MINUTES.toMillis(15);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onClientChatReceived(@NotNull final ClientChatReceivedEvent event) {
        if (Config.isGoldenFishTimer() && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type) && DarkAddons.isPlayerInCrimsonIsle()) {
            final var formattedMessage = event.message.getFormattedText();
            final var unformattedMessage = Utils.removeControlCodes(event.message.getUnformattedText()).trim();

            if (formattedMessage.contains("§")) {
                if ("You spot a Golden Fish surface from beneath the lava!".equals(unformattedMessage)) {
                    nextGoldenFish = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15);
                    goldenFishDespawnTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1);
                } else if ("The Golden Fish escapes your hook.".equals(unformattedMessage) || "The Golden Fish escapes your hook but looks weakened.".equals(unformattedMessage) || "The Golden Fish is weak!".equals(unformattedMessage)) {
                    goldenFishDespawnTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1);
                } else if (unformattedMessage.startsWith("TROPHY FISH! You caught a Golden Fish ")) {
                    goldenFishDespawnTime = 0;
                } else if ("The Golden Fish swims back beneath the lava...".equals(unformattedMessage)) {
                    nextGoldenFish = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(15);
                    goldenFishDespawnTime = 0;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public final void onWorldChange(@NotNull final WorldEvent.Unload event) {
        goldenFishDespawnTime = 0;
        nextGoldenFish = 0;
        nextRodThrowThreshold = 0;
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        String goldenFishText;
 
        final var now = System.currentTimeMillis();

        boolean reset = false;

        if (0 == nextGoldenFish) {
            nextGoldenFish = now + TimeUnit.MINUTES.toMillis(15);
            reset = true;
        }

        final var timeUntilNextGoldenFish = nextGoldenFish - now;
        final var guaranteed = timeUntilNextGoldenFish <= -TimeUnit.MINUTES.toMillis(5);

        if (guaranteed) {
            goldenFishText = "§a§lReady (Guaranteed)";
        } else if (timeUntilNextGoldenFish <= 0) {
            goldenFishText = "§aReady";
        } else {
            goldenFishText = Utils.formatTime(timeUntilNextGoldenFish, true);
        }

        final String throwRodText;

        if (0 == nextRodThrowThreshold) {
            nextRodThrowThreshold = now + TimeUnit.MINUTES.toMillis(3);
        }

        final var timeUntilMissingGoldenFish = nextRodThrowThreshold - now;

        if (timeUntilMissingGoldenFish <= 0 && !reset) {
            throwRodText = "§4You missed your chance to get Golden Fish!";

            nextGoldenFish = now + TimeUnit.MINUTES.toMillis(15);
            goldenFishText = Utils.formatTime(nextGoldenFish - now, true);
        } else {
            throwRodText = "§cThrow rod before " + Utils.formatTime(timeUntilMissingGoldenFish, true);
        }

        if (reset) {
            goldenFishText = "Throw rod to start";
            nextGoldenFish = 0;
        }

        final String despawnText = 0 == goldenFishDespawnTime ? "" : Utils.formatTime(goldenFishDespawnTime - now, true);

        if (!despawnText.isEmpty()) {
            lines.add("§6Golden Fish Timer: §6§lSpawned!");
            lines.add("§cDespawns in " + despawnText);
        } else {
            lines.add("§6Golden Fish Timer: " + goldenFishText);
            if ((timeUntilMissingGoldenFish <= TimeUnit.MINUTES.toMillis(1) || guaranteed) && !reset) {
                lines.add(throwRodText);
            }
        }
    }
}
