package com.denchy.serverrack.registry;

import com.denchy.serverrack.ModId;
import com.denchy.serverrack.blockentity.ServerRackBlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModBlockEntities {
    private ModBlockEntities() {}

    public static BlockEntityType<ServerRackBlockEntity> SERVER_RACK;

    public static void register() {
        SERVER_RACK = Registry.register(
                Registries.BLOCK_ENTITY_TYPE,
                ModId.of("server_rack"),
                BlockEntityType.Builder.create(
                        ServerRackBlockEntity::new,
                        ModBlocks.RACK_BASIC,
                        ModBlocks.RACK_ADVANCED,
                        ModBlocks.RACK_MAINFRAME
                ).build(null)
        );
    }
}
