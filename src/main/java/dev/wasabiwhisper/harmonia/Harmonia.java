package dev.wasabiwhisper.harmonia;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Harmonia implements ModInitializer {
    public static final String MOD_ID = "harmonia";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("-- Harmonia v" + getModInfoVersion() + " --");
    }

    protected String getModInfoVersion() {
        ModContainer modContainer = FabricLoader.getInstance().getModContainer(MOD_ID).get();
        return modContainer.getMetadata().getVersion().getFriendlyString();
    }

}