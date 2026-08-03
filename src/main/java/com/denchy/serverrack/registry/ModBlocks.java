package com.denchy.serverrack.registry;

import com.denchy.serverrack.ModId;
import com.denchy.serverrack.block.*;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.math.BlockPos;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class ModBlocks {

    private ModBlocks() {}

    public static final List<Block> RACKS = new ArrayList<>();

    // === Объявляем как null, регистрируем только в register() ===
    public static ServerRackBlock RACK_BASIC;
    public static BigServerRackBlock RACK_ADVANCED;
    public static MainframeRackBlock RACK_MAINFRAME;
    public static PcWallBlock PC_WALL;
    public static AkMonitorBlock AK_MONITOR;
    public static AkChairBlock AK_CHAIR;

    public static DecorBlock DECOR_MUG;
    public static DecorBlock DECOR_BIN;
    public static DecorBlock DECOR_BOARD;
    public static DecorBlock DECOR_PLANT;
    public static DecorBlock DECOR_PAPERS;
    public static DecorBlock DECOR_ROUTER;

    public static com.denchy.serverrack.block.rockstar.RockstarSignBlock ROCKSTAR_SIGN;

    /** Главный метод регистрации всех блоков */
    public static void register() {
        RACK_BASIC = registerSmall("server_rack_basic");
        RACK_ADVANCED = registerBig("server_rack_advanced");
        RACK_MAINFRAME = registerMainframe("server_rack_mainframe");
        PC_WALL = registerPcWall("pc_wall");

        AK_MONITOR = registerAkMonitor("ak_monitor");
        AK_CHAIR = registerAkChair("ak_chair");

        DECOR_MUG = registerDecor("decor_mug", VoxelShapes.cuboid(0.3125, 0, 0.3125, 0.625, 0.3125, 0.625));
        DECOR_BIN = registerDecor("decor_bin", VoxelShapes.cuboid(0.1875, 0, 0.1875, 0.8125, 0.8125, 0.8125));
        DECOR_BOARD = registerDecor("decor_board", VoxelShapes.cuboid(0.03125, 0.125, 0.875, 0.96875, 0.875, 1.0));
        DECOR_PLANT = registerDecor("decor_plant", VoxelShapes.cuboid(0.15625, 0, 0.15625, 0.84375, 0.875, 0.84375));
        DECOR_PAPERS = registerDecor("decor_papers", VoxelShapes.cuboid(0.125, 0, 0.125, 0.875, 0.25, 0.875));
        DECOR_ROUTER = registerDecor("decor_router", VoxelShapes.union(
                VoxelShapes.cuboid(0.125, 0, 0.25, 0.875, 0.1875, 0.75),
                VoxelShapes.cuboid(0.6875, 0, 0.4375, 0.75, 0.625, 0.5)));

        ROCKSTAR_SIGN = registerRockstar("rockstar_sign");
    }

    // === Регистраторы ===

    private static AkMonitorBlock registerAkMonitor(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK).strength(2.5f, 4.0f).requiresTool()
                .sounds(BlockSoundGroup.METAL).nonOpaque()
                .luminance(state -> state.get(AkMonitorBlock.ERROR) ? 12 : 9);
        AkMonitorBlock block = new AkMonitorBlock(settings);
        Registry.register(Registries.BLOCK, ModId.of(name), block);
        return block;
    }

    private static AkChairBlock registerAkChair(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK).strength(1.5f, 3.0f)
                .sounds(BlockSoundGroup.WOOL).nonOpaque();
        AkChairBlock block = new AkChairBlock(settings);
        Registry.register(Registries.BLOCK, ModId.of(name), block);
        return block;
    }

    private static DecorBlock registerDecor(String name, net.minecraft.util.shape.VoxelShape shape) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.GRAY).strength(0.5f, 1.0f)
                .sounds(BlockSoundGroup.WOOD).nonOpaque();
        DecorBlock block = new DecorBlock(settings, shape);
        Registry.register(Registries.BLOCK, ModId.of(name), block);
        return block;
    }

    private static ServerRackBlock registerSmall(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK).strength(4.0f, 6.0f).requiresTool()
                .sounds(BlockSoundGroup.METAL).nonOpaque()
                .pistonBehavior(PistonBehavior.BLOCK)
                .luminance(state -> state.get(ServerRackBlock.ACTIVE) ? 10 : 4);
        ServerRackBlock block = new ServerRackBlock(settings);
        Registry.register(Registries.BLOCK, ModId.of(name), block);
        RACKS.add(block);
        return block;
    }

    private static BigServerRackBlock registerBig(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK).strength(5.0f, 7.0f).requiresTool()
                .sounds(BlockSoundGroup.METAL).nonOpaque()
                .pistonBehavior(PistonBehavior.BLOCK)
                .luminance(state -> state.get(BigServerRackBlock.ACTIVE) ? 12 : 5);
        BigServerRackBlock block = new BigServerRackBlock(settings);
        Registry.register(Registries.BLOCK, ModId.of(name), block);
        RACKS.add(block);
        return block;
    }

    private static MainframeRackBlock registerMainframe(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK).strength(6.0f, 8.0f).requiresTool()
                .sounds(BlockSoundGroup.METAL).nonOpaque()
                .pistonBehavior(PistonBehavior.BLOCK)
                .luminance(state -> state.get(MainframeRackBlock.ACTIVE) ? 13 : 5);
        MainframeRackBlock block = new MainframeRackBlock(settings);
        Registry.register(Registries.BLOCK, ModId.of(name), block);
        RACKS.add(block);
        return block;
    }

    private static PcWallBlock registerPcWall(String name) {
        AbstractBlock.Settings settings = AbstractBlock.Settings.create()
                .mapColor(MapColor.BLACK).strength(2.5f, 4.0f).requiresTool()
                .sounds(BlockSoundGroup.METAL).nonOpaque()
                .pistonBehavior(PistonBehavior.BLOCK)
                .luminance(state -> state.get(PcWallBlock.ALERT) ? 14 : 9);
        PcWallBlock block = new PcWallBlock(settings);
        Registry.register(Registries.BLOCK, ModId.of(name), block);
        return block;
    }

    private static com.denchy.serverrack.block.rockstar.RockstarSignBlock registerRockstar(String name) {
        com.denchy.serverrack.block.rockstar.RockstarSignBlock block =
                new com.denchy.serverrack.block.rockstar.RockstarSignBlock(
                        AbstractBlock.Settings.create().strength(3.0f).nonOpaque());
        Registry.register(Registries.BLOCK, ModId.of(name), block);
        return block;
    }

    // === Вспомогательные методы (оставлены как были) ===

    public record LowerInfo(BlockPos lowerPos, int height) {}

    public static LowerInfo getLowerInfo(BlockState state, BlockPos pos) {
        var block = state.getBlock();
        if (block instanceof ServerRackBlock b) return new LowerInfo(b.lowerPos(state, pos), b.height());
        if (block instanceof BigServerRackBlock b) return new LowerInfo(b.lowerPos(state, pos), b.height());
        if (block instanceof MainframeRackBlock b) return new LowerInfo(b.lowerPos(state, pos), b.height());
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
            if (!isActiveState(world.getBlockState(lp))) { anyInactive = true; break; }
        }
        boolean target = anyInactive;
        for (BlockPos lp : lowers) {
            boolean cur = isActiveState(world.getBlockState(lp));
            if (cur != target) toggleAt(world, lp);
        }
        return lowers.size();
    }

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
            if (s.getBlock() instanceof PcWallBlock && !s.get(PcWallBlock.ALERT)) { anyCalm = true; break; }
        }
        boolean target = anyCalm;
        for (BlockPos o : origins) {
            PcWallBlock.setAlertStructure(world, o, target);
        }
        PcWallBlock.playAlertSound(world, center, target);
        return origins.size();
    }

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
            if (s.getBlock() instanceof AkMonitorBlock && !s.get(AkMonitorBlock.ERROR)) { anyNormal = true; break; }
        }
        boolean target = anyNormal;
        for (BlockPos o : origins) {
            AkMonitorBlock.setErrorStructure(world, o, target);
        }
        PcWallBlock.playAlertSound(world, center, target);
        return origins.size();
    }

    public static void toggleAt(World world, BlockPos anyPos) {
        BlockState state = world.getBlockState(anyPos);
        LowerInfo info = getLowerInfo(state, anyPos);
        if (info == null) return;
        BlockState lowerState = world.getBlockState(info.lowerPos());
        Block lowerBlock = lowerState.getBlock();
        if (lowerBlock instanceof ServerRackBlock) ServerRackBlock.toggleActive(world, info.lowerPos());
        else if (lowerBlock instanceof BigServerRackBlock) BigServerRackBlock.toggleActive(world, info.lowerPos());
        else if (lowerBlock instanceof MainframeRackBlock) MainframeRackBlock.toggleActive(world, info.lowerPos());
    }
}