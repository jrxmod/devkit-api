package io.github.jrxmod.devkit.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricModelProvider;
import net.minecraft.data.client.BlockStateModelGenerator;
import net.minecraft.data.client.ItemModelGenerator;
import net.minecraft.data.client.Models;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Simplified model datagen utilities.
 * <p>
 * Provides one-line helpers for common item and block models,
 * reducing repetitive {@code BlockStateModelGenerator} calls.
 *
 * @author jrxmod
 * @since 0.1.0
 */
public abstract class ModelGen extends FabricModelProvider {
    protected final String modId;

    public ModelGen(FabricDataOutput output, String modId) {
        super(output);
        this.modId = modId;
    }

    @Override
    public void generateBlockStateModels(BlockStateModelGenerator gen) {
        generateBlockModels(gen);
    }

    @Override
    public void generateItemModels(ItemModelGenerator gen) {
        generateItemModelsImpl(gen);
    }

    /**
     * Override to register block models.
     *
     * @param gen block state model generator
     */
    protected void generateBlockModels(BlockStateModelGenerator gen) {
        // no-op – override in subclass
    }

    /**
     * Override to register item models.
     *
     * @param gen item model generator
     */
    protected void generateItemModelsImpl(ItemModelGenerator gen) {
        // no-op – override in subclass
    }

    /**
     * Registers a simple generated item model.
     *
     * @param gen  item model generator
     * @param item target item
     */
    protected void simpleItem(ItemModelGenerator gen, Item item) {
        gen.register(item, Models.GENERATED);
    }

    /**
     * Registers a simple item model by registry identifier.
     * Useful when working with {@link io.github.jrxmod.devkit.registry.RegistrySupplier}.
     *
     * @param gen item model generator
     * @param id  item identifier (must be in current mod namespace)
     */
    protected void simpleItem(ItemModelGenerator gen, Identifier id) {
        Item item = Registries.ITEM.get(id);
        if (Registries.ITEM.getId(item).equals(id)) {
            gen.register(item, Models.GENERATED);
        }
    }

    /**
     * Registers a handheld item model (tools, weapons).
     *
     * @param gen  item model generator
     * @param item target item
     */
    protected void handheldItem(ItemModelGenerator gen, Item item) {
        gen.register(item, Models.HANDHELD);
    }
}
