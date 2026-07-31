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

    public static void register() {
        CEILING_SMOKE = Registry.register(
                Registries.PARTICLE_TYPE,
                ModId.of("ceiling_smoke"),
                FabricParticleTypes.simple()
        );
    }

    // helper for client registration of factory is done in client mod initializer
}
