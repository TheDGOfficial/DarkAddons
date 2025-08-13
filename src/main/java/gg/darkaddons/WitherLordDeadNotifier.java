package gg.darkaddons;

import gg.essential.universal.UChat;

import java.util.HashMap;
import java.util.WeakHashMap;

import com.google.common.collect.MapMaker;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.boss.EntityWither;

import net.minecraftforge.client.event.ClientChatReceivedEvent;

import org.jetbrains.annotations.NotNull;

final class WitherLordDeadNotifier {
    @NotNull
    private static final Map<String, EntityWither> witherLords = new MapMaker().weakValues().makeMap();
    @NotNull
    private static final WeakHashMap<EntityWither, Boolean> states = new WeakHashMap<>();

    static final void init() {
        DarkAddons.registerTickTask("wither_lord_dead_notifier_tick", 1, true, WitherLordDeadNotifier::tick);
    }

    static final void handleMessage(@NotNull final ClientChatReceivedEvent event) {
        McProfilerHelper.startSection("wither_lord_dead_notifier_handle_message");

        if (MessageType.STANDARD_TEXT_MESSAGE.matches(event.type)) {
            WitherLordDeadNotifier.processMessage(Utils.removeControlCodes(event.message.getFormattedText()));
        }

        McProfilerHelper.endSection();
    }

    private static final void processMessage(@NotNull final String message) {
        if (!Config.isWitherLordDeadNotifier() && !Config.isEdragReminder() && !Config.isPhase3StartingNotification()) {
            return;
        }

        switch (message) {
            case "[BOSS] Maxor: YOU TRICKED ME!" -> {
                if (Config.isWitherLordDeadNotifier()) {
                    WitherLordDeadNotifier.findWitherLord("Maxor");
                }
            }
            case "[BOSS] Storm: ENERGY HEED MY CALL!" -> {
                if (Config.isWitherLordDeadNotifier() || Config.isPhase3StartingNotification()) {
                    WitherLordDeadNotifier.findWitherLord("Storm");
                }
            }
            case "The Core entrance is opening!" -> {
                if (Config.isWitherLordDeadNotifier()) {
                    WitherLordDeadNotifier.findWitherLord("Goldor");
                }
            }
            case "[BOSS] Necron: ARGH!" -> {
                if (Config.isWitherLordDeadNotifier() || Config.isEdragReminder()) {
                    WitherLordDeadNotifier.findWitherLord("Necron");
                }
            }
        }
    }

    private static final void tick() {
        if (!Config.isWitherLordDeadNotifier() && !Config.isEdragReminder() && !Config.isPhase3StartingNotification()) {
            return;
        }

        WitherLordDeadNotifier.witherLords.forEach((name, wither) -> {
            final var state = WitherLordDeadNotifier.states.get(wither);

            final var isDead = Utils.compareFloatExact(1.0F, wither.getHealth()) || Utils.compareFloatExact(3.0F, wither.getHealth()); // It's either 1.0F or, rarely, 3.0F once it dies.

            WitherLordDeadNotifier.states.put(wither, isDead);

            if ((null == state || !state) && isDead) {
                WitherLordDeadNotifier.onWitherLordDead(name);
            }
        });
    }

    private static final void onWitherLordDead(@NotNull final String witherLord) {
        if (Config.isWitherLordDeadNotifier()) {
            GuiManager.createTitle("§b" + witherLord + " dead", 60, true, GuiManager.Sound.PLING);
            UChat.chat("§b" + witherLord + " dead");
        }

        if (Config.isEdragReminder() && "Necron".equals(witherLord)) {
            DarkAddons.sendMessage(Utils.chromaIfEnabledOrAqua() + "Phase 4 done. Equip your Ender Dragon!");
            GuiManager.createTitle("§bSwap to edrag!", 60, true, GuiManager.Sound.PLING);
        }

        if (Config.isPhase3StartingNotification() && "Storm".equals(witherLord)) {
            final var msg = Utils.chromaIfEnabledOrAqua() + "Phase 3 starting";

            DarkAddons.sendMessage(msg);
            GuiManager.createTitle(msg, 60, true, GuiManager.Sound.PLING);
        }
    }

    private static final void findWitherLord(@NotNull final String witherLord) {
        final var world = Minecraft.getMinecraft().theWorld;
        if (null != world) {
            for (final var ent : world.loadedEntityList) {
                if (ent instanceof final EntityWither wither && wither.getName().contains(witherLord)) {
                    WitherLordDeadNotifier.witherLords.put(witherLord, wither);
                    break;
                }
            }
        }
    }
}
