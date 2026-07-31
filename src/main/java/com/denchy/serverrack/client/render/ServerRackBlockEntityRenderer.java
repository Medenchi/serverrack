package com.denchy.serverrack.client.render;

import com.denchy.serverrack.blockentity.ServerRackBlockEntity;
import com.denchy.serverrack.registry.ModBlocks;
import net.minecraft.block.BlockState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;
import org.joml.Matrix4f;

public class ServerRackBlockEntityRenderer implements BlockEntityRenderer<ServerRackBlockEntity> {

    public ServerRackBlockEntityRenderer(BlockEntityRendererFactory.Context ctx) {}

    @Override
    public void render(ServerRackBlockEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay) {
        BlockState state = entity.getWorld() != null ? entity.getWorld().getBlockState(entity.getPos()) : entity.getCachedState();
        var info = ModBlocks.getLowerInfo(state, entity.getPos());
        if (info == null) return;

        boolean active = false;
        if (state.contains(com.denchy.serverrack.block.ServerRackBlock.ACTIVE)) active = state.get(com.denchy.serverrack.block.ServerRackBlock.ACTIVE);
        else if (state.contains(com.denchy.serverrack.block.BigServerRackBlock.ACTIVE)) active = state.get(com.denchy.serverrack.block.BigServerRackBlock.ACTIVE);
        else if (state.contains(com.denchy.serverrack.block.MainframeRackBlock.ACTIVE)) active = state.get(com.denchy.serverrack.block.MainframeRackBlock.ACTIVE);

        Direction facing = Direction.NORTH;
        if (state.contains(com.denchy.serverrack.block.ServerRackBlock.FACING)) facing = state.get(com.denchy.serverrack.block.ServerRackBlock.FACING);
        else if (state.contains(com.denchy.serverrack.block.BigServerRackBlock.FACING)) facing = state.get(com.denchy.serverrack.block.BigServerRackBlock.FACING);
        else if (state.contains(com.denchy.serverrack.block.MainframeRackBlock.FACING)) facing = state.get(com.denchy.serverrack.block.MainframeRackBlock.FACING);

        matrices.push();

        // Center at block
        matrices.translate(0.5, 0.5, 0.5);

        // Fan rotation - only if active
        if (active) {
            long time = entity.getWorld() != null ? entity.getWorld().getTime() : 0;
            float angle = (time % 360 + tickDelta) * 18.0f; // fast spin 18 deg per tick = 360 deg per second

            // Render fan in front
            matrices.push();
            // Move to front face
            float frontOffset = 0.45f;
            switch (facing) {
                case NORTH -> matrices.translate(0, 0, -frontOffset);
                case SOUTH -> matrices.translate(0, 0, frontOffset);
                case WEST -> matrices.translate(-frontOffset, 0, 0);
                case EAST -> matrices.translate(frontOffset, 0, 0);
                default -> matrices.translate(0, 0, -frontOffset);
            }

            // For big and mainframe, fans at different heights
            int h = info.height();
            // we are at center of lower block, so adjust y for fan position
            // For visualization, fan near top of lower block
            matrices.translate(0, 0.25, 0);

            // Rotate fan around its axis (perpendicular to facing)
            if (facing.getAxis() == Direction.Axis.Z) {
                matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(angle));
            } else {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(angle));
                matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
            }

            // render simple fan quad (2 blades crossing)
            VertexConsumer vc = vertexConsumers.getBuffer(RenderLayer.getEntityCutoutNoCull(com.denchy.serverrack.ModId.of("textures/block/fan_blade.png")));
            // If fan texture missing, fallback to solid color rendering via line? Use translucent.
            // We'll render quads manually
            renderFanBlades(matrices, vc, light, overlay, facing);

            matrices.pop();

            // Also render second fan for big rack and mainframe
            if (h >= 3 || state.getBlock() instanceof com.denchy.serverrack.block.MainframeRackBlock) {
                matrices.push();
                switch (facing) {
                    case NORTH -> matrices.translate(0, 1.0, -frontOffset);
                    case SOUTH -> matrices.translate(0, 1.0, frontOffset);
                    case WEST -> matrices.translate(-frontOffset, 1.0, 0);
                    case EAST -> matrices.translate(frontOffset, 1.0, 0);
                    default -> matrices.translate(0, 1.0, -frontOffset);
                }
                if (facing.getAxis() == Direction.Axis.Z) {
                    matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(-angle * 1.2f));
                } else {
                    matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(-angle * 1.2f));
                    matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(90));
                }
                renderFanBlades(matrices, vc, light, overlay, facing);
                matrices.pop();
            }
        }

        // LEDs blinking
        matrices.push();
        // Position LEDs near front top edge
        float front = 0.45f;
        switch (facing) {
            case NORTH -> matrices.translate(0, 0.35, -front - 0.01);
            case SOUTH -> matrices.translate(0, 0.35, front + 0.01);
            case WEST -> matrices.translate(-front - 0.01, 0.35, 0);
            case EAST -> matrices.translate(front + 0.01, 0.35, 0);
            default -> matrices.translate(0, 0.35, -front - 0.01);
        }

        // Blink logic
        long worldTime = entity.getWorld() != null ? entity.getWorld().getTime() : 0;
        boolean blinkFast = active && (worldTime % 10 < 5);
        boolean blinkSlow = active && (worldTime % 40 < 20);

        // If not active, dim LEDs
        renderLEDs(matrices, vertexConsumers, light, overlay, blinkFast, blinkSlow, active, facing);

        matrices.pop();

        matrices.pop();
    }

    private void renderFanBlades(MatrixStack matrices, VertexConsumer vc, int light, int overlay, Direction facing) {
        Matrix4f mat = matrices.peek().getPositionMatrix();
        // Simple cross: 2 quads
        // Blade size 0.3
        float s = 0.28f;
        // Use gray color with slight translucency
        // We will use entity translucent but just render white
        // Quad 1 horizontal
        // Since we use VertexConsumer with texture, UVs 0-1
        // We'll just use light 0xF000F0 for emissive? No.

        // Actually we need to get buffer with proper layer; for now we render with color

        // Blade 1
        vc.vertex(mat, -s, -0.02f, 0).color(180, 180, 180, 220).texture(0,0).overlay(overlay).light(light).normal(0,0,1).next();
        vc.vertex(mat, s, -0.02f, 0).color(180, 180, 180, 220).texture(1,0).overlay(overlay).light(light).normal(0,0,1).next();
        vc.vertex(mat, s, 0.02f, 0).color(180, 180, 180, 220).texture(1,1).overlay(overlay).light(light).normal(0,0,1).next();
        vc.vertex(mat, -s, 0.02f, 0).color(180, 180, 180, 220).texture(0,1).overlay(overlay).light(light).normal(0,0,1).next();

        // Blade 2 vertical
        vc.vertex(mat, -0.02f, -s, 0).color(160, 160, 160, 220).texture(0,0).overlay(overlay).light(light).normal(0,0,1).next();
        vc.vertex(mat, 0.02f, -s, 0).color(160, 160, 160, 220).texture(1,0).overlay(overlay).light(light).normal(0,0,1).next();
        vc.vertex(mat, 0.02f, s, 0).color(160, 160, 160, 220).texture(1,1).overlay(overlay).light(light).normal(0,0,1).next();
        vc.vertex(mat, -0.02f, s, 0).color(160, 160, 160, 220).texture(0,1).overlay(overlay).light(light).normal(0,0,1).next();
    }

    private void renderLEDs(MatrixStack matrices, VertexConsumerProvider vcp, int light, int overlay, boolean blinkFast, boolean blinkSlow, boolean active, Direction facing) {
        // Render 3 small colored squares - blue, green, red
        VertexConsumer vc = vcp.getBuffer(RenderLayer.getEntityTranslucentEmissive(com.denchy.serverrack.ModId.of("textures/block/led_overlay.png"), false));

        // If overlay missing, fallback to white translucent
        if (vc == null) vc = vcp.getBuffer(RenderLayer.getEntityTranslucent(com.denchy.serverrack.ModId.of("textures/block/server_rack_basic.png")));

        Matrix4f mat = matrices.peek().getPositionMatrix();
        float hs = 0.04f; // half size
        float spacing = 0.12f;

        // Positions for 3 LEDs
        float[][] offsets = new float[][]{
                {-spacing, 0},
                {0, 0},
                {spacing, 0}
        };
        int[][] colorsActive = new int[][]{
                {60, 140, 255}, // blue
                {60, 255, 120}, // green
                {255, 60, 60}   // red
        };
        int[][] colorsInactive = new int[][]{
                {20, 30, 50},
                {20, 40, 20},
                {50, 20, 20}
        };

        for (int i = 0; i < 3; i++) {
            float ox = offsets[i][0];
            float oy = offsets[i][1];
            int[] col;
            if (!active) col = colorsInactive[i];
            else {
                // blinking patterns
                if (i == 0) col = blinkFast ? colorsActive[i] : colorsInactive[i];
                else if (i == 1) col = blinkSlow ? colorsActive[i] : colorsInactive[i];
                else col = active ? colorsActive[i] : colorsInactive[i]; // red always on when active (power)
            }

            // Billboard facing front - we already translated to front, so just XY quad
            // Quad at 0,0
            float x0 = ox - hs;
            float x1 = ox + hs;
            float y0 = oy - hs;
            float y1 = oy + hs;

            // slight Z offset to avoid z-fighting
            float z = 0.001f * i;

            // Colors
            int r = col[0], g = col[1], b = col[2], a = active ? 255 : 120;

            // Render quad
            vc.vertex(mat, x0, y0, z).color(r, g, b, a).texture(0,0).overlay(overlay).light(0xF000F0).normal(0,0,1).next();
            vc.vertex(mat, x1, y0, z).color(r, g, b, a).texture(1,0).overlay(overlay).light(0xF000F0).normal(0,0,1).next();
            vc.vertex(mat, x1, y1, z).color(r, g, b, a).texture(1,1).overlay(overlay).light(0xF000F0).normal(0,0,1).next();
            vc.vertex(mat, x0, y1, z).color(r, g, b, a).texture(0,1).overlay(overlay).light(0xF000F0).normal(0,0,1).next();
        }
    }
}
