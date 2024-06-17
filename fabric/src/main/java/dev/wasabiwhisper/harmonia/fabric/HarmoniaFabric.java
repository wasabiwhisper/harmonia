package dev.wasabiwhisper.harmonia.fabric;

import net.fabricmc.api.ModInitializer;

import dev.wasabiwhisper.harmonia.Harmonia;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public final class HarmoniaFabric implements ModInitializer {
    public static final String MOD_ID = "harmonia";

    @Override
    public void onInitialize() {
        Harmonia.LOGGER.info("-- Harmonia v" + getModInfoVersion() + " --");
    }

    protected String getModInfoVersion() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).get();
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }
}
