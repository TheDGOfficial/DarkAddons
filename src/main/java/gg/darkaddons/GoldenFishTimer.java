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
        GoldenFishTimer.nextRodThrowThreshold = 0L;

        GoldenFishTimer.nextGoldenFish = 0L;
        GoldenFishTimer.goldenFishDespawnTime = 0L;

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
        if (Config.isGoldenFishTimer() && event.entity instanceof final EntityFishHook bobber && Minecraft.getMinecraft().thePlayer == bobber.angler && Items.fishing_rod == ItemUtils.getHeldItem(Minecraft.getMinecraft()) && DarkAddons.isPlayerInCrimsonIsle()) {
            final var now = System.currentTimeMillis();
            GoldenFishTimer.nextRodThrowThreshold = now + TimeUnit.MINUTES.toMillis(3L);
            if (0L == GoldenFishTimer.nextGoldenFish) {
                GoldenFishTimer.nextGoldenFish = now + TimeUnit.MINUTES.toMillis(8L);
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
                    GoldenFishTimer.nextGoldenFish = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(8L);
                    GoldenFishTimer.goldenFishDespawnTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1L);
                } else if ("The Golden Fish escapes your hook.".equals(unformattedMessage) || "The Golden Fish escapes your hook but looks weakened.".equals(unformattedMessage) || "The Golden Fish is weak!".equals(unformattedMessage)) {
                    GoldenFishTimer.goldenFishDespawnTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(1L);
                } else if (unformattedMessage.startsWith("TROPHY FISH! You caught a Golden Fish ")) {
                    GoldenFishTimer.goldenFishDespawnTime = 0L;
                } else if ("The Golden Fish swims back beneath the lava...".equals(unformattedMessage)) {
                    GoldenFishTimer.nextGoldenFish = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(8L);
                    GoldenFishTimer.goldenFishDespawnTime = 0L;
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public final void onWorldChange(@NotNull final WorldEvent.Unload event) {
        GoldenFishTimer.goldenFishDespawnTime = 0L;
        GoldenFishTimer.nextGoldenFish = 0L;
        GoldenFishTimer.nextRodThrowThreshold = 0L;
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {

        final var now = System.currentTimeMillis();

        var reset = false;

        if (0L == GoldenFishTimer.nextGoldenFish) {
            GoldenFishTimer.nextGoldenFish = now + TimeUnit.MINUTES.toMillis(8L);
            reset = true;
        }

        final var timeUntilNextGoldenFish = GoldenFishTimer.nextGoldenFish - now;
        final var guaranteed = timeUntilNextGoldenFish <= -TimeUnit.MINUTES.toMillis(4L);

        String goldenFishText;
        if (guaranteed) {
            goldenFishText = "§a§lReady (Guaranteed)";
        } else {
            goldenFishText = 0L >= timeUntilNextGoldenFish ? "§aReady" : Utils.formatTime(timeUntilNextGoldenFish, true);
        }

        if (0L == GoldenFishTimer.nextRodThrowThreshold) {
            GoldenFishTimer.nextRodThrowThreshold = now + TimeUnit.MINUTES.toMillis(3L);
        }

        final var timeUntilMissingGoldenFish = GoldenFishTimer.nextRodThrowThreshold - now;

        final String throwRodText;
        if (0L >= timeUntilMissingGoldenFish && !reset) {
            throwRodText = "§4You missed your chance to get Golden Fish!";

            GoldenFishTimer.nextGoldenFish = now + TimeUnit.MINUTES.toMillis(8L);
            goldenFishText = Utils.formatTime(GoldenFishTimer.nextGoldenFish - now, true);
        } else {
            throwRodText = "§cThrow rod before " + Utils.formatTime(timeUntilMissingGoldenFish, true);
        }

        if (reset) {
            goldenFishText = "Throw rod to start";
            GoldenFishTimer.nextGoldenFish = 0L;
        }

        final var despawnText = 0L == GoldenFishTimer.goldenFishDespawnTime ? "" : Utils.formatTime(GoldenFishTimer.goldenFishDespawnTime - now, true);

        if (despawnText.isEmpty()) {
            lines.add("§6Golden Fish Timer: " + goldenFishText);
            if ((timeUntilMissingGoldenFish <= TimeUnit.MINUTES.toMillis(1L) || guaranteed) && !reset) {
                lines.add(throwRodText);
            }
        } else {
            lines.add("§6Golden Fish Timer: §6§lSpawned!");
            lines.add("§cDespawns in " + despawnText);
        }
    }
}
