package com.denchy.serverrack;

import com.denchy.serverrack.registry.ModBlockEntities;
import com.denchy.serverrack.registry.ModBlocks;
import com.denchy.serverrack.registry.ModItemGroups;
import com.denchy.serverrack.registry.ModItems;
import com.denchy.serverrack.registry.ModNetworking;
import com.denchy.serverrack.registry.ModParticles;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Server Rack - Fabric 1.21.1
 * Multiblock animated server racks + dynamic ceiling smoke.
 * Made by ehalo.
 */
public class ServerRackMod implements ModInitializer {
    public static final String MOD_ID = "serverrack";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("[Server Rack] Initializing...");
        ModParticles.register();
        ModBlocks.register();
        ModItems.register();
        ModBlockEntities.register();
        ModItemGroups.register();
        ModNetworking.registerServer();
        LOGGER.info("[Server Rack] Ready.");
    }
}
