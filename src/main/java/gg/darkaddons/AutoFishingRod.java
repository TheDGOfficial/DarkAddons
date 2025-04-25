package gg.darkaddons;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.lang.ref.WeakReference;

import java.security.SecureRandom;

import net.minecraft.entity.item.EntityArmorStand;

import net.minecraft.init.Items;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import net.minecraftforge.event.world.WorldEvent;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;

import net.minecraft.entity.projectile.EntityFishHook;

import java.util.concurrent.TimeUnit;

final class AutoFishingRod {
    @NotNull
    private static final String READY = "§c§l!!!";

    @NotNull
    private static final Matcher countdownMatcher = Pattern.compile("§e§l(\\d+(\\.\\d+)?)").matcher("");

    @Nullable
    private static WeakReference<EntityArmorStand> countdownArmorStand;

    @NotNull
    private static final SecureRandom secureRandom = new SecureRandom();

    private static boolean hooking;

    private static long lastRodThrowTime;

    private static long lastMouseMoveTime;
    private static boolean lastMouseMoveWasAddition;

    AutoFishingRod() {
        super();
    }

    static final void registerTick() {
        DarkAddons.registerTickTask("auto_fishing_rod_tick", 1, true, AutoFishingRod::tick);
    }

    private static final boolean isCountdownArmorStand(@Nullable final String customNameTag) {
        return null != customNameTag && AutoFishingRod.countdownMatcher.reset(customNameTag).matches();
    }

    private static final boolean isCountdownArmorStand(@NotNull final EntityArmorStand armorStand) {
        return AutoFishingRod.isCountdownArmorStand(armorStand.getCustomNameTag());
    }

    private static final boolean isHoldingRod() {
        return Items.fishing_rod == Utils.getHeldItem(Minecraft.getMinecraft());
    }

    private static final boolean checkPreconditions() {
        return Config.isAutoFishingRod() && DarkAddons.isInSkyblock() && AutoFishingRod.isHoldingRod();
    }

    @Nullable
    private static final EntityArmorStand findAndAssignCountdownArmorStand() {
        if (AutoFishingRod.hasActiveBobber()) {
            for (final var entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                if (entity instanceof final EntityArmorStand armorStand && AutoFishingRod.isCountdownArmorStand(armorStand)) {
                    AutoFishingRod.countdownArmorStand = new WeakReference<>(armorStand);

                    return armorStand;
                }
            }
        }

        // Caller should handle this.
        return null;
    }

    @Nullable
    private static final EntityArmorStand getOrFindCountdownArmorStand() {
        if (null == AutoFishingRod.countdownArmorStand) {
            // May return null; caller should handle it.
            return AutoFishingRod.findAndAssignCountdownArmorStand();
        }

        final var cached = AutoFishingRod.countdownArmorStand.get();

        // Since it's a WeakReference, it can get cleared without us calling the clear method.
        // In this case, try to find and assign it again, otherwise will return null.
        return null == cached ? AutoFishingRod.findAndAssignCountdownArmorStand() : cached;
    }

    private static final void queueRightClick(@NotNull final Runnable continuation) {
        final var min = Config.getAutoFishingRodStartingDelay();
        final var max = Config.getAutoFishingRodMaximumDelay();

        final var delay = AutoFishingRod.secureRandom.nextInt(Math.max(1, max - min + 1)) + min;

        DarkAddons.registerTickTask("auto_fishing_rod_right_click", delay, false, () -> {
            AutoFishingRod.hook();
            continuation.run();
        });
    }

    private static final void tryExecMethods(@NotNull final Class<?> clazz, @NotNull final String[] names, @Nullable final Object instance, @Nullable final Object... args) {
        for (final var name : names) {
            try {
                final var method = clazz.getDeclaredMethod(name);

                method.setAccessible(true);
                method.invoke(instance, args);

                method.setAccessible(false);
                return;
            } catch (final NoSuchMethodException nsme) {
                /* ignored */
            } catch (final Throwable other) {
                DarkAddons.modError(other);
            }
        }
        throw new IllegalArgumentException("No method found with given names (" + String.join(", ", names) + ") in class " + clazz.getSimpleName());
    }

    private static final void hook() {
        AutoFishingRod.tryExecMethods(Minecraft.class, new String[]{"func_147121_ag", "ax", "rightClickMouse"}, Minecraft.getMinecraft());

        if (null != AutoFishingRod.countdownArmorStand) {
            AutoFishingRod.countdownArmorStand.clear();
        }
        AutoFishingRod.countdownArmorStand = null;
    }

    private static final boolean hasActiveBobber() {
        if (AutoFishingRod.isHoldingRod()) {
            for (final var entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
                if (entity instanceof final EntityFishHook bobber && Minecraft.getMinecraft().thePlayer == bobber.angler) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final void throwBobber() {
        AutoFishingRod.hooking = true;
        AutoFishingRod.queueRightClick(() -> AutoFishingRod.hooking = false);
    }

    private static final void throwBobberOnceNoBobber(final boolean initialBobber) {
        if (!initialBobber) {
            AutoFishingRod.throwBobber();
        } else {
            Utils.awaitCondition(() -> !AutoFishingRod.hasActiveBobber(), AutoFishingRod::throwBobber);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST, receiveCanceled = true)
    public final void onClientChatReceived(@NotNull final ClientChatReceivedEvent event) {
        if (Config.isAutoFishingRod() && MessageType.STANDARD_TEXT_MESSAGE.matches(event.type) && DarkAddons.isPlayerInCrimsonIsle() && AutoFishingRod.isHoldingRod()) {
            final var formattedMessage = event.message.getFormattedText();

            if (formattedMessage.contains("§")) {
                final var hasActiveBobber = AutoFishingRod.hasActiveBobber();
                final var unformattedMessage = Utils.removeControlCodes(event.message.getUnformattedText()).trim();
                if (!hasActiveBobber && "You spot a Golden Fish surface from beneath the lava!".equals(unformattedMessage)) {
                    AutoFishingRod.throwBobber();
                } else if (Config.isAutoFishingRodGoldenFishMode() && ("The Golden Fish escapes your hook.".equals(unformattedMessage) || "The Golden Fish escapes your hook but looks weakened.".equals(unformattedMessage))) {
                    AutoFishingRod.throwBobberOnceNoBobber(hasActiveBobber);
                } else if (Config.isAutoFishingRodGoldenFishMode() && "The Golden Fish is weak!".equals(unformattedMessage)) {
                    AutoFishingRod.throwBobber();
                } else if (!hasActiveBobber && "The Golden Fish swims back beneath the lava...".equals(unformattedMessage)) {
                    AutoFishingRod.throwBobber();
                } else if (Config.isAutoFishingRodGoldenFishMode() && unformattedMessage.startsWith("♔ TROPHY FISH! You caught a Golden Fish ")) {
                    AutoFishingRod.throwBobberOnceNoBobber(hasActiveBobber);
                }
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void onEntityJoinWorld(@NotNull final EntityJoinWorldEvent event) {
        final var player = Minecraft.getMinecraft().thePlayer;
        if (Config.isAutoFishingRod() && null != player && event.entity instanceof final EntityFishHook bobber && player == bobber.angler && AutoFishingRod.isHoldingRod()) {
            AutoFishingRod.lastRodThrowTime = System.currentTimeMillis();
            AutoFishingRod.moveMouse(player);

            if (null != AutoFishingRod.countdownArmorStand) {
                AutoFishingRod.countdownArmorStand.clear();
            }
            AutoFishingRod.countdownArmorStand = null;
        }
    }

    @SubscribeEvent(priority = EventPriority.NORMAL, receiveCanceled = true)
    public final void onWorldChange(@NotNull final WorldEvent.Unload event) {
        if (null != AutoFishingRod.countdownArmorStand) {
            AutoFishingRod.countdownArmorStand.clear();
        }
        AutoFishingRod.countdownArmorStand = null;

        AutoFishingRod.lastMouseMoveTime = 0L;
        AutoFishingRod.lastMouseMoveWasAddition = false;
    }

    private static float lastYaw = Float.NaN;
    private static float lastPitch = Float.NaN;

    private static final void moveMouse(@NotNull final EntityPlayerSP player) {
        final var now = System.currentTimeMillis();
 
        if (!Float.isNaN(AutoFishingRod.lastYaw) && !Float.isNaN(AutoFishingRod.lastPitch) && (player.rotationYaw != AutoFishingRod.lastYaw || player.rotationPitch != AutoFishingRod.lastPitch)) {
            // Head moved since the last time fishing rod bobber was thrown - restart the timer
            AutoFishingRod.lastMouseMoveTime = now;

            // Update values so that the if will be false if its not moved another time in the next check when the rod is thrown again
            AutoFishingRod.lastYaw = player.rotationYaw;
            AutoFishingRod.lastPitch = player.rotationPitch;

            return;
        }
 
        AutoFishingRod.lastYaw = player.rotationYaw;
        AutoFishingRod.lastPitch = player.rotationPitch;

        if (now - AutoFishingRod.lastMouseMoveTime >= TimeUnit.SECONDS.toMillis(14L)) { // Anti AFK seems to trigger about every 15 seconds
            AutoFishingRod.lastMouseMoveTime = now;

            if (AutoFishingRod.lastMouseMoveWasAddition) {
                SmoothLookHelper.setTarget(player.rotationYaw - 3.2F, player.rotationPitch - 1.2F);
                AutoFishingRod.lastMouseMoveWasAddition = false;
            } else {
                SmoothLookHelper.setTarget(player.rotationYaw + 3.2F, player.rotationPitch + 1.2F);
                AutoFishingRod.lastMouseMoveWasAddition = true;
            }
        }
    }

    private static final void tick() {
        if (AutoFishingRod.checkPreconditions() && !AutoFishingRod.hooking) {
            final var armorStand = AutoFishingRod.getOrFindCountdownArmorStand();
            if (null != armorStand) {
                final var customNameTag = armorStand.getCustomNameTag();
                final var ready = AutoFishingRod.READY.equals(customNameTag);
                if (ready && (!Config.isAutoFishingRodSlugfishMode() || 10_000L <= System.currentTimeMillis() - AutoFishingRod.lastRodThrowTime)) {
                    if (Config.isAutoFishingRodRecast()) {
                        // Right-clicks 2 times one after other with set delay, one for getting the catch and one for recasting the rod.
                        AutoFishingRod.hooking = true;
                        AutoFishingRod.queueRightClick(() -> AutoFishingRod.queueRightClick(() -> AutoFishingRod.hooking = false));
                    } else {
                        // Right-clicks 1 time with set delay to get the catch. Does not recast as per the configuration setting.
                        AutoFishingRod.throwBobber();
                    }
                } else {
                    if (Config.isAutoFishingRodSlugfishMode() && ready) {
                        if (null != AutoFishingRod.countdownArmorStand) {
                            AutoFishingRod.countdownArmorStand.clear();
                        }
                        AutoFishingRod.countdownArmorStand = null;
                    }
                }
            } else if (!Config.isAutoFishingRodSlugfishMode() && Config.isAutoFishingRodGoldenFishMode() && 10_000L <= System.currentTimeMillis() - AutoFishingRod.lastRodThrowTime && DarkAddons.isPlayerInCrimsonIsle() && AutoFishingRod.hasActiveBobber()) {
                AutoFishingRod.hooking = true;
                AutoFishingRod.lastRodThrowTime = System.currentTimeMillis();

                AutoFishingRod.queueRightClick(() -> AutoFishingRod.queueRightClick(() -> AutoFishingRod.hooking = false));
            }
        }
    }
}
