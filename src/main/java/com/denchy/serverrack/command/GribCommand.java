package com.denchy.serverrack.command;

import com.denchy.serverrack.det.NukeConfig;
import com.denchy.serverrack.network.payload.NukeConfigSyncPayload;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import java.util.List;

public class GribCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("grib")
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(() -> Text.literal("ok"), false);
                        return 1;
                    })
                    .then(CommandManager.literal("stem")
                            .then(CommandManager.argument("value", IntegerArgumentType.integer(NukeConfig.STEM_MIN, NukeConfig.STEM_MAX))
                                    .executes(c -> {
                                        int v = IntegerArgumentType.getInteger(c, "value");
                                        NukeConfig.set(v, NukeConfig.capRadius, NukeConfig.ringRadius, NukeConfig.density);
                                        broadcast(c.getSource().getServer().getPlayerManager().getPlayerList());
                                        c.getSource().sendFeedback(() -> Text.literal("stem=" + v), false);
                                        return 1;
                                    })))
                    .then(CommandManager.literal("cap")
                            .then(CommandManager.argument("value", IntegerArgumentType.integer(NukeConfig.CAP_MIN, NukeConfig.CAP_MAX))
                                    .executes(c -> {
                                        int v = IntegerArgumentType.getInteger(c, "value");
                                        NukeConfig.set(NukeConfig.stemHeight, v, NukeConfig.ringRadius, NukeConfig.density);
                                        broadcast(c.getSource().getServer().getPlayerManager().getPlayerList());
                                        c.getSource().sendFeedback(() -> Text.literal("cap=" + v), false);
                                        return 1;
                                    })))
                    .then(CommandManager.literal("ring")
                            .then(CommandManager.argument("value", IntegerArgumentType.integer(NukeConfig.RING_MIN, NukeConfig.RING_MAX))
                                    .executes(c -> {
                                        int v = IntegerArgumentType.getInteger(c, "value");
                                        NukeConfig.set(NukeConfig.stemHeight, NukeConfig.capRadius, v, NukeConfig.density);
                                        broadcast(c.getSource().getServer().getPlayerManager().getPlayerList());
                                        c.getSource().sendFeedback(() -> Text.literal("ring=" + v), false);
                                        return 1;
                                    })))
                    .then(CommandManager.literal("density")
                            .then(CommandManager.argument("value", IntegerArgumentType.integer(NukeConfig.DENSITY_MIN, NukeConfig.DENSITY_MAX))
                                    .executes(c -> {
                                        int v = IntegerArgumentType.getInteger(c, "value");
                                        NukeConfig.set(NukeConfig.stemHeight, NukeConfig.capRadius, NukeConfig.ringRadius, v);
                                        broadcast(c.getSource().getServer().getPlayerManager().getPlayerList());
                                        c.getSource().sendFeedback(() -> Text.literal("density=" + v), false);
                                        return 1;
                                    })))
                    .then(CommandManager.literal("set")
                            .then(CommandManager.argument("stem", IntegerArgumentType.integer(NukeConfig.STEM_MIN, NukeConfig.STEM_MAX))
                                    .then(CommandManager.argument("cap", IntegerArgumentType.integer(NukeConfig.CAP_MIN, NukeConfig.CAP_MAX))
                                            .then(CommandManager.argument("ring", IntegerArgumentType.integer(NukeConfig.RING_MIN, NukeConfig.RING_MAX))
                                                    .then(CommandManager.argument("density", IntegerArgumentType.integer(NukeConfig.DENSITY_MIN, NukeConfig.DENSITY_MAX))
                                                            .executes(c -> {
                                                                int stem = IntegerArgumentType.getInteger(c, "stem");
                                                                int cap = IntegerArgumentType.getInteger(c, "cap");
                                                                int ring = IntegerArgumentType.getInteger(c, "ring");
                                                                int dens = IntegerArgumentType.getInteger(c, "density");
                                                                NukeConfig.set(stem, cap, ring, dens);
                                                                broadcast(c.getSource().getServer().getPlayerManager().getPlayerList());
                                                                c.getSource().sendFeedback(() -> Text.literal("set " + stem + " " + cap + " " + ring + " " + dens), false);
                                                                return 1;
                                                            }))))));
        });
    }
    private static void broadcast(List<ServerPlayerEntity> players) {
        NukeConfigSyncPayload sync = new NukeConfigSyncPayload(NukeConfig.stemHeight, NukeConfig.capRadius, NukeConfig.ringRadius, NukeConfig.density);
        for (ServerPlayerEntity p : players) ServerPlayNetworking.send(p, sync);
    }
}
