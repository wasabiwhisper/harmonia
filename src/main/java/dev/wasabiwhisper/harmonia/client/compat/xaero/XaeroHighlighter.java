package dev.wasabiwhisper.harmonia.client.compat.xaero;

import dev.wasabiwhisper.harmonia.client.ClientClaims;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import xaero.map.WorldMap;
import xaero.map.highlight.ChunkHighlighter;

import java.util.List;

@Environment(EnvType.CLIENT)
public class XaeroHighlighter extends ChunkHighlighter {
    private final XaeroClaimsManager manager;

    protected XaeroHighlighter(XaeroClaimsManager manager) {
        super(true);
        this.manager = manager;
    }

    @Override
    protected int[] getColors(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (!WorldMap.settings.displayClaims) return null;
        ClientClaims.Entry currentClaim = this.manager.get(dimension, chunkX, chunkZ);
        if (currentClaim == null) return null;

        ClientClaims.Entry topClaim = this.manager.get(dimension, chunkX, chunkZ - 1);
        ClientClaims.Entry rightClaim = this.manager.get(dimension, chunkX + 1, chunkZ);
        ClientClaims.Entry bottomClaim = this.manager.get(dimension, chunkX, chunkZ + 1);
        ClientClaims.Entry leftClaim = this.manager.get(dimension, chunkX - 1, chunkZ);

        int claimColor = currentClaim.type() == ClaimType.CHUNK_LOADED ? 0xFFFF9900 : currentClaim.color();
        int claimColorFormatted = (claimColor & 255) << 24 | (claimColor >> 8 & 255) << 16 | (claimColor >> 16 & 255) << 8;
        int fillOpacity = WorldMap.settings.claimsFillOpacity;
        int borderOpacity = WorldMap.settings.claimsBorderOpacity;
        int centerColor = claimColorFormatted | 255 * fillOpacity / 100;
        int sideColor = claimColorFormatted | 255 * borderOpacity / 100;

        this.resultStore[0] = centerColor;
        this.resultStore[1] = !currentClaim.equals(topClaim) ? sideColor : centerColor;
        this.resultStore[2] = !currentClaim.equals(rightClaim) ? sideColor : centerColor;
        this.resultStore[3] = !currentClaim.equals(bottomClaim) ? sideColor : centerColor;
        this.resultStore[4] = !currentClaim.equals(leftClaim) ? sideColor : centerColor;
        return this.resultStore;
    }

    @Override
    public Component getChunkHighlightSubtleTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (!WorldMap.settings.displayClaims) return null;
        return Component.literal("Claim");
    }

    @Override
    public Component getChunkHighlightBluntTooltip(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        if (!WorldMap.settings.displayClaims) return null;
        if (!ClientClaims.CLAIMS.containsKey(dimension)) return null;
        ClientClaims.Entry claim = this.manager.get(dimension, chunkX, chunkZ);
        if (claim == null) return null;
        return claim.name().copy().append(claim.type() == ClaimType.CHUNK_LOADED ? Component.literal(" ").append(Component.translatable("gui.harmonia.chunkloaded").withStyle(ChatFormatting.ITALIC, ChatFormatting.GOLD)) : Component.empty());
    }

    @Override
    public int calculateRegionHash(ResourceKey<Level> dimension, int regionX, int regionZ) {
        if (!WorldMap.settings.displayClaims) return 0;
        if (!ClientClaims.CLAIMS.containsKey(dimension)) return 0;
        if (!regionHasHighlights(dimension, regionX, regionZ)) return 0;

        int chunkX = regionX << 5;
        int chunkZ = regionZ << 5;
        boolean topRegionClaimed = regionHasHighlights(dimension, regionX, regionZ - 1);
        int topChunkX = regionX << 5;
        int topChunkZ = (regionZ - 1) << 5;
        boolean rightRegionClaimed = regionHasHighlights(dimension, regionX + 1, regionZ);
        int rightChunkX = (regionX + 1) << 5;
        int rightChunkZ = regionZ << 5;
        boolean bottomRegionClaimed = regionHasHighlights(dimension, regionX, regionZ + 1);
        int bottomChunkX = regionX << 5;
        int bottomChunkZ = (regionZ + 1) << 5;
        boolean leftRegionClaimed = regionHasHighlights(dimension, regionX - 1, regionZ);
        int leftChunkX = (regionX - 1) << 5;
        int leftChunkZ = regionZ << 5;

        long accumulator = WorldMap.settings.claimsBorderOpacity;
        accumulator = accumulator * 37L + (long) WorldMap.settings.claimsFillOpacity;

        for (int regionLocalX = 0; regionLocalX < 32; ++regionLocalX) {
            accumulator = this.accountClaim(accumulator, topRegionClaimed ? this.manager.get(dimension, topChunkX + regionLocalX, topChunkZ + 31) : null);
            accumulator = this.accountClaim(accumulator, rightRegionClaimed ? this.manager.get(dimension, rightChunkX, rightChunkZ + regionLocalX) : null);
            accumulator = this.accountClaim(accumulator, bottomRegionClaimed ? this.manager.get(dimension, bottomChunkX + regionLocalX, bottomChunkZ) : null);
            accumulator = this.accountClaim(accumulator, leftRegionClaimed ? this.manager.get(dimension, leftChunkX + 31, leftChunkZ + regionLocalX) : null);

            for (int regionLocalZ = 0; regionLocalZ < 32; ++regionLocalZ) {
                ClientClaims.Entry claim = this.manager.get(dimension, chunkX + regionLocalX, chunkZ + regionLocalZ);
                accumulator = this.accountClaim(accumulator, claim);
            }
        }
        return (int) (accumulator >> 32) * 37 + (int) (accumulator & -1L);
    }

    private long accountClaim(long accumulator, ClientClaims.Entry claim) {
        if (claim != null) {
            String name = claim.name().getString();
            for (int i = 0; i < name.length(); i++) {
                accumulator = accumulator * 37L + name.charAt(i);
            }
            accumulator += claim.color();
        }
        accumulator *= 37L;
        return accumulator;
    }

    @Override
    public boolean regionHasHighlights(ResourceKey<Level> dimension, int regionX, int regionZ) {
        if (!WorldMap.settings.displayClaims) return false;
        if (!ClientClaims.CLAIMS.containsKey(dimension)) return false;

        for (int regionLocalX = 0; regionLocalX < 32; ++regionLocalX) {
            for (int regionLocalZ = 0; regionLocalZ < 32; ++regionLocalZ) {
                if (this.manager.get(dimension, (regionX << 5) + regionLocalX, (regionZ << 5) + regionLocalZ) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean chunkIsHighlit(ResourceKey<Level> dimension, int chunkX, int chunkZ) {
        return this.manager.get(dimension, chunkX, chunkZ) != null;
    }

    @Override
    public void addMinimapBlockHighlightTooltips(List<Component> list, ResourceKey<Level> dimension, int blockX, int blockZ, int width) {

    }
}
