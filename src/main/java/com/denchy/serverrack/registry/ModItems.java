package com.denchy.serverrack.registry;

import com.denchy.serverrack.ModId;
import com.denchy.serverrack.det.DetonatorItem;
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

    // Director module
    public static final Item DETONATOR = registerItem("detonator",
            new DetonatorItem(new Item.Settings().maxCount(1)));
    public static final Item PAPER_SCRAP_A = registerItem("paper_scrap_a", new Item(new Item.Settings()));
    public static final Item PAPER_SCRAP_B = registerItem("paper_scrap_b", new Item(new Item.Settings()));
    public static final Item PAPER_SCRAP_C = registerItem("paper_scrap_c", new Item(new Item.Settings()));
    public static final Item PAPER_SCRAP_D = registerItem("paper_scrap_d", new Item(new Item.Settings()));
    public static final Item PAPER_SCRAP_E = registerItem("paper_scrap_e", new Item(new Item.Settings()));
    public static final Item PAPER_SCRAP_F = registerItem("paper_scrap_f", new Item(new Item.Settings()));

    private static <T extends Item> T registerItem(String name, T item) {
        Registry.register(Registries.ITEM, ModId.of(name), item);
        RACK_ITEMS.add(item);
        return item;
    }

    public static void register() {
        for (Block block : ModBlocks.RACKS) {
            var id = Registries.BLOCK.getId(block);
            BlockItem item = new BlockItem(block, new Item.Settings());
            Registry.register(Registries.ITEM, id, item);
            RACK_ITEMS.add(item);
        }
        // PC video wall
        BlockItem pcItem = new BlockItem(ModBlocks.PC_WALL, new Item.Settings());
        Registry.register(Registries.ITEM, ModId.of("pc_wall"), pcItem);
        RACK_ITEMS.add(pcItem);
    }
}
