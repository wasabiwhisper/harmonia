package dev.wasabiwhisper.harmonia.mixin.api.claims;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import earth.terrarium.cadmus.api.claims.ClaimApi;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import earth.terrarium.cadmus.common.util.ModUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;


@Mixin(value = ClaimApi.class)
public interface CadmusClaimApiMixin {

    @Redirect(method = "unclaim(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ChunkPos;Lnet/minecraft/server/level/ServerPlayer;)V",
            at = @At(value = "INVOKE", target = "Learth/terrarium/cadmus/api/claims/ClaimApi;unclaim(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/level/ChunkPos;Ljava/lang/String;)V"))
    private void unclaim(ClaimApi instance, ServerLevel serverLevel, ChunkPos chunkPos, String s, @Local(argsOnly = true) ServerPlayer player) {
        Pair<String, ClaimType> claim = ClaimHandler.getClaim(serverLevel, chunkPos);
        if (claim != null) {
            if (player.hasPermissions(2)) {
                s = claim.getFirst();
            }
        }
        ClaimHandler.unclaim(serverLevel, s, chunkPos);
        serverLevel.players().forEach((p) -> {
            ModUtils.displayTeamName(p, p.chunkPosition());
        });
    }

}
