package dev.wasabiwhisper.harmonia.claims;

import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;

public interface ClaimHandlerExtListener {
    ClaimListenHandler harmonia$getListenHandler();

    Map<ChunkPos, Pair<String, ClaimType>> harmonia$getClaims();
}
