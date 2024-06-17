package dev.wasabiwhisper.harmonia.client.compat.xaero;

import dev.wasabiwhisper.harmonia.client.ClientClaims;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.network.NetworkHandler;
import earth.terrarium.cadmus.common.network.messages.ServerboundUpdateClaimedChunksPacket;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

@Environment(EnvType.CLIENT)
public class XaeroClaimsManager {
    private final Map<ResourceKey<Level>, Map<ChunkPos, ClientClaims.Entry>> map = new HashMap<>();

    public @Nullable ClientClaims.Entry get(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        Map<ChunkPos, ClientClaims.Entry> claims = map.get(dimension);
        if (claims == null) return null;
        return claims.get(new ChunkPos(chunkX, chunkZ));
    }

    public Map<ChunkPos, ClientClaims.Entry> put(ResourceKey<Level> dimension, Map<ChunkPos, ClientClaims.Entry> claims) {
        return map.put(dimension, claims);
    }

    public void clear(ResourceKey<Level> dimension) {
        map.remove(dimension);
    }

    public void changeSelection(ResourceKey<Level> dimension, int left, int top, int right, int bottom, boolean claim, @Nullable ClaimType type, boolean admin) {
        Map<ChunkPos, ClaimType> addedChunks = new HashMap<>();
        Map<ChunkPos, ClaimType> removedChunks = new HashMap<>();
        for (int x = left; x <= right; ++x) {
            for (int z = top; z <= bottom; ++z) {
                var entry = get(dimension, x, z);
                if (claim) {
                    if (type == null) continue;
                    switch (type) {
                        // Claim
                        case CLAIMED -> {
                            if (entry == null) addedChunks.put(new ChunkPos(x, z), type);
                        }
                        // Chunkload
                        case CHUNK_LOADED -> {
                            if (entry != null && entry.teamId().equals(ClientClaims.ID) && entry.type() != ClaimType.CHUNK_LOADED)
                                addedChunks.put(new ChunkPos(x, z), type);
                        }
                    }
                } else {
                    // Unclaim
                    if (type == null && entry != null && (admin || entry.teamId().equals(ClientClaims.ID))) {
                        removedChunks.put(new ChunkPos(x, z), entry.type());
                    }
                    // Unchunkload
                    if (type == ClaimType.CLAIMED && entry != null && entry.teamId().equals(ClientClaims.ID) && entry.type() == ClaimType.CHUNK_LOADED)
                        addedChunks.put(new ChunkPos(x, z), type);
                }
            }
        }
        if (!addedChunks.isEmpty() || !removedChunks.isEmpty()) {
            NetworkHandler.CHANNEL.sendToServer(new ServerboundUpdateClaimedChunksPacket(addedChunks, removedChunks));
        }
    }
}
