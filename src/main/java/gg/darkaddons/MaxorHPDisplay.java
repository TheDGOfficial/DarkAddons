package gg.darkaddons;

import net.minecraft.client.Minecraft;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;

import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityArmorStand;

import java.lang.ref.WeakReference;

final class MaxorHPDisplay extends SimpleGuiElement {
    // Safety: Uses WeakReference to not create a memory leak of the entity.
    private static @Nullable WeakReference<EntityWither> maxor;
    private static @Nullable WeakReference<EntityArmorStand> maxorNametag;

    @Nullable
    private static String maxorName;
    @Nullable
    private static String lastMaxorName;

    private static double maxorHp;
    private static double lastMaxorHp;

    private static boolean maxorDead;

    private static boolean saidEnrageSkipHelperMessage;

    MaxorHPDisplay() {
        super("Maxor HP Display", Config::isMaxorHPDisplay, () -> true, () -> 0);

        DarkAddons.registerTickTask("maxor_hp_display_update_maxor_hp", 1, true, this::update);
    }

    @Nullable
    private static final EntityWither findAndAssignMaxor() {
        for (final var entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (entity instanceof final EntityWither wither && wither.getName().contains("Maxor")) {
                MaxorHPDisplay.maxor = new WeakReference<>(wither);

                return wither;
            }
        }

        // Caller should handle this.
        return null;
    }

    @Nullable
    private static final EntityWither getOrFindMaxor() {
        if (null == MaxorHPDisplay.maxor) {
            // May return null; caller should handle it.
            return MaxorHPDisplay.findAndAssignMaxor();
        }

        final var cached = MaxorHPDisplay.maxor.get();

        // Since it's a WeakReference, it can get cleared without us calling the clear method. In this case, try to find and assign it again, otherwise will return null. Return existing entity if cached is not null.
        return null == cached ? MaxorHPDisplay.findAndAssignMaxor() : cached;
    }

    @Nullable
    private static final EntityArmorStand findAndAssignMaxorNametag() {
        for (final var entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (entity instanceof final EntityArmorStand stand && stand.getName().contains("Maxor")) {
                MaxorHPDisplay.maxorNametag = new WeakReference<>(stand);

                return stand;
            }
        }

        // Caller should handle this.
        return null;
    }

    @Nullable
    private static final EntityArmorStand getOrFindMaxorNametag() {
        if (null == MaxorHPDisplay.maxorNametag) {
            // May return null; caller should handle it.
            return MaxorHPDisplay.findAndAssignMaxorNametag();
        }

        final var cached = MaxorHPDisplay.maxorNametag.get();

        // Since it's a WeakReference, it can get cleared without us calling the clear method. In this case, try to find and assign it again, otherwise will return null. Return existing entity if cached is not null.
        return null == cached ? MaxorHPDisplay.findAndAssignMaxorNametag() : cached;
    }

    private static final double findMaxorHp() {
        final var entity = MaxorHPDisplay.getOrFindMaxor();

        if (null == entity) {
            return -1.0D;
        }

        final var hp = entity.getHealth();
        MaxorHPDisplay.maxorDead = Utils.compareFloatExact(1.0F, hp) || Utils.compareFloatExact(3.0F, hp); // It's either 1.0F or, rarely, 3.0F once it dies.

        return hp / Math.max(1.0D, entity.getMaxHealth()) * 100.0D;
    }

    @Override
    final void clear() {
        if (null != MaxorHPDisplay.maxor) {
            MaxorHPDisplay.maxor.clear();
        }

        if (null != MaxorHPDisplay.maxorNametag) {
            MaxorHPDisplay.maxorNametag.clear();
        }

        MaxorHPDisplay.maxor = null;
        MaxorHPDisplay.maxorNametag = null;

        MaxorHPDisplay.maxorHp = 0.0D;
        MaxorHPDisplay.lastMaxorHp = 0.0D;

        MaxorHPDisplay.maxorDead = false;
        MaxorHPDisplay.saidEnrageSkipHelperMessage = false;

        super.clear();
    }

    @Override
    final void update() {
        if (!this.isEnabled()) {
            return;
        }

        final var isDemoRenderBypass = this.isDemoRenderBypass();

        if (!isDemoRenderBypass && !AdditionalM7Features.isAtPhase1()) {
            this.clear();
            return;
        }

        MaxorHPDisplay.maxorHp = MaxorHPDisplay.findMaxorHp();

        final var maxorNametag = MaxorHPDisplay.getOrFindMaxorNametag();

        MaxorHPDisplay.maxorName = null == maxorNametag ? null : maxorNametag.getName();

        if (isDemoRenderBypass || !Utils.compareDoubleExact(MaxorHPDisplay.lastMaxorHp, MaxorHPDisplay.maxorHp) || !Objects.equals(MaxorHPDisplay.lastMaxorName, MaxorHPDisplay.maxorName) || this.isEmpty()) {
            MaxorHPDisplay.lastMaxorHp = MaxorHPDisplay.maxorHp;
            MaxorHPDisplay.lastMaxorName = MaxorHPDisplay.maxorName;

            super.update();
        }
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        var maxorName = "§e﴾ §8☠§5♃ §c§lMaxor§r §e﴿";
        final var maxorNametag = MaxorHPDisplay.maxorName;

        if (null != maxorNametag) {
            maxorName = maxorNametag;
        }

        final var text = maxorName + "§d's health percentage: ";

        final var hp = MaxorHPDisplay.maxorHp;

        final var color = MaxorHPDisplay.maxorDead ? "§4" : 24.0D >= hp ? "§c" : 74.0D >= hp ? "§e" : "§a";
        final var hpText = MaxorHPDisplay.maxorDead ? "§a§lDead" : String.format("%.1f", hp) + '%';

        lines.add(Utils.compareDoubleExact(-1.0D, hp) && !MaxorHPDisplay.maxorDead ? text + "§c§lUnknown" : text + color + hpText);

        if (Config.isSendEnrageSkipHelperMessage() && !MaxorHPDisplay.saidEnrageSkipHelperMessage && 74.0D >= hp && !DarkAddons.isInLocationEditingGui()) {
            MaxorHPDisplay.saidEnrageSkipHelperMessage = true;
            DarkAddons.queueUserSentMessageOrCommand("/pc Maxor HP: " + hpText + " | Enough damage dealt for first DPS phase!");
        }
    }
}
