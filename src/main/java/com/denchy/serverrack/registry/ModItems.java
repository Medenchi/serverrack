package com.denchy.serverrack.registry;

import com.denchy.serverrack.ModId;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

import java.util.ArrayList;
import java.util.List;

public final class ModItems {
    private ModItems() {}

    public static final List<Item> RACK_ITEMS = new ArrayList<>();

    public static void register() {
        for (Block block : ModBlocks.RACKS) {
            var id = Registries.BLOCK.getId(block);
            BlockItem item = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, item);
            RACK_ITEMS.add(item);
        }
    }
}
