package gg.darkaddons;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.input.Keyboard;

final class RequeueKey {
    @NotNull
    private static final KeyBinding REQUEUE_KEY = new KeyBinding("Requeue to Another Dungeon/Kuudra", Keyboard.KEY_R, DarkAddons.MOD_NAME);

    RequeueKey() {
        super();
    }

    static final void registerKeybindings() {
        ClientRegistry.registerKeyBinding(RequeueKey.REQUEUE_KEY);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public final void onTick(@NotNull final TickEvent.ClientTickEvent event) {
        if (Config.isPressKeyToRequeue() && TickEvent.Phase.END == event.phase && RequeueKey.REQUEUE_KEY.isPressed()) {
            final var requeueCommand = "/instancerequeue";

            if (!DarkAddons.isMessageOrCommandQueuedToBeSentByUser(requeueCommand)) {
                DarkAddons.queueUserSentMessageOrCommand(requeueCommand);
            }
        }
    }
}
