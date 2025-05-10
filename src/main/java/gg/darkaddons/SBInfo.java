package gg.darkaddons;

import net.minecraft.network.Packet;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import net.hypixel.data.type.ServerType;
import net.hypixel.data.type.GameType;

import net.hypixel.modapi.HypixelModAPI;
import net.hypixel.modapi.packet.impl.clientbound.event.ClientboundLocationPacket;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class SBInfo {
    SBInfo() {
        super();
    }

    @Nullable
    private static String mode;
    @Nullable
    private static ServerType serverType;

    static {
        HypixelModAPI.getInstance().subscribeToEventPacket(ClientboundLocationPacket.class);
        HypixelModAPI.getInstance().createHandler(ClientboundLocationPacket.class, packet -> {
            SBInfo.mode = packet.getMode().orElse(null);
            SBInfo.serverType = packet.getServerType().orElse(null);
        });
    }

    @SubscribeEvent
    public final void onDisconnect(@NotNull final FMLNetworkEvent.ClientDisconnectionFromServerEvent event)  {
        SBInfo.mode = null;
        SBInfo.serverType = null;
    }

    static final boolean isInSkyblock() {
        return GameType.SKYBLOCK == SBInfo.serverType;
    }

    @Nullable
    static final String getMode() {
        return SBInfo.mode;
    }
}
