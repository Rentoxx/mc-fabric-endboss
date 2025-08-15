package tech.tguentner.item;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import tech.tguentner.AssasinEndboss;

public class ModItems {
    // ÄNDERUNG HIER: Verwende deine neue Klasse "MagischerStabItem"
    public static final Item MAGISCHER_STAB = register("magischer_stab", new MagischerStabItem(new Item.Settings()));

    public static final Item DEBUG_ICON = register("debug_icon", new Item(new Item.Settings()));

    private static Item register(String path, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(AssasinEndboss.MOD_ID, path), item);
    }

    private static void addItemsToCombatTab(FabricItemGroupEntries entries) {
        entries.add(MAGISCHER_STAB);
        entries.add(DEBUG_ICON);
    }

    public static void registerModItems() {
        AssasinEndboss.LOGGER.info("Registering Mod Items for {}", AssasinEndboss.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.COMBAT).register(ModItems::addItemsToCombatTab);
    }
}