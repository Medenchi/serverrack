package com.denchy.serverrack.client;

import com.denchy.serverrack.client.render.ServerRackBlockEntityRenderer;
import com.denchy.serverrack.client.screen.SmokeConfigScreen;
import com.denchy.serverrack.network.payload.ClearSmokeSyncPayload;
import com.denchy.serverrack.network.payload.SmokeConfigSyncPayload;
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

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.world == null || client.player == null) return;

            // Tick smoke manager
            CeilingSmokeManager.get(client.world).tick(client.world);

            // Handle toggle key
            while (TOGGLE_KEY.wasPressed()) {
                handleToggle(client);
            }
            while (MENU_KEY.wasPressed()) {
                client.setScreen(new SmokeConfigScreen());
            }
        });
    }

    private void handleToggle(MinecraftClient client) {
        if (client.player == null) return;
        HitResult hit = client.crosshairTarget;
        BlockPos targetPos = null;

        if (hit instanceof BlockHitResult blockHit) {
            if (blockHit.getType() == HitResult.Type.BLOCK) {
                BlockPos pos = blockHit.getBlockPos();
                var state = client.world.getBlockState(pos);
                if (ModBlocks.isRack(state.getBlock())) {
                    targetPos = pos;
                }
            }
        }

        // If no rack under crosshair, try near player (within 5 blocks) looking for active racks
        if (targetPos == null) {
            BlockPos playerPos = client.player.getBlockPos();
            double closestDist = 6.0;
            BlockPos closest = null;
            for (BlockPos p : BlockPos.iterateOutwards(playerPos, 5, 3, 5)) {
                var s = client.world.getBlockState(p);
                if (!ModBlocks.isRack(s.getBlock())) continue;
                double d = Math.sqrt(p.getSquaredDistance(playerPos));
                if (d < closestDist) {
                    closestDist = d;
                    closest = p.toImmutable();
                }
            }
            targetPos = closest;
        }

        if (targetPos != null) {
            ClientPlayNetworking.send(new ToggleRackPayload(targetPos));
        } else {
            client.player.sendMessage(Text.literal("§c[ServerRack] Стойка не найдена в радиусе 5 блоков"), true);
        }
    }
}
