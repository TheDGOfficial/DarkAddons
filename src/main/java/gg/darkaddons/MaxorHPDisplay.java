package gg.darkaddons;

import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import net.minecraft.entity.boss.EntityWither;

import net.minecraft.entity.boss.IBossDisplayData;
import net.minecraft.entity.EntityLivingBase;

final class MaxorHPDisplay extends SimpleGuiElement {
    private static double maxorHp;
    private static double lastMaxorHp;

    private static boolean maxorDead;

    MaxorHPDisplay() {
        super("Maxor HP Display", Config::isMaxorHPDisplay, () -> true, () -> 0);

        DarkAddons.registerTickTask("maxor_hp_display_update_maxor_hp", 1, true, this::update);
    }

    private static final double findMaxorHp() {
        for (final var entity : Minecraft.getMinecraft().theWorld.loadedEntityList) {
            if (entity instanceof EntityWither && entity.getName().contains("Maxor")) {
                float hp = -1;
                float maxHp = -1;

                if (entity instanceof final IBossDisplayData boss) {
                    hp = boss.getHealth();
                    maxHp = boss.getMaxHealth();
                }

                if (entity instanceof final EntityLivingBase living) {
                    hp = Math.min(hp, living.getHealth());
                    maxHp = Math.min(maxHp, living.getMaxHealth());
                }

                if (hp == -1 || maxHp == -1) {
                    return -1;
                }

                final var hpPerc = (hp / maxHp) * 100;
                MaxorHPDisplay.maxorDead = hp <= 1;

                return hpPerc;
            }
        }

        return -1;
    }

    @Override
    final void clear() {
        MaxorHPDisplay.maxorHp = 0;
        MaxorHPDisplay.lastMaxorHp = 0;
        MaxorHPDisplay.maxorDead = false;

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

        if (isDemoRenderBypass || MaxorHPDisplay.lastMaxorHp != MaxorHPDisplay.maxorHp || this.isEmpty()) {
            MaxorHPDisplay.lastMaxorHp = MaxorHPDisplay.maxorHp;

            super.update();
        }
    }

    @Override
    final void buildHudLines(@NotNull final Collection<String> lines) {
        final var text = "§e﴾ §c§lMaxor§r§r §e﴿§r§d's health percentage: ";

        final var hp = MaxorHPDisplay.maxorHp;

        final var color = MaxorHPDisplay.maxorDead ? "§4" : hp <= 24 ? "§c" : hp <= 74 ? "§e" : "§a";
        final var hpText = MaxorHPDisplay.maxorDead ? "§a§lDead" : String.format("%.1f", hp) + "%";

        lines.add(hp == -1 && !MaxorHPDisplay.maxorDead ? text + "§c§lUnknown" : text + color + hpText);
    }
}
