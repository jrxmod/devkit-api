package io.github.jrxmod.devkit.datagen;

import io.github.jrxmod.devkit.core.DevkitCore;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;

/**
 * Default datagen entrypoint for the {@code devkit-api} aggregator mod.
 * <p>
 * The library itself does not generate any data – it only provides
 * helpers ({@link LootHelper}, {@link ModelGen}, {@link TagHelper}) for
 * dependent mods. This entrypoint is registered so that the build
 * pipeline validates the datagen classpath successfully. Dependent
 * mods should create their own entrypoint that extends
 * {@code FabricDataGenerator.createPack()} and registers their own
 * providers.
 * <p>
 * Example for a dependent mod:
 * <pre>{@code
 * public class MyModDataGen implements DataGeneratorEntrypoint {
 *     @Override
 *     public void onInitializeDataGenerator(FabricDataGenerator gen) {
 *         FabricDataGenerator.Pack pack = gen.createPack();
 *         pack.addProvider(MyModelGen::new);
 *         pack.addProvider((output, registries) -> new MyTagGen(output, registries, "mymod"));
 *     }
 * }
 * }</pre>
 *
 * @author jrxmod
 * @since 0.1.0
 */
public class DevkitDataGenerator implements DataGeneratorEntrypoint {
    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        // Library provides helpers only; no providers are registered here.
        DevkitCore.LOGGER.info("[DevKit] Datagen entrypoint ready (no providers – library mode)");
    }
}
