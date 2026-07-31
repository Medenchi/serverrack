package com.denchy.serverrack.smoke;

import com.denchy.serverrack.registry.ModParticles;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks ceiling smoke density.
 * Client-side accumulation that spreads along ceilings.
 * Server-side could be used for sync but we keep client authoritative for visuals.
 */
public class CeilingSmokeManager {

    private static final Map<World, CeilingSmokeManager> INSTANCES = new WeakHashMap<>();

    public static CeilingSmokeManager get(World world) {
        return INSTANCES.computeIfAbsent(world, w -> new CeilingSmokeManager());
    }

    // Map ceiling pos -> density
    private final Map<BlockPos, Integer> densityMap = new ConcurrentHashMap<>();
    private final Map<BlockPos, Integer> ageMap = new ConcurrentHashMap<>();

    private int tickCounter = 0;

    public void clear() {
        densityMap.clear();
        ageMap.clear();
    }

    public Map<BlockPos, Integer> getDensityMap() {
        return Collections.unmodifiableMap(densityMap);
    }

    /**
     * Add smoke from a rack. Finds ceiling above rack and spreads.
     */
    public void addSmokeFromRack(World world, BlockPos lowerPos, BlockPos topPos) {
        Optional<BlockPos> ceilingOpt = findCeiling(world, topPos);
        if (ceilingOpt.isEmpty()) return;

        BlockPos ceilingPos = ceilingOpt.get();
        addDensity(ceilingPos, SmokeConfig.particlesPerEmit);

        // BFS spread
        int radius = SmokeConfig.ceilingRadius;
        int maxDensity = SmokeConfig.maxDensity;

        Queue<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        Map<BlockPos, Integer> distMap = new HashMap<>();

        queue.add(ceilingPos);
        visited.add(ceilingPos);
        distMap.put(ceilingPos, 0);

        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            int dist = distMap.getOrDefault(cur, 0);
            if (dist >= radius) continue;

            int curDensity = densityMap.getOrDefault(cur, 0);
            // Spread only if current density is high enough
            if (curDensity < 5 && dist > 0) continue;

            for (BlockPos neighbor : neighbors(cur)) {
                if (visited.contains(neighbor)) continue;
                if (!isCeiling(world, neighbor)) continue;
                int ndist = dist + 1;
                if (ndist > radius) continue;
                visited.add(neighbor);
                distMap.put(neighbor, ndist);
                // Spread amount decreases with distance
                int spreadAmount = Math.max(1, (maxDensity - ndist * 3) / 8);
                // Only add small amount to neighbors
                if (world.random.nextFloat() < 0.6f) {
                    addDensity(neighbor, spreadAmount);
                }
                queue.add(neighbor);
            }
        }
    }

    private List<BlockPos> neighbors(BlockPos pos) {
        return List.of(
                pos.north(),
                pos.south(),
                pos.east(),
                pos.west()
        );
    }

    private void addDensity(BlockPos pos, int amount) {
        BlockPos immutable = pos.toImmutable();
        densityMap.merge(immutable, amount, Integer::sum);
        ageMap.put(immutable, 0);
        // Clamp
        int cur = densityMap.get(immutable);
        if (cur > SmokeConfig.maxDensity) {
            densityMap.put(immutable, SmokeConfig.maxDensity);
        }
    }

    public Optional<BlockPos> findCeiling(World world, BlockPos start) {
        // Start from top of rack +1, go up maxRiseHeight
        for (int y = 1; y <= SmokeConfig.maxRiseHeight; y++) {
            BlockPos checkAir = start.up(y);
            if (checkAir.getY() >= world.getTopY()) break;
            BlockPos above = checkAir.up();
            if (world.getBlockState(checkAir).isAir() && isSolidCeiling(world, above)) {
                return Optional.of(checkAir.toImmutable());
            }
        }
        // Also check if there's ceiling already in map near? no
        return Optional.empty();
    }

    public boolean isCeiling(World world, BlockPos airPos) {
        if (!world.getBlockState(airPos).isAir()) return false;
        return isSolidCeiling(world, airPos.up());
    }

    public boolean isSolidCeiling(World world, BlockPos solidPos) {
        var state = world.getBlockState(solidPos);
        // Consider opaque full cube or solid blocks
        return !state.isAir() && state.isOpaqueFullCube(world, solidPos);
    }

    /**
     * Called each client tick for world - handles decay and visual particle spawning at ceiling
     */
    public void tick(World world) {
        tickCounter++;
        // Decay every 20 ticks (1 second)
        if (tickCounter % 20 == 0) {
            Iterator<Map.Entry<BlockPos, Integer>> it = densityMap.entrySet().iterator();
            while (it.hasNext()) {
                var entry = it.next();
                BlockPos pos = entry.getKey();
                int dens = entry.getValue();
                // If no longer ceiling (block broken or changed), decay faster
                if (!isCeiling(world, pos)) {
                    dens -= 8;
                } else {
                    dens -= 2; // slow decay
                }
                if (dens <= 0) {
                    it.remove();
                    ageMap.remove(pos);
                } else {
                    entry.setValue(dens);
                    ageMap.merge(pos, 1, Integer::sum);
                }
            }
        }

        // Spawn ceiling drift particles occasionally based on density
        if (!world.isClient) return;
        if (tickCounter % 6 != 0) return;

        for (var entry : densityMap.entrySet()) {
            BlockPos pos = entry.getKey();
            int dens = entry.getValue();
            if (dens < 2) continue;

            double x = pos.getX() + 0.5 + (world.random.nextDouble() - 0.5) * 0.8;
            double y = pos.getY() + 0.15 + world.random.nextDouble() * 0.2;
            double z = pos.getZ() + 0.5 + (world.random.nextDouble() - 0.5) * 0.8;

            // Probability based on density
            float chance = Math.min(0.8f, dens / (float)SmokeConfig.maxDensity * 0.5f + 0.1f);
            if (world.random.nextFloat() > chance) continue;

            double vx = (world.random.nextDouble() - 0.5) * 0.015;
            double vy = (world.random.nextDouble() - 0.5) * 0.005;
            double vz = (world.random.nextDouble() - 0.5) * 0.015;

            try {
                world.addParticle(ModParticles.CEILING_SMOKE, x, y, z, vx, vy, vz);
            } catch (Exception e) {
                // fallback
            }
        }
    }

    // Static tick handler called from client world tick event
    public static void tickAll() {
        // tick each world instance
        for (var entry : INSTANCES.entrySet()) {
            World w = entry.getKey();
            if (w != null) {
                entry.getValue().tick(w);
            }
        }
    }

    public static void clearAll() {
        for (var mgr : INSTANCES.values()) {
            mgr.clear();
        }
    }
}
