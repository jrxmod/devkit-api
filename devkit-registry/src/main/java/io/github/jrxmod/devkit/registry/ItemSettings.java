package io.github.jrxmod.devkit.registry;

import net.minecraft.component.type.FoodComponent;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Rarity;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

public final class ItemSettings {
    private Item.Settings settings;
    private ItemSettings(Item.Settings settings) { this.settings = Objects.requireNonNull(settings, "settings"); }
    public static ItemSettings of() { return new ItemSettings(new Item.Settings()); }
    public ItemSettings maxCount(int maxCount) { settings = settings.maxCount(maxCount); return this; }
    public ItemSettings maxDamage(int maxDamage) { settings = settings.maxDamage(maxDamage); return this; }
    public ItemSettings fireproof() { settings = settings.fireproof(); return this; }
    public ItemSettings recipeRemainder(Item remainder) { settings = settings.recipeRemainder(remainder); return this; }
    public ItemSettings rarity(Rarity rarity) { settings = settings.rarity(rarity); return this; }
    public ItemSettings food(FoodComponent foodComponent) { settings = settings.food(foodComponent); return this; }
    public Supplier<Item> buildSimple() { final Item.Settings c = this.settings; return () -> new Item(c); }
    public Function<RegistryKey<Item>, Item> buildKeyed() { final Item.Settings c = this.settings; return key -> ItemFactory.create(c, key); }
}
