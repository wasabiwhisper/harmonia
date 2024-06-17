package dev.wasabiwhisper.forge.mixin.api.claims;

import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.claims.ClaimApiImpl;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.teams.TeamHelper;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;


@Mixin(value = ClaimApiImpl.class)
public abstract class CadmusClaimApiMixin implements ClaimApi {

    @Override
    public void unclaim(ServerLevel serverLevel, ChunkPos chunkPos, @NotNull ServerPlayer player) {
        String id = TeamHelper.getTeamId(player.server, player.getUUID());
        Pair<String, ClaimType> claim = ClaimHandler.getClaim(serverLevel, chunkPos);
        if (claim != null) {
            if (player.hasPermissions(2)) {
                id = claim.getFirst();
            }
        }
        ClaimHandler.unclaim(serverLevel, id, chunkPos);
        serverLevel.players().forEach((p) -> {
            ModUtils.displayTeamName(p, p.chunkPosition());
        });
    }

}
