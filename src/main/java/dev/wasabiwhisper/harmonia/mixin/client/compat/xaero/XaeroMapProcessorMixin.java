package dev.wasabiwhisper.harmonia.mixin.client.compat.xaero;

import dev.wasabiwhisper.harmonia.client.compat.xaero.XaeroCompat;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.MapProcessor;
import xaero.map.world.MapWorld;

@Mixin(value = MapProcessor.class, remap = false)
public abstract class XaeroMapProcessorMixin {
    @Shadow
    private MapWorld mapWorld;

    @Inject(method = "onWorldUnload", at = @At("TAIL"))
    private void removeListener(CallbackInfo info) {
        ResourceKey<Level> dimId = mapWorld.getCurrentDimensionId();
        if (dimId != null) XaeroCompat.removeListener(dimId);
    }

    @Inject(method = "updateDimension", at = @At("TAIL"))
    private void updateListener(ClientLevel world, ResourceKey<Level> dimId, CallbackInfo info) {
        if (dimId != null) XaeroCompat.registerListener(dimId);
    }
}
