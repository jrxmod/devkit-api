package io.github.jrxmod.devkit.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootTableProvider;
import net.minecraft.block.Block;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.RegistryWrapper;

import java.util.concurrent.CompletableFuture;

/**
 * Base provider exposing concise wrappers around vanilla's protected block
 * loot-table helpers.
 *
 * <pre>{@code
 * public final class MyLoot extends LootHelper {
 *     public MyLoot(FabricDataOutput output,
 *                   CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
 *         super(output, registries);
 *     }
 *
 *     public void generate() {
 *         dropsSelf(ModBlocks.RUBY_BLOCK);
 *     }
 * }
 * }</pre>
 */
public abstract class LootHelper extends FabricBlockLootTableProvider {
    protected LootHelper(FabricDataOutput output,
                         CompletableFuture<RegistryWrapper.WrapperLookup> registries) {
        super(output, registries);
    }

    /** Adds normal self-drop tables for all supplied blocks. */
    protected final void dropsSelf(Block... blocks) {
        for (Block block : blocks) {
            addDrop(block);
        }
    }

    /** Adds a table where breaking {@code block} drops another item or block. */
    protected final void dropsAs(Block block, ItemConvertible drop) {
        addDrop(block, drop);
    }

    /** Adds a silk-touch-only drop table. */
    protected final void dropsWithSilkTouch(Block block) {
        addDropWithSilkTouch(block);
    }
}
