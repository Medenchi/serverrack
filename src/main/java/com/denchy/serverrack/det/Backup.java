package com.denchy.serverrack.det;

import com.denchy.serverrack.ServerRackMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIntArray;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.WorldSavePath;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Region snapshot: blocks (palette + int ids), block entities, pinned entities
 * (item frames, armor stands, paintings - no mobs, no players).
 * Stored on disk inside the world folder so retakes survive re-logging.
 */
public final class Backup {

    public final BlockPos origin;      // min corner
    public final int sx, sy, sz;
    public final List<NbtCompound> palette;          // BlockState NBTs
    public final int[] blockIds;                     // sx*sy*sz indices into palette
    public final Map<Integer, NbtCompound> blockEntities; // index -> be nbt
    public final List<NbtCompound> entities;

    private Backup(BlockPos origin, int sx, int sy, int sz,
                   List<NbtCompound> palette, int[] blockIds,
                   Map<Integer, NbtCompound> blockEntities, List<NbtCompound> entities) {
        this.origin = origin;
        this.sx = sx; this.sy = sy; this.sz = sz;
        this.palette = palette;
        this.blockIds = blockIds;
        this.blockEntities = blockEntities;
        this.entities = entities;
    }

    public int index(int x, int y, int z) {
        return (y * sz + z) * sx + x;
    }

    // ---------------------------------------------------------------- folder/files
    public static Path backupDir(ServerWorld world) {
        return world.getServer().getSavePath(WorldSavePath.ROOT).resolve("serverrack_backups");
    }

    public static Path fileFor(ServerWorld world, UUID player) {
        String dim = world.getRegistryKey().getValue().toString().replace(':', '_');
        return backupDir(world).resolve(player.toString() + "_" + dim + ".nbt");
    }

    // ---------------------------------------------------------------- capture
    public static Backup capture(ServerWorld world, BlockPos min, BlockPos max) {
        int sx = max.getX() - min.getX() + 1;
        int sy = max.getY() - min.getY() + 1;
        int sz = max.getZ() - min.getZ() + 1;

        List<NbtCompound> palette = new ArrayList<>();
        Map<BlockState, Integer> paletteIdx = new HashMap<>();
        int[] ids = new int[sx * sy * sz];
        Map<Integer, NbtCompound> bes = new HashMap<>();

        int i = 0;
        for (int y = 0; y < sy; y++) {
            for (int z = 0; z < sz; z++) {
                for (int x = 0; x < sx; x++, i++) {
                    BlockPos p = min.add(x, y, z);
                    BlockState s = world.getBlockState(p);
                    Integer id = paletteIdx.get(s);
                    if (id == null) {
                        id = palette.size();
                        paletteIdx.put(s, id);
                        palette.add(NbtHelper.fromBlockState(s));
                    }
                    ids[i] = id;
                    BlockEntity be = world.getBlockEntity(p);
                    if (be != null) {
                        bes.put(i, be.createNbtWithIdentifyingData(world.getRegistryManager()));
                    }
                }
            }
        }

        List<NbtCompound> ents = new ArrayList<>();
        Box box = new Box(min, max.add(1, 1, 1));
        for (Entity e : world.getEntitiesByClass(Entity.class, box,
                en -> !(en instanceof PlayerEntity) && !(en instanceof MobEntity))) {
            NbtCompound c = new NbtCompound();
            if (e.saveNbt(c)) {
                ents.add(c);
            }
        }
        return new Backup(min.toImmutable(), sx, sy, sz, palette, ids, bes, ents);
    }

    // ---------------------------------------------------------------- nbt io
    public NbtCompound toNbt() {
        NbtCompound root = new NbtCompound();
        root.putIntArray("origin", new int[]{origin.getX(), origin.getY(), origin.getZ()});
        root.putInt("sx", sx);
        root.putInt("sy", sy);
        root.putInt("sz", sz);
        NbtList pal = new NbtList();
        pal.addAll(palette);
        root.put("palette", pal);
        root.put("blocks", new NbtIntArray(blockIds));
        NbtList bes = new NbtList();
        for (var entry : blockEntities.entrySet()) {
            NbtCompound c = new NbtCompound();
            c.putInt("i", entry.getKey());
            c.put("nbt", entry.getValue());
            bes.add(c);
        }
        root.put("block_entities", bes);
        NbtList ents = new NbtList();
        ents.addAll(entities);
        root.put("entities", ents);
        return root;
    }

    public static Backup fromNbt(NbtCompound root) {
        int[] o = root.getIntArray("origin");
        BlockPos origin = new BlockPos(o[0], o[1], o[2]);
        int sx = root.getInt("sx");
        int sy = root.getInt("sy");
        int sz = root.getInt("sz");
        List<NbtCompound> palette = new ArrayList<>();
        for (int i = 0; i < root.getList("palette", 10).size(); i++) {
            palette.add(root.getList("palette", 10).getCompound(i));
        }
        int[] ids = root.getIntArray("blocks");
        Map<Integer, NbtCompound> bes = new HashMap<>();
        NbtList besList = root.getList("block_entities", 10);
        for (int i = 0; i < besList.size(); i++) {
            NbtCompound c = besList.getCompound(i);
            bes.put(c.getInt("i"), c.getCompound("nbt"));
        }
        List<NbtCompound> ents = new ArrayList<>();
        NbtList entsList = root.getList("entities", 10);
        for (int i = 0; i < entsList.size(); i++) {
            ents.add(entsList.getCompound(i));
        }
        return new Backup(origin, sx, sy, sz, palette, ids, bes, ents);
    }

    /** Write to disk off-thread so a big region stutters the game less. */
    public void saveAsync(ServerWorld world, UUID player) {
        Path file = fileFor(world, player);
        NbtCompound nbt = toNbt();
        new Thread(() -> {
            try {
                Files.createDirectories(file.getParent());
                NbtIo.writeCompressed(nbt, file);
                ServerRackMod.LOGGER.info("[ServerRack] Backup written: {} ({} blocks)", file, (long) sx * sy * sz);
            } catch (IOException e) {
                ServerRackMod.LOGGER.error("[ServerRack] Backup write failed", e);
            }
        }, "serverrack-backup-writer").start();
    }

    public static Backup load(Path file) throws IOException {
        return fromNbt(NbtIo.readCompressed(file, NbtSizeTracker.ofUnlimitedBytes()));
    }
}
