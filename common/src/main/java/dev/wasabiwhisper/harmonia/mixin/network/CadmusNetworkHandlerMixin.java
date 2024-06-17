package dev.wasabiwhisper.harmonia.mixin.network;

import com.teamresourceful.resourcefullib.common.networking.base.NetworkDirection;
import dev.wasabiwhisper.harmonia.network.messages.ClientboundUpdateListeningChunksPacket;
import dev.wasabiwhisper.harmonia.network.messages.ServerboundListenToChunksPacket;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static earth.terrarium.cadmus.common.network.NetworkHandler.CHANNEL;

@Mixin(value = NetworkHandler.class, remap = false)
public class CadmusNetworkHandlerMixin {
    @Inject(method = "init", at = @At("TAIL"))
    private static void registerPackets(CallbackInfo info) {
        CHANNEL.registerPacket(NetworkDirection.CLIENT_TO_SERVER,
                ServerboundListenToChunksPacket.ID,
                ServerboundListenToChunksPacket.HANDLER,
                ServerboundListenToChunksPacket.class);
        CHANNEL.registerPacket(NetworkDirection.SERVER_TO_CLIENT,
                ClientboundUpdateListeningChunksPacket.ID,
                ClientboundUpdateListeningChunksPacket.HANDLER,
                ClientboundUpdateListeningChunksPacket.class);
    }
}
