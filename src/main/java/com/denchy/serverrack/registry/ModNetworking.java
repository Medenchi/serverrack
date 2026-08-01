package com.denchy.serverrack.registry;

import com.denchy.serverrack.network.payload.*;
import com.denchy.serverrack.smoke.SmokeConfig;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;

public final class ModNetworking {
    private ModNetworking() {}

    public static void registerServer() {
        // Register C2S
        PayloadTypeRegistry.playC2S().register(SmokeConfigPayload.ID, SmokeConfigPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ToggleRackPayload.ID, ToggleRackPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(TogglePcPayload.ID, TogglePcPayload.CODEC);
        PayloadTypeRegistry.playC2S().register(ClearSmokePayload.ID, ClearSmokePayload.CODEC);

        // Register S2C
        PayloadTypeRegistry.playS2C().register(SmokeConfigSyncPayload.ID, SmokeConfigSyncPayload.CODEC);
        PayloadTypeRegistry.playS2C().register(ClearSmokeSyncPayload.ID, ClearSmokeSyncPayload.CODEC);

        // Handlers
        ServerPlayNetworking.registerGlobalReceiver(SmokeConfigPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                SmokeConfig.set(payload.frequency(), payload.count(), payload.radius(), payload.rise(), payload.density());
                // broadcast to all
                var sync = SmokeConfigSyncPayload.from(payload);
                for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
                    ServerPlayNetworking.send(p, sync);
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ToggleRackPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var world = context.player().getWorld();
                ModBlocks.toggleRacksInRadius(world, payload.pos(), payload.radius());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(TogglePcPayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                var world = context.player().getWorld();
                ModBlocks.togglePcsInRadius(world, payload.pos(), payload.radius());
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(ClearSmokePayload.ID, (payload, context) -> {
            context.server().execute(() -> {
                // broadcast clear to all
                var clear = new ClearSmokeSyncPayload();
                for (ServerPlayerEntity p : context.server().getPlayerManager().getPlayerList()) {
                    ServerPlayNetworking.send(p, clear);
                }
                // Optionally deactivate all racks in this world? We keep active but clear visual.
                // If want to deactivate all, uncomment:
                // deactivateAllInWorld(context.player().getWorld());
            });
        });

        // On join, sync current config
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            var sync = new SmokeConfigSyncPayload(
                    SmokeConfig.frequencyTicks,
                    SmokeConfig.particlesPerEmit,
                    SmokeConfig.ceilingRadius,
                    SmokeConfig.maxRiseHeight,
                    SmokeConfig.maxDensity
            );
            sender.sendPacket(sync);
        });
    }
}
