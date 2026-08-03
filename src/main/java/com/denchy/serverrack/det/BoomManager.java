package com.denchy.serverrack.det;

import com.denchy.serverrack.network.payload.BoomActionPayload;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.BlockState;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Director module state: per-player selection, boom-type/speed, running jobs,
 * slow-mo lift. Fully independent from the rack/pc modules.
 */
public final class BoomManager {
    private BoomManager() {}

    public static class Sel {
        public BlockPos p1, p2;
        public BoomType type = BoomType.MELTDOWN;
        public float speed = 1.0f;
        public boolean hasBackup = false;
    }

    private static final Map<UUID, Sel> SELS = new ConcurrentHashMap<>();
    private static final List<BoomJob> JOBS = new CopyOnWriteArrayList<>();
    private static boolean slowed = false;

    private static Sel sel(UUID id) {
        return SELS.computeIfAbsent(id, u -> new Sel());
    }

    public static void register() {
        // LMB with the detonator = corner 1 (and protect the block)
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (!(player.getStackInHand(hand).getItem() instanceof DetonatorItem)) {
                return ActionResult.PASS;
            }
            if (!world.isClient && player instanceof ServerPlayerEntity sp) {
                sel(sp.getUuid()).p1 = pos.toImmutable();
                msg(sp, "§a[Детонатор] Точка 1: §f" + posFmt(pos));
                outlinePoint(sp, pos);
            }
            return ActionResult.SUCCESS;
        });

        ServerPlayNetworking.registerGlobalReceiver(BoomActionPayload.ID, (payload, context) -> {
            context.server().execute(() -> handle(context.player(), payload.action()));
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (BoomJob job : JOBS) {
                job.step();
            }
            JOBS.removeIf(j -> j.done);
            if (JOBS.isEmpty() && slowed) {
                server.getTickManager().setTickRate(20.0f);
                slowed = false;
            }
        });
    }

    /** sneak+RMB in air */
    public static void clearViaItem(ServerPlayerEntity player) {
        Sel s = sel(player.getUuid());
        s.p1 = null;
        s.p2 = null;
        s.hasBackup = false;
        msg(player, "§7[Детонатор] Выделение снято");
    }

    // RMB on a block = corner 2 (called from the item)
    public static void setSel2(ServerPlayerEntity player, BlockPos pos) {
        sel(player.getUuid()).p2 = pos.toImmutable();
        msg(player, "§a[Детонатор] Точка 2: §f" + posFmt(pos));
        outlinePoint(player, pos);
        Sel s = sel(player.getUuid());
        if (s.p1 != null && s.p2 != null) {
            outlineBox(player);
            msg(player, "§e[Детонатор] Выделено: §f" + blockCount(s) + " §eблоков. Бэкаплю...");
            backupAsync(player);
        }
    }

    private static int blockCount(Sel s) {
        BlockPos a = s.p1, b = s.p2;
        return (Math.abs(a.getX() - b.getX()) + 1) * (Math.abs(a.getY() - b.getY()) + 1)
                * (Math.abs(a.getZ() - b.getZ()) + 1);
    }

    private static void handle(ServerPlayerEntity player, String action) {
        Sel s = sel(player.getUuid());
        switch (action) {
            case "boom" -> boom(player, s);
            case "restore" -> restore(player, s);
            case "rebackup" -> {
                if (s.p1 == null || s.p2 == null) {
                    err(player, "Сначала выдели две точки детонатором");
                } else {
                    msg(player, "§e[Детонатор] Переснимаю бэкап...");
                    backupAsync(player);
                }
            }
            case "clear" -> {
                s.p1 = null;
                s.p2 = null;
                s.hasBackup = false;
                msg(player, "§7[Детонатор] Выделение снято");
            }
            default -> {
                if (action.startsWith("type_")) {
                    s.type = BoomType.byIndex(Integer.parseInt(action.substring(5)));
                    msg(player, "§e[Детонатор] Взрыв: §f" + s.type.ruName());
                } else if (action.startsWith("speed_")) {
                    s.speed = Float.parseFloat(action.substring(6));
                    msg(player, "§e[Детонатор] Темп: §fx" + s.speed);
                }
            }
        }
    }

    private static void boom(ServerPlayerEntity player, Sel s) {
        if (s.p1 == null || s.p2 == null) {
            err(player, "Нет выделения! ЛКМ - точка 1, ПКМ - точка 2");
            return;
        }
        ServerWorld world = (ServerWorld) player.getWorld();
        BlockPos min = minOf(s.p1, s.p2);
        BlockPos max = maxOf(s.p1, s.p2);

        if (!s.hasBackup) {
            msg(player, "§c[Детонатор] Бэкапа не было - делаю сейчас, сцена №1");
            Backup b = Backup.capture(world, min, max);
            b.saveAsync(world, player.getUuid());
            s.hasBackup = true;
        }

        JOBS.add(BoomJob.create(s.type, world, min, max, s.speed));
        msg(player, "§4[Детонатор] §cБАБАХ! §7«" + s.type.ruName() + "», темп x" + s.speed);

        if (s.speed < 1.0f) {
            player.getServer().getTickManager().setTickRate(20.0f * s.speed);
            slowed = true;
        }
    }

    private static void restore(ServerPlayerEntity player, Sel s) {
        ServerWorld world = (ServerWorld) player.getWorld();
        Path file = Backup.fileFor(world, player.getUuid());
        if (!Files.exists(file)) {
            err(player, "Бэкапа нет в этом измерении - сначала выдели и бахни");
            return;
        }
        try {
            Backup b = Backup.load(file);
            JOBS.add(new BoomJob.RestoreJob(world, b, 2.0f));
            msg(player, "§a[Детонатор] Восстанавливаю сцену по слоям, дубль 2 готовится...");
        } catch (IOException e) {
            err(player, "Бэкап не читается: " + e.getMessage());
        }
    }

    private static void backupAsync(ServerPlayerEntity player) {
        Sel s = sel(player.getUuid());
        if (s.p1 == null || s.p2 == null) return;
        ServerWorld world = (ServerWorld) player.getWorld();
        Backup b = Backup.capture(world, minOf(s.p1, s.p2), maxOf(s.p1, s.p2));
        b.saveAsync(world, player.getUuid());
        s.hasBackup = true;
    }

    // ---------------------------------------------------------------- helpers
    private static BlockPos minOf(BlockPos a, BlockPos b) {
        return new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
    }

    private static BlockPos maxOf(BlockPos a, BlockPos b) {
        return new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }

    private static String posFmt(BlockPos p) {
        return p.getX() + " " + p.getY() + " " + p.getZ();
    }

    private static void msg(ServerPlayerEntity p, String s) {
        p.sendMessage(Text.literal(s), true);
    }

    private static void err(ServerPlayerEntity p, String s) {
        p.sendMessage(Text.literal("§c[Детонатор] §f" + s), true);
    }

    private static void outlinePoint(ServerPlayerEntity p, BlockPos pos) {
        ServerWorld w = (ServerWorld) p.getWorld();
        w.spawnParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                8, 0.3, 0.3, 0.3, 0.02);
    }

    private static void outlineBox(ServerPlayerEntity p) {
        Sel s = sel(p.getUuid());
        ServerWorld w = (ServerWorld) p.getWorld();
        Vec3d a = Vec3d.of(minOf(s.p1, s.p2));
        Vec3d b = Vec3d.of(maxOf(s.p1, s.p2)).add(1, 1, 1);
        // 12 edges
        double[][] c = {
                {a.x, a.y}, {a.x, b.y}, {b.x, a.y}, {b.x, b.y}
        };
        for (double[] xy : c) {
            for (double z = a.z; z <= b.z; z += 1.0) {
                w.spawnParticles(ParticleTypes.WAX_ON, xy[0], xy[1], z, 1, 0, 0, 0, 0);
            }
        }
        for (double y = a.y; y <= b.y; y += 1.0) {
            w.spawnParticles(ParticleTypes.WAX_ON, a.x, y, a.z, 1, 0, 0, 0, 0);
            w.spawnParticles(ParticleTypes.WAX_ON, b.x, y, a.z, 1, 0, 0, 0, 0);
            w.spawnParticles(ParticleTypes.WAX_ON, a.x, y, b.z, 1, 0, 0, 0, 0);
            w.spawnParticles(ParticleTypes.WAX_ON, b.x, y, b.z, 1, 0, 0, 0, 0);
        }
        for (double x = a.x; x <= b.x; x += 1.0) {
            for (double z = a.z; z <= b.z; z += Math.max(1.0, b.z - a.z)) {
                w.spawnParticles(ParticleTypes.WAX_ON, x, a.y, z, 1, 0, 0, 0, 0);
                w.spawnParticles(ParticleTypes.WAX_ON, x, b.y, z, 1, 0, 0, 0, 0);
            }
        }
    }
}
