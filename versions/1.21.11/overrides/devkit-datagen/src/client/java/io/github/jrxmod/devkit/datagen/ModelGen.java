package io.github.jrxmod.devkit.datagen;

import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.minecraft.client.data.BlockStateModelGenerator;
import net.minecraft.client.data.ItemModelGenerator;
import net.minecraft.client.data.Models;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

/**
 * Model datagen base for Minecraft 1.21.11's client data API.
 *
 * @author jrxmod
 * @since 0.2.0
 */
public abstract class ModelGen extends FabricModelProvider {
    protected final String modId;

    public ModelGen(FabricDataOutput output, String modId) {
        super(output);
        this.modId = modId;
    }

    @Override
    public final void generateBlockStateModels(BlockStateModelGenerator generator) {
        generateBlockModels(generator);
    }

    @Override
    public final void generateItemModels(ItemModelGenerator generator) {
        generateItemModelsImpl(generator);
    }

    protected void generateBlockModels(BlockStateModelGenerator generator) {}

    protected void generateItemModelsImpl(ItemModelGenerator generator) {}

    protected final void simpleItem(ItemModelGenerator generator, Item item) {
        generator.register(item, Models.GENERATED);
    }

    protected final void simpleItem(ItemModelGenerator generator, Identifier id) {
        Item item = Registries.ITEM.get(id);
        if (Registries.ITEM.getId(item).equals(id)) {
            simpleItem(generator, item);
        }
    }

    protected final void handheldItem(ItemModelGenerator generator, Item item) {
        generator.register(item, Models.HANDHELD);
    }
}
