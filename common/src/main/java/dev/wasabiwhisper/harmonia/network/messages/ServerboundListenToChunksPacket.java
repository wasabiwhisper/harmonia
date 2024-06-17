package dev.wasabiwhisper.harmonia.network.messages;

import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.networking.base.CodecPacketHandler;
import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import dev.wasabiwhisper.harmonia.Harmonia;
import dev.wasabiwhisper.harmonia.claims.ClaimHandlerExtListener;
import dev.wasabiwhisper.harmonia.claims.ClaimListenHandler;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;

public record ServerboundListenToChunksPacket(ResourceKey<Level> dimension,
                                              boolean subscribe) implements Packet<ServerboundListenToChunksPacket> {
    public static final ResourceLocation ID = new ResourceLocation(Harmonia.MOD_ID, "listen_to_chunks");
    public static final Handler HANDLER = new Handler();

    public ResourceLocation getID() {
        return ID;
    }

    public PacketHandler<ServerboundListenToChunksPacket> getHandler() {
        return HANDLER;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    public boolean subscribe() {
        return this.subscribe;
    }

    private static class Handler extends CodecPacketHandler<ServerboundListenToChunksPacket> {
        public Handler() {
            super(ObjectByteCodec.create(ExtraByteCodecs.DIMENSION.fieldOf(ServerboundListenToChunksPacket::dimension), ByteCodec.BOOLEAN.fieldOf(ServerboundListenToChunksPacket::subscribe), ServerboundListenToChunksPacket::new));
        }

        public PacketContext handle(ServerboundListenToChunksPacket message) {
            return (player, level) -> {
                MinecraftServer server = player.getServer();
                if (server != null) {
                    ClaimListenHandler handler = ((ClaimHandlerExtListener) ClaimHandler.read(server.getLevel(message.dimension()))).harmonia$getListenHandler();
                    if (message.subscribe) {
                        handler.addListener(player);
                    } else {
                        handler.removeListener(player);
                    }
                }
            };
        }
    }
}
