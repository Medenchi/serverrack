package com.denchy.serverrack.command;

import com.denchy.serverrack.client.screen.NukeConfigScreen;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;

public class GribCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("grib")
                    .executes(context -> {
                        var player = context.getSource().getPlayer();
                        if (player != null) {
                            // Открываем экран настроек ядерного гриба
                            player.getServer().execute(() -> {
                                // В реальной игре нужно отправлять пакет на клиент
                                player.sendMessage(Text.literal("§eОткрывается меню ядерного гриба..."), false);
                            });
                        }
                        return 1;
                    }));
        });
    }
}