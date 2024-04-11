package dev.wasabiwhisper.harmonia.claims;

import com.mojang.datafixers.util.Pair;
import dev.wasabiwhisper.harmonia.network.messages.ClientboundUpdateListeningChunksPacket;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.function.Supplier;

public final class ClaimListenHandler {
    private static final int MAX_CHUNKS_PER_PACKET = 500;
    // Players which are listening to updates.
    private final Set<UUID> listeners = new HashSet<>();
    private final ResourceKey<Level> dimension;

    public ClaimListenHandler(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public void addListener(Player player) {
        listeners.add(player.getUUID());
        var server = player.getServer();
        if (server == null) return;
        var level = server.getLevel(dimension);
        if (level == null) return;
        var claimData = ClaimHandler.getAllTeamClaims(level);
        if (claimData != null) {
            claimData.forEach((teamId, teamClaims) -> {
                Map<ChunkPos, Pair<Boolean, ClaimType>> claims = new HashMap<>();
                teamClaims.forEach((pos, type) -> {
                    claims.put(pos, Pair.of(true, type));
                });
                sendOrSplitPacket(List.of((ServerPlayer) player), level, teamId, claims);
            });
        }
    }

    public void removeListener(Player player) {
        listeners.remove(player.getUUID());
    }

    public void addClaims(ServerLevel level, String id, Pair<ChunkPos, ClaimType> claimData) {
        sendPacket(level, id, () -> {
            Map<ChunkPos, Pair<Boolean, ClaimType>> claims = new HashMap<>();
            if (claimData != null) {
                claims.put(claimData.getFirst(), Pair.of(true, claimData.getSecond()));
            }
            return claims;
        });
    }

    public void removeClaims(ServerLevel level, String id, Set<ChunkPos> claimData) {
        sendPacket(level, id, () -> {
            Map<ChunkPos, Pair<Boolean, ClaimType>> claims = new HashMap<>();
            if (claimData != null) {
                claimData.forEach(c -> claims.put(c, Pair.of(false, ClaimType.CLAIMED)));
            }
            return claims;
        });
    }

    private void sendPacket(ServerLevel level, String id, Supplier<Map<ChunkPos, Pair<Boolean, ClaimType>>> getter) {
        if (listeners.isEmpty()) return;
        List<ServerPlayer> players = level.getServer().getPlayerList().getPlayers().stream().filter(p -> listeners.contains(p.getUUID())).toList();
        if (players.isEmpty()) return;
        Map<ChunkPos, Pair<Boolean, ClaimType>> claims = getter.get();
        if (claims.isEmpty()) return;
        sendOrSplitPacket(players, level, id, claims);
    }

    private void sendClaims(List<ServerPlayer> players, Component displayName, int color, String teamId, Map<ChunkPos, Pair<Boolean, ClaimType>> claims) {
        List<Map<ChunkPos, Pair<Boolean, ClaimType>>> split = new ArrayList<>();
        Map<ChunkPos, Pair<Boolean, ClaimType>> current = new HashMap<>();
        int count = 0;
        for (var entry : claims.entrySet()) {
            current.put(entry.getKey(), entry.getValue());
            count++;
            if (count >= MAX_CHUNKS_PER_PACKET) {
                split.add(current);
                current = new HashMap<>();
                count = 0;
            }
        }
        if (!current.isEmpty()) {
            split.add(current);
        }
        for (var claimMap : split) {
            players.forEach(player -> NetworkHandler.CHANNEL.sendToPlayer(new ClientboundUpdateListeningChunksPacket(TeamHelper.getTeamId(player.getServer(), player.getUUID()), this.dimension, displayName, color, claimMap, teamId), player));
        }
    }

    private void sendOrSplitPacket(List<ServerPlayer> players, ServerLevel level, String id, Map<ChunkPos, Pair<Boolean, ClaimType>> claims) {
        Component displayName = TeamHelper.getTeamName(id, level.getServer());
        ChatFormatting color = TeamHelper.getTeamColor(id, level.getServer());

        sendClaims(players, displayName, color.getColor() == null ? -1 : color.getColor(), id, claims);
    }
}