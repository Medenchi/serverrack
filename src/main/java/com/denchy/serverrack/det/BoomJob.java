package com.denchy.serverrack.det;

import com.denchy.serverrack.registry.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * One running detonation (or restoration). Ticked once per server tick.
 * Destruction never drops loot and never spills further than ~1 block past
 * the region edge (the movie-set safety rail).
 */
public class BoomJob {

    protected final ServerWorld world;
    protected final BlockPos min, max;
    protected final BoomType type;
    protected final float speed;
    protected final Vec3d center;

    /** Solid blocks captured at job start. */
    protected final List<BlockPos> blocks = new ArrayList<>();
    protected java.util.List<BlockPos> order;
    protected int cursor = 0;
    protected int tick = 0;
    public boolean done = false;

    public BoomJob(ServerWorld world, BlockPos min, BlockPos max, BoomType type, float speed) {
        this.world = world;
        this.min = min;
        this.max = max;
        this.type = type;
        this.speed = speed;
        this.center = new Vec3d((min.getX() + max.getX()) / 2.0 + 0.5,
                                (min.getY() + max.getY()) / 2.0 + 0.5,
                                (min.getZ() + max.getZ()) / 2.0 + 0.5);

        for (BlockPos p : BlockPos.iterate(min, max)) {
            BlockState s = world.getBlockState(p);
            if (s.isAir()) continue;
            if (s.getHardness(world, p) < 0) continue; // bedrock stays on set
            blocks.add(p.toImmutable());
        }

        switch (type) {
            case SHRAPNEL -> {
                order = new ArrayList<>(blocks);
                Collections.shuffle(order);
            }
            case PANCAKE -> {
                order = new ArrayList<>(blocks);
                order.sort(Comparator.comparingInt(BlockPos::getY));
            }
            case IMPLODE -> {
                order = new ArrayList<>(blocks);
                order.sort(Comparator.comparingDouble(p -> -p.getSquaredDistance(center)));
            }
            default -> order = null;
        }
    }

    public void step() {
        tick++;
        try {
            switch (type) {
                case SHOCKWAVE -> stepShockwave();
                case FUSE -> stepFuse();
                case PANCAKE -> stepPancake();
                case SHRAPNEL -> stepShrapnel();
                case TWO_STAGE -> stepTwoStage();
                case MELTDOWN -> stepMeltdown();
                case FLAK -> stepFlak();
                case IMPLODE -> stepImplode();
                case SWEEP -> stepSweep();
                case RAIN -> stepRain();
            }
        } catch (Exception e) {
            done = true; // never let a job wedge the ticker
        }
    }

    // ================================================================== core fx
    protected boolean destroy(BlockPos p, float fallingChance, Vec3d impulse) {
        BlockState s = world.getBlockState(p);
        if (s.isAir()) return false;
        if (s.getHardness(world, p) < 0) return false;

        BlockEntity be = world.getBlockEntity(p);
        if (be instanceof Inventory inv) inv.clear();

        // office paperwork must fly
        if (world.random.nextFloat() < 0.025) spawnPaper(p);

        world.spawnParticles(new BlockStateParticleEffect(ParticleTypes.BLOCK, s),
                p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                8, 0.35, 0.35, 0.35, 0.0);

        if (world.random.nextFloat() < fallingChance) {
            FallingBlockEntity fe = FallingBlockEntity.spawnFromBlock(world, p, s);
            if (impulse != null) fe.addVelocity(impulse);
            else fe.addVelocity((world.random.nextDouble() - 0.5) * 0.45,
                    0.25 + world.random.nextDouble() * 0.55,
                    (world.random.nextDouble() - 0.5) * 0.45);
        } else {
            world.setBlockState(p, Blocks.AIR.getDefaultState(), 3);
        }
        return true;
    }

    /** Destroy + small chance to chip the first block just OUTSIDE the set (=realism, agreed). */
    protected void destroyWithOverspill(BlockPos p, float fallingChance, Vec3d impulse) {
        destroy(p, fallingChance, impulse);
        if (world.random.nextFloat() > 0.10) return;
        List<BlockPos> out = new ArrayList<>();
        for (var d : net.minecraft.util.math.Direction.values()) {
            BlockPos q = p.offset(d);
            if (!inRegion(q)) out.add(q);
        }
        if (!out.isEmpty()) {
            BlockPos q = out.get(world.random.nextInt(out.size()));
            destroy(q, fallingChance * 0.5f, impulse);
        }
    }

    protected boolean inRegion(BlockPos p) {
        return p.getX() >= min.getX() && p.getX() <= max.getX()
                && p.getY() >= min.getY() && p.getY() <= max.getY()
                && p.getZ() >= min.getZ() && p.getZ() <= max.getZ();
    }

    protected void destroyCube(Vec3d c, double r, float fallingChance) {
        BlockPos lo = BlockPos.ofFloored(c.x - r, c.y - r, c.z - r);
        BlockPos hi = BlockPos.ofFloored(c.x + r, c.y + r, c.z + r);
        for (BlockPos p : BlockPos.iterate(lo, hi)) {
            if (!inRegion(p)) continue;
            if (p.getSquaredDistance(c) > r * r) continue;
            destroyWithOverspill(p.toImmutable(), fallingChance, null);
        }
        boomFx(c);
    }

    private void sound(Vec3d c, float vol, float pitch) {
        // NOTE: ENTITY_GENERIC_EXPLODE is RegistryEntry<SoundEvent> - the only
        // playSound overloads that accept a RegistryEntry are the double-coord ones.
        world.playSound(null, c.x, c.y, c.z, SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.BLOCKS, vol, pitch);
    }

    private void fireSound(Vec3d c, float vol, float pitch) {
        world.playSound(null, c.x, c.y, c.z, SoundEvents.BLOCK_FIRE_AMBIENT,
                SoundCategory.BLOCKS, vol, pitch);
    }

    protected void boomFx(Vec3d c) {
        world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y, c.z, 1, 0, 0, 0, 0);
        world.spawnParticles(ParticleTypes.FLAME, c.x, c.y, c.z, 30, 1.2, 1.2, 1.2, 0.02);
        world.spawnParticles(ParticleTypes.LARGE_SMOKE, c.x, c.y, c.z, 25, 1.0, 1.0, 1.0, 0.02);
        sound(c, 4.0f, 0.6f + world.random.nextFloat() * 0.5f);
    }

    protected void spawnPaper(BlockPos p) {
        Item[] scraps = {ModItems.PAPER_SCRAP_A, ModItems.PAPER_SCRAP_B, ModItems.PAPER_SCRAP_C};
        Item pick = scraps[world.random.nextInt(scraps.length)];
        ItemEntity it = new ItemEntity(world, p.getX() + 0.5, p.getY() + 0.8, p.getZ() + 0.5,
                new ItemStack(pick));
        it.setVelocity((world.random.nextDouble() - 0.5) * 0.35,
                0.25 + world.random.nextDouble() * 0.4,
                (world.random.nextDouble() - 0.5) * 0.35);
        world.spawnEntity(it);
    }

    // ================================================================== the ten
    private void stepShockwave() {
        double r = tick * 2.0 * speed;
        double maxDist = center.distanceTo(new Vec3d(min.getX(), min.getY(), min.getZ())) + 4;
        boolean hit = false;
        for (BlockPos p : blocks) {
            double d = p.getSquaredDistance(center);
            d = Math.sqrt(d);
            if (d >= r - 2.2 && d <= r) {
                Vec3d imp = new Vec3d(p.getX() + 0.5 - center.x, p.getY() + 0.5 - center.y,
                        p.getZ() + 0.5 - center.z).normalize().multiply(0.5);
                destroyWithOverspill(p, 0.18f, imp);
                hit = true;
            }
        }
        if (hit && tick % 2 == 0) {
            Vec3d shell = center.add(world.random.nextGaussian() * r * 0.6,
                    world.random.nextGaussian() * r * 0.35,
                    world.random.nextGaussian() * r * 0.6);
            world.spawnParticles(ParticleTypes.FLAME, shell.x, shell.y, shell.z, 20, 1.5, 1.5, 1.5, 0.01);
            sound(shell, 3.0f, 0.7f + world.random.nextFloat() * 0.4f);
        }
        if (tick % 4 == 0) {
            world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, center.x, center.y, center.z,
                    1, 0, 0, 0, 0);
        }
        if (r > maxDist) done = true;
    }

    private void stepFuse() {
        // serpentine path z-x-z through the region
        int stepTx = Math.max(2, (int) (4 / Math.max(0.25f, speed)));
        if (tick % stepTx != 0) return;
        int sx = max.getX() - min.getX() + 1;
        int n = (int) Math.ceil(sx / 4.0) * ((max.getZ() - min.getZ() + 1 + 5) / 6);
        int idx = tick / stepTx;
        if (idx > n) { done = true; return; }
        int perRow = Math.max(1, sx / 4);
        int row = idx / perRow;
        int col = idx % perRow;
        double x = min.getX() + 2 + col * 4.0;
        double z = min.getZ() + ((row % 2 == 0) ? 2 + (idx * 2.7) % Math.max(2, max.getZ() - min.getZ() - 2)
                : max.getZ() - 2 - (idx * 2.7) % Math.max(2, max.getZ() - min.getZ() - 2));
        double y = min.getY() + ((idx * 3.1) % Math.max(1, max.getY() - min.getY()));
        // spark run ahead of the blast
        world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, x, y + 0.5, z, 10, 0.4, 0.4, 0.4, 0.05);
        destroyCube(new Vec3d(x, y, z), 2.6, 0.15f);
    }

    private void stepPancake() {
        int per = Math.max(1, (int) (3 / Math.max(0.25f, speed)));
        if (tick % per != 0) return;
        int floor = tick / per;
        int y = min.getY() + floor;
        if (y > max.getY()) { done = true; return; }
        for (int x = min.getX(); x <= max.getX(); x++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                BlockPos p = new BlockPos(x, y, z);
                Vec3d down = new Vec3d((world.random.nextDouble() - 0.5) * 0.1, -0.6,
                        (world.random.nextDouble() - 0.5) * 0.1);
                destroyWithOverspill(p, 0.30f, down);
            }
        }
        // dust plumes out of the "windows"
        Vec3d edge = new Vec3d(min.getX() + world.random.nextInt(max.getX() - min.getX() + 1),
                y + 0.5, world.random.nextBoolean() ? min.getZ() - 0.2 : max.getZ() + 1.2);
        world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, edge.x, edge.y, edge.z,
                15, 0.4, 0.4, 0.4, 0.02);
        if (floor % 2 == 0) {
            sound(new Vec3d(center.x, y, center.z), 2.5f, 0.5f);
        }
    }

    private void stepShrapnel() {
        if (order == null) { done = true; return; }
        int batch = Math.max(16, (int) (blocks.size() / (60.0 * speed)));
        int end = Math.min(order.size(), cursor + batch);
        for (int i = cursor; i < end; i++) {
            destroyWithOverspill(order.get(i), 0.12f, null);
        }
        cursor = end;
        if (tick % 2 == 0) boomFx(center.add(world.random.nextGaussian() * 2,
                world.random.nextGaussian() * 2, world.random.nextGaussian() * 2));
        if (cursor >= order.size()) done = true;
    }

    private void stepTwoStage() {
        int phase1End = (int) (70 / Math.max(0.25f, speed));
        if (tick <= phase1End) {
            // fire + smoke crawl before the bang
            for (int i = 0; i < 10; i++) {
                BlockPos p = blocks.get(world.random.nextInt(blocks.size()));
                BlockPos up = p.up();
                world.spawnParticles(ParticleTypes.LARGE_SMOKE, p.getX() + 0.5, p.getY() + 1,
                        p.getZ() + 0.5, 3, 0.3, 0.3, 0.3, 0.01);
                world.spawnParticles(ParticleTypes.FLAME, p.getX() + 0.5, p.getY() + 1,
                        p.getZ() + 0.5, 2, 0.25, 0.25, 0.25, 0.01);
                if (world.getBlockState(up).isAir() && world.random.nextFloat() < 0.25f) {
                    world.setBlockState(up, Blocks.FIRE.getDefaultState(), 3);
                }
            }
            if (tick % 10 == 0) {
                fireSound(center, 2.0f, 1.0f);
            }
            return;
        }
        // section-by-section detonation on a shuffled 4x2x4 sub-grid
        java.util.List<BlockPos> secs = new ArrayList<>();
        int gx = Math.max(1, (int) Math.ceil((max.getX() - min.getX() + 1) / 8.0));
        int gy = Math.max(1, (int) Math.ceil((max.getY() - min.getY() + 1) / 6.0));
        int gz = Math.max(1, (int) Math.ceil((max.getZ() - min.getZ() + 1) / 8.0));
        for (int ix = 0; ix < gx; ix++)
            for (int iy = 0; iy < gy; iy++)
                for (int iz = 0; iz < gz; iz++)
                    secs.add(new BlockPos(ix, iy, iz));
        Collections.shuffle(secs);
        int idx = (tick - phase1End) / Math.max(1, (int) (4 / Math.max(0.25f, speed)));
        if (idx >= secs.size()) { done = true; return; }
        BlockPos s = secs.get(idx);
        Vec3d c = new Vec3d(min.getX() + (s.getX() + 0.5) * 8.0,
                min.getY() + (s.getY() + 0.5) * 6.0,
                min.getZ() + (s.getZ() + 0.5) * 8.0);
        destroyCube(c, 6.5, 0.15f);
    }

    private void stepMeltdown() {
        int critTick = (int) (50 / Math.max(0.25f, speed));
        if (tick < critTick) {
            // server room is about to blow: sparks + smoke from the core
            for (int i = 0; i < 12; i++) {
                Vec3d v = center.add(world.random.nextGaussian() * 2.5,
                        world.random.nextGaussian() * 1.5,
                        world.random.nextGaussian() * 2.5);
                world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, v.x, v.y, v.z,
                        4, 0.3, 0.3, 0.3, 0.08);
                world.spawnParticles(ParticleTypes.SMOKE, v.x, v.y, v.z,
                        3, 0.3, 0.4, 0.3, 0.02);
            }
            if (tick % 8 == 0) {
                fireSound(center, 1.5f, 0.6f);
            }
            return;
        }
        if (tick == critTick) {
            // THE event - flash, deep boom, mushroom column starts
            world.spawnParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0, 0, 0, 0);
            for (int i = 0; i < 8; i++) {
                world.spawnParticles(ParticleTypes.EXPLOSION_EMITTER, center.x,
                        center.y + world.random.nextDouble() * 3, center.z, 1, 0, 0, 0, 0);
            }
            sound(center, 6.0f, 0.5f);
        }
        // carving wave inside the region (fast shockwave)
        double r = (tick - critTick) * 2.6 * speed;
        double maxDist = center.distanceTo(new Vec3d(min.getX(), min.getY(), min.getZ())) + 4;
        for (BlockPos p : blocks) {
            double d = Math.sqrt(p.getSquaredDistance(center));
            if (d >= r - 2.6 && d <= r) {
                Vec3d imp = new Vec3d(p.getX() + 0.5 - center.x, 0.6,
                        p.getZ() + 0.5 - center.z).normalize().multiply(0.55);
                destroyWithOverspill(p, 0.22f, imp);
            }
        }
        // mushroom stem + cap
        double h = 2 + (tick - critTick) * 0.6;
        for (int i = 0; i < 6; i++) {
            double rad = 0.8 + (i / 6.0) * 1.4;
            world.spawnParticles(ParticleTypes.LARGE_SMOKE, center.x + world.random.nextGaussian() * rad,
                    center.y + h, center.z + world.random.nextGaussian() * rad, 1, 0, 0, 0, 0);
            world.spawnParticles(ParticleTypes.FLAME, center.x + world.random.nextGaussian() * rad,
                    center.y + h, center.z + world.random.nextGaussian() * rad, 1, 0, 0, 0, 0);
        }
        if (tick - critTick > 8) {
            // cap bloom: rings of fire/cloud above
            double capY = center.y + 7 + (tick - critTick) * 0.35;
            double capR = Math.min(6.0, (tick - critTick - 8) * 0.45);
            for (int a = 0; a < 360; a += 20) {
                double rad = Math.toRadians(a);
                world.spawnParticles(ParticleTypes.CLOUD,
                        center.x + Math.cos(rad) * capR, capY, center.z + Math.sin(rad) * capR,
                        1, 0, 0.03, 0, 0);
                world.spawnParticles(ParticleTypes.FLAME,
                        center.x + Math.cos(rad) * capR * 0.6, capY - 0.5, center.z + Math.sin(rad) * capR * 0.6,
                        1, 0, 0, 0, 0);
            }
        }
        if (r > maxDist && tick - critTick > (int) (40 / Math.max(0.25f, speed))) done = true;
    }

    private void stepFlak() {
        int per = Math.max(1, (int) (3 / Math.max(0.25f, speed)));
        if (tick % per != 0) return;
        int shots = tick / per;
        int totalShots = Math.max(6, (blocks.size() / 90));
        if (shots > totalShots) { done = true; return; }
        Vec3d c = new Vec3d(
                min.getX() + world.random.nextInt(Math.max(1, max.getX() - min.getX() + 1)),
                min.getY() + world.random.nextInt(Math.max(1, max.getY() - min.getY() + 1)),
                min.getZ() + world.random.nextInt(Math.max(1, max.getZ() - min.getZ() + 1)));
        // flak tracer first
        for (int yy = max.getY() + 12; yy > c.y; yy -= 3) {
            world.spawnParticles(ParticleTypes.END_ROD, c.x, yy, c.z, 1, 0, 0, 0, 0);
        }
        destroyCube(c, 2.8, 0.16f);
    }

    private void stepImplode() {
        if (order == null) { done = true; return; }
        if (cursor >= order.size()) {
            // grand finale at the core
            world.spawnParticles(ParticleTypes.FLASH, center.x, center.y, center.z, 1, 0, 0, 0, 0);
            boomFx(center);
            done = true;
            return;
        }
        int batch = Math.max(12, (int) (blocks.size() / (70.0 * speed)));
        int end = Math.min(order.size(), cursor + batch);
        for (int i = cursor; i < end; i++) {
            BlockPos p = order.get(i);
            Vec3d imp = new Vec3d(center.x - p.getX() - 0.5, center.y - p.getY() - 0.5,
                    center.z - p.getZ() - 0.5).normalize().multiply(0.9);
            destroyWithOverspill(p, 0.30f, imp);
        }
        cursor = end;
        if (tick % 6 == 0) {
            sound(center, 2.0f, 1.4f);
        }
    }

    private void stepSweep() {
        int spanX = max.getX() - min.getX() + 1;
        double plane = min.getX() + spanX * (tick * 0.03 * speed);
        if (plane > max.getX() + 1) { done = true; return; }
        int px = (int) Math.floor(plane);
        for (int y = min.getY(); y <= max.getY(); y++) {
            for (int z = min.getZ(); z <= max.getZ(); z++) {
                BlockPos p = new BlockPos(px, y, z);
                destroyWithOverspill(p, 0.08f, new Vec3d(0.5, 0.4, 0));
                if (world.random.nextFloat() < 0.2f) {
                    world.spawnParticles(ParticleTypes.ELECTRIC_SPARK, px + 0.5, y + 0.5, z + 0.5,
                            2, 0.3, 0.3, 0.3, 0.05);
                }
            }
        }
        if (tick % 3 == 0) {
            sound(new Vec3d(px + 0.5, center.y, center.z), 1.5f, 1.8f);
        }
    }

    private void stepRain() {
        int per = Math.max(1, (int) (2 / Math.max(0.25f, speed)));
        if (tick % per != 0) return;
        // shell count until ~85% of blocks are gone
        int x = min.getX() + world.random.nextInt(Math.max(1, max.getX() - min.getX() + 1));
        int z = min.getZ() + world.random.nextInt(Math.max(1, max.getZ() - min.getZ() + 1));
        int contactY = max.getY();
        while (contactY > min.getY() && world.getBlockState(new BlockPos(x, contactY, z)).isAir()) contactY--;
        // tracer column
        for (int yy = max.getY() + 14; yy > contactY; yy -= 2) {
            world.spawnParticles(ParticleTypes.FLAME, x + 0.5, yy, z + 0.5, 1, 0, 0, 0, 0);
        }
        destroyCube(new Vec3d(x, Math.max(min.getY(), contactY), z), 2.2, 0.15f);
        if (tick > (int) (260 / Math.max(0.25f, speed))) done = true;
    }

    // ================================================================== factory
    public static BoomJob create(BoomType type, ServerWorld world, BlockPos min, BlockPos max, float speed) {
        return new BoomJob(world, min, max, type, speed);
    }

    // ================================================================== restore
    /** Layer-by-layer rebuild from a disk backup (slow-place, no lag spike). */
    public static class RestoreJob extends BoomJob {
        private final Backup backup;
        private final List<BlockState> palette;
        private int currentY = 0;
        private boolean entitiesSpawned = false;

        public RestoreJob(ServerWorld world, Backup backup, float speed) {
            super(world, backup.origin, backup.origin.add(backup.sx - 1, backup.sy - 1, backup.sz - 1),
                    BoomType.PANCAKE, speed); // dummy type; step() overridden
            this.backup = backup;
            this.palette = new ArrayList<>(backup.palette.size());
            // toBlockState wants RegistryEntryLookup<Block>, not DynamicRegistryManager
            var blockLookup = world.getRegistryManager().createRegistryLookup()
                    .getOrThrow(net.minecraft.registry.RegistryKeys.BLOCK);
            for (NbtCompound c : backup.palette) {
                this.palette.add(NbtHelper.toBlockState(blockLookup, c));
            }
            this.blocks.clear();
        }

        @Override
        public void step() {
            tick++;
            try {
                int layersPerTick = Math.max(1, (int) Math.ceil(speed));
                for (int n = 0; n < layersPerTick; n++) {
                    if (currentY >= backup.sy) break;
                    placeLayer(currentY);
                    currentY++;
                }
                if (currentY >= backup.sy) {
                    if (!entitiesSpawned) {
                        spawnEntities();
                        entitiesSpawned = true;
                    }
                    done = true;
                }
            } catch (Exception e) {
                done = true;
            }
        }

        private void placeLayer(int y) {
            for (int z = 0; z < backup.sz; z++) {
                for (int x = 0; x < backup.sx; x++) {
                    int i = backup.index(x, y, z);
                    BlockPos p = backup.origin.add(x, y, z);
                    BlockState s = palette.get(backup.blockIds[i]);
                    if (!world.getBlockState(p).equals(s)) {
                        world.setBlockState(p, s, 3);
                    }
                    NbtCompound beNbt = backup.blockEntities.get(i);
                    if (beNbt != null) {
                        BlockEntity be = world.getBlockEntity(p);
                        if (be != null) {
                            be.read(beNbt, world.getRegistryManager());
                        }
                    }
                }
            }
            // soft shimmer along the new floor
            Vec3d c = new Vec3d(backup.origin.getX() + backup.sx / 2.0,
                    backup.origin.getY() + y, backup.origin.getZ() + backup.sz / 2.0);
            world.spawnParticles(ParticleTypes.END_ROD, c.x, c.y + 0.5, c.z,
                    12, backup.sx / 2.0, 0.2, backup.sz / 2.0, 0.01);
        }

        private void spawnEntities() {
            for (NbtCompound c : backup.entities) {
                EntityType.getEntityFromNbt(c, world).ifPresent(world::spawnEntity);
            }
        }
    }
}
