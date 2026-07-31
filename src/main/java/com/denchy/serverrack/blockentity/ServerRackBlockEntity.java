package com.denchy.serverrack.blockentity;

import com.denchy.serverrack.registry.ModParticles;
import com.denchy.serverrack.registry.ModBlockEntities;
import com.denchy.serverrack.registry.ModBlocks;
import com.denchy.serverrack.smoke.CeilingSmokeManager;
import com.denchy.serverrack.smoke.SmokeConfig;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class ServerRackBlockEntity extends BlockEntity {

    private int tickCounter = 0;

    public ServerRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SERVER_RACK, pos, state);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, ServerRackBlockEntity be) {
        // Server just keeps tick counter for possible future use.
        // Smoke is client-side based on ACTIVE.
        // Could auto-clear smoke map here if deactivated, but client handles decay.
        be.tickCounter++;

        // Optional: if active, ensure chunk stays loaded? No.
        // No particle spawning on server.
    }

    public static void clientTick(World world, BlockPos pos, BlockState state, ServerRackBlockEntity be) {
        // Check ACTIVE property dynamically from state - because state passed is lower's state
        boolean active = false;
        var info = ModBlocks.getLowerInfo(state, pos);
        int height = 2;
        if (info != null) height = info.height();
        // Determine active from current lower state (might have been toggled)
        BlockState currentLower = world.getBlockState(pos);
        if (currentLower.contains(com.denchy.serverrack.block.ServerRackBlock.ACTIVE)) {
            active = currentLower.get(com.denchy.serverrack.block.ServerRackBlock.ACTIVE);
        } else if (currentLower.contains(com.denchy.serverrack.block.BigServerRackBlock.ACTIVE)) {
            active = currentLower.get(com.denchy.serverrack.block.BigServerRackBlock.ACTIVE);
        } else if (currentLower.contains(com.denchy.serverrack.block.MainframeRackBlock.ACTIVE)) {
            active = currentLower.get(com.denchy.serverrack.block.MainframeRackBlock.ACTIVE);
        } else {
            // fallback check passed state
            if (state.contains(com.denchy.serverrack.block.ServerRackBlock.ACTIVE)) active = state.get(com.denchy.serverrack.block.ServerRackBlock.ACTIVE);
            else if (state.contains(com.denchy.serverrack.block.BigServerRackBlock.ACTIVE)) active = state.get(com.denchy.serverrack.block.BigServerRackBlock.ACTIVE);
            else if (state.contains(com.denchy.serverrack.block.MainframeRackBlock.ACTIVE)) active = state.get(com.denchy.serverrack.block.MainframeRackBlock.ACTIVE);
        }

        if (!active) return;

        be.tickCounter++;
        if (be.tickCounter % SmokeConfig.frequencyTicks != 0) return;

        // Find top of rack
        BlockPos topPos = pos.up(height - 1);

        // Rising smoke particles from top
        if (world.random.nextFloat() < 0.9f) {
            double x = pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.6;
            double y = topPos.getY() + 1.0 + 0.1;
            double z = pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.6;

            // For mainframe, offset to cover wider area
            if (currentLower.getBlock() instanceof com.denchy.serverrack.block.MainframeRackBlock) {
                // spread a bit wider for mainframe
                x += (world.random.nextDouble() - 0.5) * 0.8;
                z += (world.random.nextDouble() - 0.5) * 0.8;
            }

            // Spawn rising smoke: use custom ceiling smoke but with upward velocity
            for (int i = 0; i < SmokeConfig.particlesPerEmit; i++) {
                double vx = (world.random.nextDouble() - 0.5) * 0.02;
                double vy = 0.08 + world.random.nextDouble() * 0.06;
                double vz = (world.random.nextDouble() - 0.5) * 0.02;
                // Try custom particle, fallback to campfire if not yet registered on client
                try {
                    world.addParticle(ModParticles.CEILING_SMOKE,
                            x + (world.random.nextDouble() - 0.5) * 0.3,
                            y,
                            z + (world.random.nextDouble() - 0.5) * 0.3,
                            vx, vy, vz);
                } catch (Exception e) {
                    world.addParticle(net.minecraft.particle.ParticleTypes.CAMPFIRE_COSY_SMOKE,
                            x, y, z, vx, vy, vz);
                }
            }
        }

        // Feed ceiling smoke manager - find ceiling and accumulate
        CeilingSmokeManager.get(world).addSmokeFromRack(world, pos, topPos);
    }
}
