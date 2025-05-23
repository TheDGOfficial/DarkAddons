package gg.darkaddons.mixins;

import gg.darkaddons.DarkAddons;
import gg.darkaddons.annotations.bytecode.Bridge;
import gg.darkaddons.annotations.bytecode.Synthetic;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.NetworkManager;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.network.play.client.C19PacketResourcePackStatus;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.network.play.server.S48PacketResourcePackSend;
import net.minecraft.network.play.server.S0FPacketSpawnMob;
import net.minecraft.tileentity.TileEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@Mixin(value = NetHandlerPlayClient.class, priority = 999)
final class MixinNetHandlerPlayClient {
    @NotNull
    @Unique
    private static final Logger LOGGER = LogManager.getLogger();

    @NotNull
    @Shadow
    private WorldClient clientWorldController;

    private MixinNetHandlerPlayClient() {
        super();
    }

    @Synthetic
    @Bridge
    @NotNull
    @Unique
    private static final String decd(@NotNull final String str, @NotNull final String enc) throws UnsupportedEncodingException {
        return URLDecoder.decode(str, enc);
    }

    private static final boolean isValidResourcePackUrl$darkaddons(@NotNull final NetHandlerPlayClient client, @NotNull final S48PacketResourcePackSend packet) {
        try {
            var url = packet.getURL();
            final var uri = new URI(url);
            final var scheme = uri.getScheme();
            @SuppressWarnings("NegativelyNamedBooleanVariable") final var isNotLevelProtocol = !"level".equals(scheme);
            if (!"http".equals(scheme) && !"https".equals(scheme) && isNotLevelProtocol) {
                client.getNetworkManager().sendPacket(new C19PacketResourcePackStatus(packet.getHash(), C19PacketResourcePackStatus.Action.FAILED_DOWNLOAD));
                throw new URISyntaxException(url, "Wrong protocol");
            }
            url = MixinNetHandlerPlayClient.decd(url.substring("level://".length()), StandardCharsets.UTF_8.toString());
            if (isNotLevelProtocol || !url.contains("..") && url.endsWith("/resources.zip")) {
                return true;
            }
            MixinNetHandlerPlayClient.LOGGER.error("Malicious server tried to access {}", url);
            throw new URISyntaxException(url, "Invalid levelstorage resourcepack path");
        } catch (final URISyntaxException | UnsupportedEncodingException e) {
            MixinNetHandlerPlayClient.LOGGER.catching(e);
            return false;
        }
    }

    @Inject(method = "handleResourcePack", at = @At("HEAD"), cancellable = true)
    private final void resourceExploitFix$darkaddons(@NotNull final S48PacketResourcePackSend packetIn, @NotNull final CallbackInfo ci) {
        //noinspection DataFlowIssue
        if (!MixinNetHandlerPlayClient.isValidResourcePackUrl$darkaddons((NetHandlerPlayClient) (Object) this, packetIn)) {
            ci.cancel();
        }
    }

    @Redirect(method = "handleUpdateTileEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/tileentity/TileEntity;onDataPacket(Lnet/minecraft/network/NetworkManager;Lnet/minecraft/network/play/server/S35PacketUpdateTileEntity;)V", remap = false))
    private final void onDataPacket$darkaddons(@Nullable final TileEntity tileEntity, @NotNull final NetworkManager networkManager, @NotNull final S35PacketUpdateTileEntity packet) {
        if (null != tileEntity) {
            tileEntity.onDataPacket(networkManager, packet);
        }
    }

    @Inject(method = "handleSpawnMob", at = @At("TAIL"))
    private final void handleSpawnMob$darkaddons(@NotNull final S0FPacketSpawnMob packetIn, @NotNull final CallbackInfo ci) {
        final var entity = this.clientWorldController.getEntityByID(packetIn.getEntityID());
        if (null != entity) {
            DarkAddons.handleSpawnMob(entity);
        }
    }
}
