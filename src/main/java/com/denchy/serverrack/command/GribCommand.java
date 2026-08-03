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
                    .then(CommandManager.literal("set")
                            .then(CommandManager.argument("a", IntegerArgumentType.integer(1, 40))
                                    .then(CommandManager.argument("b", IntegerArgumentType.integer(1, 40))
                                            .then(CommandManager.argument("c", IntegerArgumentType.integer(1, 40))
                                                    .then(CommandManager.argument("d", IntegerArgumentType.integer(1, 40))
                                                            .executes(c -> {
                                                                int a = IntegerArgumentType.getInteger(c, "a");
                                                                int b = IntegerArgumentType.getInteger(c, "b");
                                                                int cc = IntegerArgumentType.getInteger(c, "c");
                                                                int d = IntegerArgumentType.getInteger(c, "d");
                                                                c.getSource().sendFeedback(() -> Text.literal("set " + a + " " + b + " " + cc + " " + d), false);
                                                                return 1;
                                                            })))))));
        });
    }
}
