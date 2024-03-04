package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
//import gg.darkaddons.annotations.bytecode.Bridge;
//import gg.darkaddons.annotations.bytecode.Synthetic;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.GuiOpenEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = EventBus.class, priority = 1_001)
final class MixinEventBus {
    private MixinEventBus() {
        super();
    }

    @Inject(method = "post", at = @At("HEAD"), remap = false)
    private final void post$darkaddons(@NotNull final Event event, @NotNull final CallbackInfoReturnable<Boolean> cir) {
        MixinEventBus.postStatic$darkaddons(event);
    }

    private static final void postStatic$darkaddons(@NotNull final Event event) {
        final boolean tickEvent;
        //noinspection ChainOfInstanceofChecks
        if (event instanceof RenderLivingEvent.Post) {
            //noinspection OverlyStrongTypeCast
            DarkAddons.handleRenderLivingPost(((RenderLivingEvent.Post<?>) event).entity);
        } else if (event instanceof RenderWorldLastEvent) {
            DarkAddons.handleRenderWorldLast((RenderWorldLastEvent) event);
        } else if ((tickEvent = event instanceof TickEvent) && event instanceof TickEvent.RenderTickEvent) {
            DarkAddons.handleRenderTick((TickEvent.RenderTickEvent) event);
        } else if (tickEvent && event instanceof TickEvent.ClientTickEvent) {
            DarkAddons.handleClientTick((TickEvent.ClientTickEvent) event);
        } else if (tickEvent && event instanceof TickEvent.PlayerTickEvent) {
            DarkAddons.handlePlayerTick((TickEvent.PlayerTickEvent) event);
        } else if (event instanceof final ClientChatReceivedEvent e) {

            DarkAddons.handleClientChatReceived(e);
            //MixinEventBus.handleMessageInternal$darkaddons(e);
        } else if (event instanceof GuiOpenEvent) {
            DarkAddons.handleGuiOpen((GuiOpenEvent) event);
        } else if (event instanceof WorldEvent.Load) {
            DarkAddons.handleWorldLoad();
        }/* else {
            MixinEventBus.handleOtherEvents$darkaddons(event);
        }*/
    }

    /*@Synthetic
    @Bridge
    private static final void handleOtherEvents$darkaddons(@NotNull final Event event) {
        if (MixinUtils.isMagicFlag() && event instanceof PlayerEvent.NameFormat) {
            final PlayerEvent.NameFormat e = (PlayerEvent.NameFormat) event;
            if ("TheDark_Gamer".equals(e.username)) {
                if (!e.displayname.contains("§6TheDark_Gamer")) {
                    e.displayname = MixinUtils.makeOrangeName(e.displayname);
                }

                final String uncolored = PublicUtils.removeControlCodes(e.displayname);

                if (uncolored.contains("[MVP+]") && !uncolored.contains("[MVP++]")) {
                    e.displayname = MixinUtils.makeBlackMVPPlusPlus(e.displayname);
                }

                if (e.displayname.contains("§8[") && e.displayname.contains("§8]") && !e.displayname.contains("§c320")) {
                    e.displayname = MixinUtils.makeSpecialPrefix(e.displayname);
                }
            }
        }
    }*/

    /*@Synthetic
    @Bridge
    private static final void handleMessageInternal$darkaddons(@NotNull final ClientChatReceivedEvent event) {
        if (!MixinUtils.isMagicFlag() || !PublicUtils.isStandardTextMessage(event)) {
            return;
        }
        final String message = PublicUtils.removeControlCodes(event.message.getUnformattedText());
        if (message.contains("TheDark_Gamer")) {
            final String oldMessage = event.message.getFormattedText();

            if (!oldMessage.contains("§6TheDark_Gamer")) {
                String newMessage = MixinUtils.makeOrangeName(oldMessage);

                if (message.contains("[MVP+]") && !message.contains("[MVP++]")) {
                    newMessage = MixinUtils.makeBlackMVPPlusPlus(newMessage);
                }

                if (newMessage.contains("joined the lobby!") && !newMessage.contains(">")) {
                    newMessage = "§b>§c>§a> " + newMessage + " §a<§c<§b<";
                }

                if (newMessage.contains("§8[") && newMessage.contains("§8]") && !newMessage.contains("§c320")) {
                    newMessage = MixinUtils.makeSpecialPrefix(newMessage);
                }

                if (!newMessage.equals(oldMessage)) {
                    event.setCanceled(true);
                    UChat.chat(newMessage);
                }
            }
        }
    }*/
}
