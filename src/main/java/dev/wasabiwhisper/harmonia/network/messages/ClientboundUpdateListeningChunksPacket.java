package dev.wasabiwhisper.harmonia.network.messages;

import com.mojang.datafixers.util.Pair;
import com.teamresourceful.bytecodecs.base.ByteCodec;
import com.teamresourceful.bytecodecs.base.object.ObjectByteCodec;
import com.teamresourceful.bytecodecs.defaults.MapCodec;
import com.teamresourceful.resourcefullib.common.bytecodecs.ExtraByteCodecs;
import com.teamresourceful.resourcefullib.common.networking.base.CodecPacketHandler;
import com.teamresourceful.resourcefullib.common.networking.base.Packet;
import com.teamresourceful.resourcefullib.common.networking.base.PacketContext;
import com.teamresourceful.resourcefullib.common.networking.base.PacketHandler;
import dev.wasabiwhisper.harmonia.Harmonia;
import dev.wasabiwhisper.harmonia.client.ClientClaims;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.Map;

public record ClientboundUpdateListeningChunksPacket(
        String id,
        ResourceKey<Level> dimension,
        Component displayName,
        int color,
        Map<ChunkPos, Pair<Boolean, ClaimType>> claims,
        String teamId
) implements Packet<ClientboundUpdateListeningChunksPacket> {
    public static final ResourceLocation ID = new ResourceLocation(Harmonia.MOD_ID, "update_listening_chunks");
    public static final Handler HANDLER = new Handler();

    public ResourceLocation getID() {
        return ID;
    }

    public PacketHandler<ClientboundUpdateListeningChunksPacket> getHandler() {
        return HANDLER;
    }

    public String id() {
        return id;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    public Component displayName() {
        return this.displayName;
    }

    public int color() {
        return this.color;
    }

    public Map<ChunkPos, Pair<Boolean, ClaimType>> claims() {
        return this.claims;
    }

    public String teamId() {
        return teamId;
    }

    private static class Handler extends CodecPacketHandler<ClientboundUpdateListeningChunksPacket> {
        private static final MapCodec<ChunkPos, Pair<Boolean, ClaimType>> CHUNK_POS_CLAIM_CODEC = new MapCodec<>(ExtraByteCodecs.CHUNK_POS, ObjectByteCodec.create(ByteCodec.BOOLEAN.fieldOf(Pair::getFirst), ClaimType.CODEC.fieldOf(Pair::getSecond), Pair::new));

        public Handler() {
            super(ObjectByteCodec.create(ByteCodec.STRING.fieldOf(ClientboundUpdateListeningChunksPacket::id),
                    ExtraByteCodecs.DIMENSION.fieldOf(ClientboundUpdateListeningChunksPacket::dimension),
                    ExtraByteCodecs.COMPONENT.fieldOf(ClientboundUpdateListeningChunksPacket::displayName),
                    ByteCodec.VAR_INT.fieldOf(ClientboundUpdateListeningChunksPacket::color),
                    CHUNK_POS_CLAIM_CODEC.fieldOf(ClientboundUpdateListeningChunksPacket::claims),
                    ByteCodec.STRING.fieldOf(ClientboundUpdateListeningChunksPacket::teamId),
                    ClientboundUpdateListeningChunksPacket::new));
        }

        public PacketContext handle(ClientboundUpdateListeningChunksPacket message) {
            return (player, level) -> ClientClaims.get(message.dimension).update(message.id(), message.displayName(), message.color(), message.claims(), message.teamId());
        }
    }
}
