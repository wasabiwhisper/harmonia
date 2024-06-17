package dev.wasabiwhisper.harmonia.client;

import dev.wasabiwhisper.harmonia.Harmonia;
import dev.wasabiwhisper.harmonia.client.compat.xaero.XaeroCompat;

public class HarmoniaClient {
    public static XaeroCompat xaeros = null;

    public static void init() {
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