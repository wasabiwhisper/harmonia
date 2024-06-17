package dev.wasabiwhisper.harmonia.mixin.claims;

import com.mojang.datafixers.util.Pair;
import dev.wasabiwhisper.harmonia.claims.ClaimHandlerExtListener;
import dev.wasabiwhisper.harmonia.claims.ClaimListenHandler;
import earth.terrarium.cadmus.common.claims.ClaimHandler;
import earth.terrarium.cadmus.common.claims.ClaimType;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;
import java.util.Set;

import static earth.terrarium.cadmus.common.claims.ClaimHandler.getAllTeamClaims;
import static earth.terrarium.cadmus.common.claims.ClaimHandler.read;

@Mixin(value = ClaimHandler.class, remap = false)
public class CadmusClaimHandlerMixin implements ClaimHandlerExtListener {
    @Shadow
    @Final
    private Map<ChunkPos, Pair<String, ClaimType>> claims;
    @Unique
    private ClaimListenHandler listenHandler;

    @Override
    public ClaimListenHandler harmonia$getListenHandler() {
        return listenHandler;
    }

    @Override
    public Map<ChunkPos, Pair<String, ClaimType>> harmonia$getClaims() {
        return claims;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    public void init(ResourceKey<Level> dimension, CallbackInfo info) {
        listenHandler = new ClaimListenHandler(dimension);
    }

    @Inject(method = "claim", at = @At("TAIL"))
    private static void claim(ServerLevel level, String id, ChunkPos pos, ClaimType type, CallbackInfo info) {
        var claims = ((ClaimHandlerExtListener) read(level)).harmonia$getClaims();
        if (claims.containsKey(pos) && !claims.get(pos).getFirst().equals(id)) return;
        ((ClaimHandlerExtListener) read(level)).harmonia$getListenHandler().addClaims(level, id, Pair.of(pos, type));
    }

    @Inject(method = "unclaim", at = @At("TAIL"))
    private static void unclaim(ServerLevel level, String id, ChunkPos pos, CallbackInfo info) {
        ((ClaimHandlerExtListener) read(level)).harmonia$getListenHandler().removeClaims(level, id, Set.of(pos));
    }

    @Inject(method = "clear", at = @At("HEAD"))
    private static void clear(ServerLevel level, String id, CallbackInfo info) {
        var claimsById = getAllTeamClaims(level);
        if (claimsById.containsKey(id)) {
            ((ClaimHandlerExtListener) read(level)).harmonia$getListenHandler().removeClaims(level, id, claimsById.get(id).keySet());
        }
    }

}