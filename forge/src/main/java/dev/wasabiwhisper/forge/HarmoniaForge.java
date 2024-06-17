package dev.wasabiwhisper.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import dev.wasabiwhisper.harmonia.Harmonia;

@Mod(Harmonia.MOD_ID)
public final class HarmoniaForge {
    public HarmoniaForge() {
        // Submit our event bus to let Architectury API register our content on the right time.
        EventBuses.registerModEventBus(Harmonia.MOD_ID, FMLJavaModLoadingContext.get().getModEventBus());

        // Run our common setup.
        Harmonia.init();
    }
}
