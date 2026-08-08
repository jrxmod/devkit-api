package io.github.jrxmod.devkit.registry;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
final class ItemFactory {
    private ItemFactory() {}
    static Item create(Item.Settings settings, RegistryKey<Item> key) { return new Item(settings); }
}
