package io.github.jrxmod.devkit.datagen;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricTagProvider;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

/**
 * Simplified tag datagen helper for Fabric 1.21.x.
 * <p>
 * Reduces boilerplate when creating common block/item tags
 * driven by DevKit registry containers.
 *
 * @author jrxmod
 * @since 0.1.0
 */
public abstract class TagHelper {

    /**
     * Base class for block tag generation with fluent helpers.
     */
    public static abstract class Blocks extends FabricTagProvider.BlockTagProvider {
        protected final String modId;

        public Blocks(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries, String modId) {
            super(output, registries);
            this.modId = modId;
        }

        /**
         * Creates a namespaced tag key quickly.
         *
         * @param path tag path without namespace
         * @return block tag key
         */
        protected TagKey<net.minecraft.block.Block> tag(String path) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of(modId, path));
        }

        /**
         * Creates a common (c:) convention tag key.
         *
         * @param path tag path
         * @return block tag key in c namespace
         */
        protected TagKey<net.minecraft.block.Block> cTag(String path) {
            return TagKey.of(RegistryKeys.BLOCK, Identifier.of("c", path));
        }
    }

    /**
     * Base class for item tag generation with fluent helpers.
     * <p>
     * Modern Fabric API (&ge; 0.73) requires a resolved
     * {@code BlockTagProvider} rather than a {@code CompletableFuture}.
     * The dependent mod's data entrypoint is responsible for
     * instantiating the block tag provider first and then passing it
     * into this constructor.
     */
    public static abstract class Items extends FabricTagProvider.ItemTagProvider {
        protected final String modId;

        public Items(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registries,
                     FabricTagProvider.BlockTagProvider blockTags, String modId) {
            super(output, registries, blockTags);
            this.modId = modId;
        }

        protected TagKey<net.minecraft.item.Item> tag(String path) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of(modId, path));
        }

        protected TagKey<net.minecraft.item.Item> cTag(String path) {
            return TagKey.of(RegistryKeys.ITEM, Identifier.of("c", path));
        }
    }
}
