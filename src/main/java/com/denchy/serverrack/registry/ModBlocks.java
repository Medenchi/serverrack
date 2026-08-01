package com.denchy.serverrack.registry;

import com.denchy.serverrack.ModId;
import com.denchy.serverrack.block.BigServerRackBlock;
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
