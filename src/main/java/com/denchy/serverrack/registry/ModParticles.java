package com.denchy.serverrack.registry;

import com.denchy.serverrack.ModId;
import com.denchy.serverrack.particle.CeilingSmokeParticle;
import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public final class ModParticles {
    private ModParticles() {}

    public static SimpleParticleType CEILING_SMOKE;

    // Nuke mushroom bricks (textures in textures/particle, factories registered client-side)
    public static SimpleParticleType NUKE_CAP;
    public static SimpleParticleType NUKE_STEM;
    public static SimpleParticleType NUKE_FIRE;
    public static SimpleParticleType NUKE_RING;
    public static SimpleParticleType NUKE_SMOKE;
    public static SimpleParticleType NUKE_SPARK;
    public static SimpleParticleType NUKE_GLOW;

    public static void register() {
        CEILING_SMOKE = Registry.register(
                Registries.PARTICLE_TYPE,
                ModId.of("ceiling_smoke"),
                FabricParticleTypes.simple()
        );
        NUKE_CAP = Registry.register(Registries.PARTICLE_TYPE, ModId.of("nuke_cap"), FabricParticleTypes.simple());
        NUKE_STEM = Registry.register(Registries.PARTICLE_TYPE, ModId.of("nuke_stem"), FabricParticleTypes.simple());
        NUKE_FIRE = Registry.register(Registries.PARTICLE_TYPE, ModId.of("nuke_fire"), FabricParticleTypes.simple());
        NUKE_RING = Registry.register(Registries.PARTICLE_TYPE, ModId.of("nuke_ring"), FabricParticleTypes.simple());
        NUKE_SMOKE = Registry.register(Registries.PARTICLE_TYPE, ModId.of("nuke_smoke"), FabricParticleTypes.simple());
        NUKE_SPARK = Registry.register(Registries.PARTICLE_TYPE, ModId.of("nuke_spark"), FabricParticleTypes.simple());
        NUKE_GLOW = Registry.register(Registries.PARTICLE_TYPE, ModId.of("nuke_glow"), FabricParticleTypes.simple());
    }

    // helper for client registration of factory is done in client mod initializer
}
