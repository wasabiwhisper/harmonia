package dev.wasabiwhisper.harmonia.mixin.client.compat.xaero;

import dev.wasabiwhisper.harmonia.client.compat.xaero.XaeroCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xaero.map.highlight.AbstractHighlighter;
import xaero.map.highlight.HighlighterRegistry;

@Mixin(value = HighlighterRegistry.class, remap = false)
public abstract class XaeroHighlighterRegistryMixin {
    @Shadow
    public abstract void register(AbstractHighlighter highlighter);

    @Inject(method = "<init>()V", at = @At("TAIL"))
    private void registerHighlighter(CallbackInfo info) {
        this.register(XaeroCompat.registerHighlighter());
    }
}
