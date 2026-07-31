package com.denchy.serverrack.client;

import com.denchy.serverrack.client.render.ServerRackBlockEntityRenderer;
import com.denchy.serverrack.client.screen.SmokeConfigScreen;
import com.denchy.serverrack.network.payload.ClearSmokeSyncPayload;
import com.denchy.serverrack.network.payload.SmokeConfigSyncPayload;
import com.denchy.serverrack.network.payload.TogglePcPayload;
import com.denchy.serverrack.network.payload.ToggleRackPayload;
import com.denchy.serverrack.particle.CeilingSmokeParticle;
import com.denchy.serverrack.registry.ModBlockEntities;
import com.denchy.serverrack.registry.ModBlocks;
import com.denchy.serverrack.registry.ModParticles;
import com.denchy.serverrack.smoke.CeilingSmokeManager;
import com.denchy.serverrack.smoke.SmokeConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.lwjgl.glfw.GLFW;

public class ServerRackClient implements ClientModInitializer {

    public static KeyBinding TOGGLE_KEY;
    public static KeyBinding MENU_KEY;
    public static KeyBinding PC_ALERT_KEY;

    /** Radius in blocks for J (racks) and Z (PC walls) toggles. */
    public static final int TOGGLE_RADIUS = 32;

    @Override
    public void onInitializeClient() {
        // Particle
        ParticleFactoryRegistry.getInstance().register(ModParticles.CEILING_SMOKE, CeilingSmokeParticle.Factory::new);

        // BER
        BlockEntityRendererRegistry.register(ModBlockEntities.SERVER_RACK, ServerRackBlockEntityRenderer::new);

        // Networking S2C
        ClientPlayNetworking.registerGlobalReceiver(SmokeConfigSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                SmokeConfig.set(payload.frequency(), payload.count(), payload.radius(), payload.rise(), payload.density());
            });
        });

        ClientPlayNetworking.registerGlobalReceiver(ClearSmokeSyncPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                CeilingSmokeManager.clearAll();
                // Optional message
                if (context.client().player != null) {
                    context.client().player.sendMessage(Text.literal("§7[ServerRack] Дым очищен"), false);
                }
            });
        });

        // Keybinds
        TOGGLE_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.serverrack.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_J,
                "category.serverrack"
        ));
        MENU_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.serverrack.menu",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_U,
                "category.serverrack"
        ));
        PC_ALERT_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.serverrack.pc_alert",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_Z,
                "category.serverrack"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) return;

            // Tick smoke manager
            CeilingSmokeManager.get(client.world).tick(client.world);

            // J = ALL racks in TOGGLE_RADIUS
            while (TOGGLE_KEY.wasPressed()) {
                handleToggle(client);
            }
            // Z = ALL pc walls in TOGGLE_RADIUS -> СЕРВЕРАМ ПИЗДА mode
            while (PC_ALERT_KEY.wasPressed()) {
                handlePcAlert(client);
            }
            while (MENU_KEY.wasPressed()) {
                client.setScreen(new SmokeConfigScreen());
            }
        });
    }

    private void handleToggle(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        BlockPos playerPos = client.player.getBlockPos();
        int found = countNearby(client, playerPos, true);

        ClientPlayNetworking.send(new ToggleRackPayload(playerPos, TOGGLE_RADIUS));

        if (found == 0) {
            client.player.sendMessage(Text.literal("§c[ServerRack] Стойки не найдены в радиусе " + TOGGLE_RADIUS + " блоков"), true);
        } else {
            client.player.sendMessage(Text.literal("§7[ServerRack] Стойки в радиусе " + TOGGLE_RADIUS + ": §f" + found), true);
        }
    }

    private void handlePcAlert(MinecraftClient client) {
        if (client.player == null || client.world == null) return;
        BlockPos playerPos = client.player.getBlockPos();
        int found = countNearby(client, playerPos, false);

        ClientPlayNetworking.send(new TogglePcPayload(playerPos, TOGGLE_RADIUS));

        if (found == 0) {
            client.player.sendMessage(Text.literal("§c[ServerRack] ПК-стены не найдены в радиусе " + TOGGLE_RADIUS + " блоков"), true);
        } else {
            client.player.sendMessage(Text.literal("§4[ServerRack] СЕРВЕРАМ ПИЗДА §c(пк-стен в радиусе " + TOGGLE_RADIUS + ": " + found + ")"), true);
        }
    }

    /** Client-side pre-scan so we can show feedback; server does the authoritative pass anyway. */
    private int countNearby(MinecraftClient client, BlockPos playerPos, boolean racks) {
        BlockPos min = playerPos.add(-TOGGLE_RADIUS, -TOGGLE_RADIUS, -TOGGLE_RADIUS);
        BlockPos max = playerPos.add(TOGGLE_RADIUS, TOGGLE_RADIUS, TOGGLE_RADIUS);
        int count = 0;
        for (BlockPos p : BlockPos.iterate(min, max)) {
            var s = client.world.getBlockState(p);
            boolean match = racks ? ModBlocks.isRack(s.getBlock()) : ModBlocks.isPc(s.getBlock());
            if (!match) continue;
            if (racks) {
                var info = ModBlocks.getLowerInfo(s, p);
                if (info == null || !info.lowerPos().equals(p)) continue; // count each rack once
            }
            count++;
        }
        return count;
    }
}
