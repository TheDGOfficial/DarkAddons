package gg.darkaddons;

import net.minecraft.entity.item.EntityXPOrb;
import org.jetbrains.annotations.NotNull;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;

import net.minecraftforge.event.entity.EntityJoinWorldEvent;

final class HideXPOrbs {
    HideXPOrbs() {
        super();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void onEntityJoinWorld(@NotNull final EntityJoinWorldEvent event) {
        final var entity = event.entity;

        if (Config.isHideExperienceOrbs() && entity instanceof EntityXPOrb) {
            entity.setDead();
        }
    }
}
