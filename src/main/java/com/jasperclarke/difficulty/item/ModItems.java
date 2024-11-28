package com.jasperclarke.difficulty.item;

import com.jasperclarke.difficulty.Difficulty;
import com.jasperclarke.difficulty.item.custom.DifficultyToggleDisabledItem;
import com.jasperclarke.difficulty.item.custom.DifficultyToggleEnabledItem;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class ModItems {

    public static final Item PUTRID_FLESH = registerItem("putrid_flesh", new Item(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Difficulty.MOD_ID, "putrid_flesh"))).food(ModFoodComponents.PUTRID_FLESH, ModConsumableComponents.PUTRID_FLESH)));

    public static final Item DIFFICULTY_TOGGLE_ENABLED = registerItem("difficulty_toggle_enabled", new DifficultyToggleEnabledItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Difficulty.MOD_ID, "difficulty_toggle_enabled")))));

    public static final Item DIFFICULTY_TOGGLE_DISABLED = registerItem("difficulty_toggle_disabled", new DifficultyToggleDisabledItem(new Item.Settings()
            .registryKey(RegistryKey.of(RegistryKeys.ITEM, Identifier.of(Difficulty.MOD_ID, "difficulty_toggle_disabled")))));

    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, Identifier.of(Difficulty.MOD_ID, name), item);
    }

    public static void registerModItems() {
        Difficulty.LOGGER.info("Registering items for " + Difficulty.MOD_ID);

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.TOOLS).register(entries -> {
            entries.add(PUTRID_FLESH);
        });
    }
}
