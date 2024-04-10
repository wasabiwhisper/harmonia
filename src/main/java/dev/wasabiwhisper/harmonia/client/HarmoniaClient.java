package dev.wasabiwhisper.harmonia.client;

import dev.wasabiwhisper.harmonia.Harmonia;
import dev.wasabiwhisper.harmonia.client.compat.xaero.XaeroCompat;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class HarmoniaClient implements ClientModInitializer {
    public static XaeroCompat xaeros = null;

    @Override
    public void onInitializeClient() {
        try {
            Class.forName("earth.terrarium.cadmus.Cadmus");
        } catch (ClassNotFoundException ignored) {
            Harmonia.LOGGER.error("Cadmus not found!");
        }
        try {
            Class.forName("xaero.map.WorldMap");
            xaeros = new XaeroCompat();
        } catch (ClassNotFoundException ignored) {
            Harmonia.LOGGER.error("Xaero's WorldMap not found!");
        }
    }
}