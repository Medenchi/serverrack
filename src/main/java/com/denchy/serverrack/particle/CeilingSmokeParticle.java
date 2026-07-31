package com.denchy.serverrack.particle;

import net.minecraft.client.particle.*;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.BlockPos;
import org.jetbrains.annotations.Nullable;

/**
 * Realistic ceiling smoke:
 * - Rises up quickly
 * - When hits ceiling (solid block above), switches to horizontal drift
 * - Slowly expands and fades
 * - Affected by density (higher alpha when dense, but we handle alpha via manager)
 */
public class CeilingSmokeParticle extends SpriteBillboardParticle {

    private boolean hasHitCeiling = false;
    private int ceilingY = -1;
    private float horizontalSpeed = 0.0f;
    private float driftAngle = 0.0f;

    protected CeilingSmokeParticle(ClientWorld world, double x, double y, double z,
                                   double vx, double vy, double vz,
                                   SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
        this.scale *= 0.75f + world.random.nextFloat() * 0.75f;
        this.maxAge = 200 + world.random.nextInt(120); // long lived for accumulation
        this.setSpriteForAge(spriteProvider);
        this.spriteProvider = spriteProvider;
        this.collidesWithWorld = false;
        this.alpha = 0.45f;
        this.driftAngle = world.random.nextFloat() * 360.0f;
        this.horizontalSpeed = 0.01f + world.random.nextFloat() * 0.02f;
    }

    private final SpriteProvider spriteProvider;

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;

        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }

        this.setSpriteForAge(spriteProvider);

        // Physics
        if (!hasHitCeiling) {
            // Rise
            this.velocityY += 0.0015; // slight acceleration up
            this.velocityY *= 0.99;

            this.x += this.velocityX;
            this.y += this.velocityY;
            this.z += this.velocityZ;

            // Check for ceiling collision: look 0.2 above
            BlockPos above = BlockPos.ofFloored(this.x, this.y + 0.2, this.z);
            if (!this.world.getBlockState(above).isAir() && this.world.getBlockState(above).isOpaqueFullCube(world, above)) {
                hasHitCeiling = true;
                ceilingY = above.getY() - 1;
                // Switch to horizontal drift
                this.velocityY = (world.random.nextDouble() - 0.5) * 0.005;
                // pick random horizontal direction
                double angle = world.random.nextDouble() * Math.PI * 2.0;
                this.velocityX = Math.cos(angle) * horizontalSpeed;
                this.velocityZ = Math.sin(angle) * horizontalSpeed;
                this.y = ceilingY + 0.1 + world.random.nextDouble() * 0.2;
            }
        } else {
            // Ceiling drift - very slow horizontal
            this.driftAngle += 0.3f;
            // Add slight wobble
            this.velocityX += (world.random.nextDouble() - 0.5) * 0.0005;
            this.velocityZ += (world.random.nextDouble() - 0.5) * 0.0005;

            // Keep near ceiling Y
            if (ceilingY != -1) {
                double targetY = ceilingY + 0.15;
                if (this.y < targetY - 0.1) this.velocityY += 0.0005;
                if (this.y > targetY + 0.3) this.velocityY -= 0.0005;
            }
            this.velocityY *= 0.92;
            this.velocityX *= 0.995;
            this.velocityZ *= 0.995;

            this.x += this.velocityX;
            this.y += this.velocityY;
            this.z += this.velocityZ;

            // Slowly expand
            this.scale += 0.0015f;

            // Fade based on age
            float life = (float)age / (float)maxAge;
            this.alpha = 0.6f * (1.0f - life * 0.8f);
        }

        // Gravity tiny
        // this.velocityY -= 0.0001;
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
    }

    public static class Factory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public Factory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        @Nullable
        @Override
        public Particle createParticle(SimpleParticleType parameters, ClientWorld world, double x, double y, double z, double vx, double vy, double vz) {
            return new CeilingSmokeParticle(world, x, y, z, vx, vy, vz, spriteProvider);
        }
    }
}
