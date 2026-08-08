package io.github.jrxmod.devkit.registry;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.registry.RegistryKey;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class BlockSettings {
    private AbstractBlock.Settings settings;
    private BlockSettings(AbstractBlock.Settings settings) { this.settings = Objects.requireNonNull(settings, "settings"); }
    public static BlockSettings of() { return new BlockSettings(AbstractBlock.Settings.create()); }
    public BlockSettings strength(float hardness, float resistance) { settings = settings.strength(hardness, resistance); return this; }
    public BlockSettings requiresTool() { settings = settings.requiresTool(); return this; }
    public BlockSettings nonOpaque() { settings = settings.nonOpaque(); return this; }
    public BlockSettings burnable() { settings = settings.burnable(); return this; }
    public BlockSettings dropsNothing() { settings = settings.dropsNothing(); return this; }
    public Supplier<Block> buildSimple() { final AbstractBlock.Settings c = this.settings; return () -> new Block(c); }
    public Function<RegistryKey<Block>, Block> buildKeyed() { final AbstractBlock.Settings c = this.settings; return key -> BlockFactory.create(c, key); }
}
