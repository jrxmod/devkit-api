package io.github.jrxmod.devkit.fabric.client;

import io.github.jrxmod.devkit.client.DevkitClientCore;
import net.fabricmc.api.ClientModInitializer;

public class DevkitClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        DevkitClientCore.init();
    }
}
