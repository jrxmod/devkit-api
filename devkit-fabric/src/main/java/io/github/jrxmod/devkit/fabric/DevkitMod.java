package io.github.jrxmod.devkit.fabric;

import io.github.jrxmod.devkit.config.ConfigSyncManager;
import io.github.jrxmod.devkit.core.DevkitCore;
import io.github.jrxmod.devkit.networking.DevkitNetworking;
import io.github.jrxmod.devkit.registry.DevkitRegistry;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

/** Main Fabric entrypoint for the all-in-one DevKit API artifact. */
public final class DevkitMod implements ModInitializer {
    @Override
    public void onInitialize() {
        String version = FabricLoader.getInstance()
                .getModContainer(DevkitCore.MOD_ID)
                .map(container -> container.getMetadata().getVersion().getFriendlyString())
                .orElse("development");

        DevkitCore.LOGGER.info("[DevKit API] Initializing {} by {}", version, DevkitCore.AUTHOR);
        DevkitCore.init();
        DevkitRegistry.init();
        DevkitNetworking.init();
        ConfigSyncManager.init();
        DevkitCore.LOGGER.info("[DevKit API] Ready for dependent mods");
    }
}
