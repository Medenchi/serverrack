package com.denchy.serverrack.registry;

import com.denchy.serverrack.ModId;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.text.Text;

public final class ModItemGroups {
    private ModItemGroups() {}

    public static final RegistryKey<ItemGroup> SERVER_RACK_GROUP =
            RegistryKey.of(RegistryKeys.ITEM_GROUP, ModId.of("server_racks"));

    public static void register() {
        ItemGroup group = ItemGroup.create(null, -1)
                .displayName(Text.translatable("itemGroup.serverrack.server_racks"))
                .icon(() -> new ItemStack(ModItems.RACK_ITEMS.isEmpty()
                        ? Items.REPEATER : ModItems.RACK_ITEMS.get(0)))
                .entries((context, entries) -> {
                    for (var item : ModItems.RACK_ITEMS) {
                        entries.add(item);
                    }
                })
                .build();
        Registry.register(Registries.ITEM_GROUP, SERVER_RACK_GROUP, group);
    }
}
