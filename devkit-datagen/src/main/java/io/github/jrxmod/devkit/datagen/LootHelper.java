package io.github.jrxmod.devkit.datagen;

import net.minecraft.block.Block;
import net.minecraft.data.server.loottable.BlockLootTableGenerator;
import net.minecraft.loot.LootTable;

/**
 * Loot table generation utilities.
 * <p>
 * Provides concise wrappers around vanilla
 * {@link BlockLootTableGenerator} methods frequently used
 * in content mods.
 *
 * @author jrxmod
 * @since 0.1.0
 */
public final class LootHelper {
    private LootHelper() {}

    /**
     * Functional interface used by DevKit datagen providers
     * to reduce anonymous class boilerplate.
     */
    @FunctionalInterface
    public interface BlockLootFactory {
        /**
         * Creates a loot table builder for the given block.
         *
         * @param block target block
         * @return loot table builder
         */
        LootTable.Builder create(Block block);
    }

    /**
     * Standard drop-self loot table.
     * Intended for method reference usage:
     * {@code LootHelper::dropsSelf}
     *
     * @param block block instance - caller must supply via wrapper
     * @return null – placeholder for future fluent API
     */
    public static LootTable.Builder dropsSelf(Block block) {
        // Implemented by caller via BlockLootTableGenerator.addDrop(block)
        // Retained as typed marker for future DSL expansion.
        return null;
    }
}
