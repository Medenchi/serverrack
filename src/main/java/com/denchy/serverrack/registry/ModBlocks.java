package com.denchy.serverrack.registry;

import com.denchy.serverrack.ModId;
import com.denchy.serverrack.block.AkChairBlock;
import com.denchy.serverrack.block.AkMonitorBlock;
import com.denchy.serverrack.block.BigServerRackBlock;
import com.denchy.serverrack.block.DecorBlock;
import com.denchy.serverrack.block.MainframeRackBlock;
import com.denchy.serverrack.block.PcWallBlock;
import com.denchy.serverrack.block.ServerRackBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ModBlocks {
    private ModBlocks() {}

    public static final List<Block> RACKS = new ArrayList<>();

    public static final ServerRackBlock RACK_BASIC = registerSmall("server_rack_basic");
    public static final BigServerRackBlock RACK_ADVANCED = registerBig("server_rack_advanced");
    public static final MainframeRackBlock RACK_MAINFRAME = registerMainframe("server_rack_mainframe");
    public static final PcWallBlock PC_WALL = registerPcWall("pc_wall");

    // AK module: one big GTA-6-loading monitor on a stand + the gaming chair
    public static final AkMonitorBlock AK_MONITOR = registerAkMonitor("ak_monitor");
    public static final AkChairBlock AK_CHAIR = registerAkChair("ak_chair");

    // Small room decor (mug, bin, board, plant, papers, router)
    public static final DecorBlock DECOR_MUG = registerDecor("decor_mug",
            net.minecraft.util.shape.VoxelShapes.cuboid(0.3125, 0, 0.3125, 0.625, 0.3125, 0.625), false);
    public static final DecorBlock DECOR_BIN = registerDecor("decor_bin",
            net.minecraft.util.shape.VoxelShapes.cuboid(0.1875, 0, 0.1875, 0.8125, 0.8125, 0.8125), false);
    public static final DecorBlock DECOR_BOARD = registerDecor("decor_board",
            net.minecraft.util.shape.VoxelShapes.cuboid(0.03125, 0.125, 0.875, 0.96875, 0.875, 1.0), true);
    public static final DecorBlock DECOR_PLANT = registerDecor("decor_plant",
            net.minecraft.util.shape.VoxelShapes.cuboid(0.15625, 0, 0.15625, 0.84375, 0.875, 0.84375), false);
    public static final DecorBlock DECOR_PAPERS = registerDecor("decor_papers",
            net.minecraft.util.shape.VoxelShapes.cuboid(0.125, 0, 0.125, 0.875, 0.25, 0.875), false);
    public static final DecorBlock DECOR_ROUTER = registerDecor("decor_router",
            net.minecraft.util.shape.VoxelShapes.union(
                    net.minecraft.util.shape.VoxelShapes.cuboid(0.125, 0, 0.25, 0.875, 0.1875, 0.75),
                    net.minecraft.util.shape.VoxelShapes.cuboid(0.6875, 0, 0.4375, 0.75, 0.625, 0.5)), false);

    private static AkMonitorBlock registerAkMonitor(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK)
                .strength(2.5f, 4.0f)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .luminance(state -> state.get(AkMonitorBlock.ERROR) ? 12 : 9);
        AkMonitorBlock block = new AkMonitorBlock(settings);
        Registry.register(Registries.BLOCK, ModId.of(name), block);
        return block;
    }

    private static AkChairBlock registerAkChair(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK)
                .strength(1.5f, 3.0f)
                .sounds(BlockSoundGroup.WOOL)
                .nonOpaque();
        AkChairBlock block = new AkChairBlock(settings);
        Registry.register(Registries.BLOCK, ModId.of(name), block);
        return block;
    }

    private static DecorBlock registerDecor(String name, net.minecraft.util.shape.VoxelShape shape, boolean facing) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.GRAY)
                .strength(0.5f, 1.0f)
                .sounds(BlockSoundGroup.WOOD)
                .nonOpaque();
        DecorBlock block = new DecorBlock(settings, shape, facing);
        Registry.register(Registries.BLOCK, ModId.of(name), block);
        return block;
    }

    private static ServerRackBlock registerSmall(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK)
                .strength(4.0f, 6.0f)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .pistonBehavior(PistonBehavior.BLOCK)
                .luminance(state -> state.get(ServerRackBlock.ACTIVE) ? 10 : 4);
        ServerRackBlock block = new ServerRackBlock(settings);
        Identifier id = ModId.of(name);
        Registry.register(Registries.BLOCK, id, block);
        RACKS.add(block);
        return block;
    }

    private static BigServerRackBlock registerBig(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK)
                .strength(5.0f, 7.0f)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .pistonBehavior(PistonBehavior.BLOCK)
                .luminance(state -> state.get(BigServerRackBlock.ACTIVE) ? 12 : 5);
        BigServerRackBlock block = new BigServerRackBlock(settings);
        Identifier id = ModId.of(name);
        Registry.register(Registries.BLOCK, id, block);
        RACKS.add(block);
        return block;
    }

    private static MainframeRackBlock registerMainframe(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK)
                .strength(6.0f, 8.0f)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .pistonBehavior(PistonBehavior.BLOCK)
                .luminance(state -> state.get(MainframeRackBlock.ACTIVE) ? 13 : 5);
        MainframeRackBlock block = new MainframeRackBlock(settings);
        Identifier id = ModId.of(name);
        Registry.register(Registries.BLOCK, id, block);
        RACKS.add(block);
        return block;
    }

    private static PcWallBlock registerPcWall(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK)
                .strength(2.5f, 4.0f)
                .requiresTool()
                .sounds(BlockSoundGroup.METAL)
                .nonOpaque()
                .pistonBehavior(PistonBehavior.BLOCK)
                .luminance(state -> state.get(PcWallBlock.ALERT) ? 14 : 9);
        PcWallBlock block = new PcWallBlock(settings);
        Identifier id = ModId.of(name);
        Registry.register(Registries.BLOCK, id, block);
        return block;
    }

    public static void register() {}

    public record LowerInfo(BlockPos lowerPos, int height) {}

    public static LowerInfo getLowerInfo(BlockState state, BlockPos pos) {
        var block = state.getBlock();
        if (block instanceof ServerRackBlock b) {
            return new LowerInfo(b.lowerPos(state, pos), b.height());
        } else if (block instanceof BigServerRackBlock b) {
            return new LowerInfo(b.lowerPos(state, pos), b.height());
        } else if (block instanceof MainframeRackBlock b) {
            return new LowerInfo(b.lowerPos(state, pos), b.height());
        }
        return null;
    }

    public static boolean isRack(Block block) {
        return block instanceof ServerRackBlock || block instanceof BigServerRackBlock || block instanceof MainframeRackBlock;
    }

    public static boolean isPc(Block block) {
        return block instanceof PcWallBlock;
    }

    public static boolean isAk(Block block) {
        return block instanceof AkMonitorBlock;
    }

    public static boolean isActiveState(BlockState state) {
        Block block = state.getBlock();
        if (block instanceof ServerRackBlock) return state.get(ServerRackBlock.ACTIVE);
        if (block instanceof BigServerRackBlock) return state.get(BigServerRackBlock.ACTIVE);
        if (block instanceof MainframeRackBlock) return state.get(MainframeRackBlock.ACTIVE);
        if (block instanceof PcWallBlock) return state.get(PcWallBlock.ALERT);
        return false;
    }

    /**
     * Toggle every rack in a cube around center.
     * If at least one rack is inactive -> turn ALL on. Otherwise -> turn ALL off.
     * Returns number of racks affected.
     */
    public static int toggleRacksInRadius(World world, BlockPos center, int radius) {
        Set<BlockPos> lowers = new LinkedHashSet<>();
        BlockPos min = center.add(-radius, -radius, -radius);
        BlockPos max = center.add(radius, radius, radius);
        for (BlockPos p : BlockPos.iterate(min, max)) {
            BlockState s = world.getBlockState(p);
            if (!isRack(s.getBlock())) continue;
            LowerInfo info = getLowerInfo(s, p);
            if (info != null) lowers.add(info.lowerPos().toImmutable());
        }
        if (lowers.isEmpty()) return 0;

        boolean anyInactive = false;
        for (BlockPos lp : lowers) {
            if (!isActiveState(world.getBlockState(lp))) {
                anyInactive = true;
                break;
            }
        }
        boolean target = anyInactive; // any off -> turn everything on
        for (BlockPos lp : lowers) {
            boolean cur = isActiveState(world.getBlockState(lp));
            if (cur != target) toggleAt(world, lp);
        }
        return lowers.size();
    }

    /**
     * Same as toggleRacksInRadius but flips PcWallBlock ALERT state.
     * Any PC setup not in alert -> ALL go alert. Otherwise ALL calm down.
     * Structures are de-duplicated by their desk-center origin.
     */
    public static int togglePcsInRadius(World world, BlockPos center, int radius) {
        Set<BlockPos> origins = new LinkedHashSet<>();
        BlockPos min = center.add(-radius, -radius, -radius);
        BlockPos max = center.add(radius, radius, radius);
        for (BlockPos p : BlockPos.iterate(min, max)) {
            BlockState s = world.getBlockState(p);
            if (!(s.getBlock() instanceof PcWallBlock)) continue;
            origins.add(PcWallBlock.getOrigin(s, p));
        }
        if (origins.isEmpty()) return 0;

        boolean anyCalm = false;
        for (BlockPos o : origins) {
            BlockState s = world.getBlockState(o);
            if (s.getBlock() instanceof PcWallBlock && !s.get(PcWallBlock.ALERT)) {
                anyCalm = true;
                break;
            }
        }
        boolean target = anyCalm;
        for (BlockPos o : origins) {
            PcWallBlock.setAlertStructure(world, o, target);
        }
        // one sound from the middle of the action instead of a choir
        PcWallBlock.playAlertSound(world, center, target);
        return origins.size();
    }

    /**
     * Same as togglePcsInRadius but flips AkMonitorBlock ERROR state (X key).
     * Any AK setup not in error -> ALL show ОШИБКА ЗАГРУЗКИ!; a second press
     * brings every monitor back to the GTA-6 loading loop.
     * Structures are de-duplicated by their viewer-right-bottom origin.
     */
    public static int toggleAksInRadius(World world, BlockPos center, int radius) {
        Set<BlockPos> origins = new LinkedHashSet<>();
        BlockPos min = center.add(-radius, -radius, -radius);
        BlockPos max = center.add(radius, radius, radius);
        for (BlockPos p : BlockPos.iterate(min, max)) {
            BlockState s = world.getBlockState(p);
            if (!(s.getBlock() instanceof AkMonitorBlock)) continue;
            origins.add(AkMonitorBlock.getOrigin(s, p));
        }
        if (origins.isEmpty()) return 0;

        boolean anyNormal = false;
        for (BlockPos o : origins) {
            BlockState s = world.getBlockState(o);
            if (s.getBlock() instanceof AkMonitorBlock && !s.get(AkMonitorBlock.ERROR)) {
                anyNormal = true;
                break;
            }
        }
        boolean target = anyNormal;
        for (BlockPos o : origins) {
            AkMonitorBlock.setErrorStructure(world, o, target);
        }
        // one sound from the middle of the action instead of a choir
        PcWallBlock.playAlertSound(world, center, target);
        return origins.size();
    }

    public static void toggleAt(World world, BlockPos anyPos) {
        BlockState state = world.getBlockState(anyPos);
        LowerInfo info = getLowerInfo(state, anyPos);
        if (info == null) return;
        BlockState lowerState = world.getBlockState(info.lowerPos());
        Block lowerBlock = lowerState.getBlock();
        if (lowerBlock instanceof ServerRackBlock) {
            ServerRackBlock.toggleActive(world, info.lowerPos());
        } else if (lowerBlock instanceof BigServerRackBlock) {
            BigServerRackBlock.toggleActive(world, info.lowerPos());
        } else if (lowerBlock instanceof MainframeRackBlock) {
            MainframeRackBlock.toggleActive(world, info.lowerPos());
        }
    }
}
