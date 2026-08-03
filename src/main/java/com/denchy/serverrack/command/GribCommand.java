package com.denchy.serverrack.command;

import com.denchy.serverrack.det.NukeConfig;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class GribCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("grib")
                    .executes(ctx -> {
                        ctx.getSource().sendFeedback(() -> Text.literal("stem=" + NukeConfig.stemHeight), false);
                        return 1;
                    })
                    .then(CommandManager.literal("stem")
                            .then(CommandManager.argument("value", IntegerArgumentType.integer(1, 40))
                                    .executes(c -> {
                                        int v = IntegerArgumentType.getInteger(c, "value");
                                        NukeConfig.stemHeight = v;
                                        c.getSource().sendFeedback(() -> Text.literal("stem=" + v), false);
                                        return 1;
                                    }))));
        });
    }
}
