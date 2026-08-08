package io.github.jrxmod.devkit.registry;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
final class BlockFactory {
    private BlockFactory() {}
    static Block create(AbstractBlock.Settings settings, RegistryKey<Block> key) { return new Block(settings.registryKey(key)); }
}
