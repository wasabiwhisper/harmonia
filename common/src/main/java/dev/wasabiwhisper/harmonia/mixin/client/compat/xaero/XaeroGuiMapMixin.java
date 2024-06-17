package dev.wasabiwhisper.harmonia.mixin.client.compat.xaero;

import com.llamalad7.mixinextras.sugar.Local;
import dev.wasabiwhisper.harmonia.client.HarmoniaClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xaero.map.MapProcessor;
import xaero.map.gui.GuiMap;
import xaero.map.gui.MapTileSelection;
import xaero.map.gui.dropdown.rightclick.RightClickOption;

import java.util.ArrayList;

@Mixin(value = GuiMap.class, remap = false)
public class XaeroGuiMapMixin {
    @Shadow
    private MapTileSelection mapTileSelection;
    @Shadow
    private MapProcessor mapProcessor;

    @Inject(method = "getRightClickOptions", at = @At("RETURN"))
    private void getRightClickOptions(CallbackInfoReturnable<ArrayList<RightClickOption>> cir, @Local ArrayList<RightClickOption> options) {
        HarmoniaClient.xaeros.addRightClickOptions((GuiMap) (Object) this, options, mapTileSelection, mapProcessor);
    }

}
