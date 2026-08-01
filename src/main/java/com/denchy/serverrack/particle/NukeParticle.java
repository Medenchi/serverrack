package com.denchy.serverrack.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.world.ClientWorld;
import org.jetbrains.annotations.Nullable;

/**
 * One generic mushroom-building-brick: drifts slightly, grows, fades out.
 * The seven NUKE_* particle types share this behaviour; their looks differ
 * only by texture.
 */
public class NukeParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;

    protected NukeParticle(ClientWorld world, double x, double y, double z,
                           double vx, double vy, double vz,
                           SpriteProvider spriteProvider) {
        super(world, x, y, z, vx, vy, vz);
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
        this.maxAge = 30 + world.random.nextInt(50);
        this.scale = 0.9f + world.random.nextFloat() * 1.7f;
        this.collidesWithWorld = false;
        this.spriteProvider = spriteProvider;
        this.setSpriteForAge(spriteProvider);
    }

    @Override
    public void tick() {
        this.prevPosX = this.x;
        this.prevPosY = this.y;
        this.prevPosZ = this.z;
        if (this.age++ >= this.maxAge) {
            this.markDead();
            return;
        }
        float life = (float) this.age / (float) this.maxAge;
        // slow the drift, fatten the blob, fade out at the end of life
        this.velocityX *= 0.98;
        this.velocityY = this.velocityY * 0.98 + 0.004;
        this.velocityZ *= 0.98;
        this.move(this.velocityX, this.velocityY, this.velocityZ);
        this.scale += 0.012f;
        this.alpha = 0.95f * (1.0f - life * life);
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
        public Particle createParticle(SimpleParticleType parameters, ClientWorld world,
                                       double x, double y, double z, double vx, double vy, double vz) {
            return new NukeParticle(world, x, y, z, vx, vy, vz, spriteProvider);
        }
    }
}
