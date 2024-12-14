package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
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
import org.spongepowered.asm.mixin.Unique;
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

    @Unique
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
        } else if (event instanceof GuiOpenEvent) {
            DarkAddons.handleGuiOpen((GuiOpenEvent) event);
        } else if (event instanceof WorldEvent.Unload) {
            DarkAddons.handleWorldLoad();
        }
    }
}
