package dev.wasabiwhisper.harmonia.fabric.client;

import dev.wasabiwhisper.harmonia.client.HarmoniaClient;
import net.fabricmc.api.ClientModInitializer;

public final class HarmoniaFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // This entrypoint is suitable for setting up client-specific logic, such as rendering.
        HarmoniaClient.init();
    }
}
