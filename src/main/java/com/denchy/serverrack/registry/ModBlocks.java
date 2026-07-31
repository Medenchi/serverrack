package com.denchy.serverrack.registry;

import com.denchy.serverrack.ModId;
import com.denchy.serverrack.block.BigServerRackBlock;
import com.denchy.serverrack.block.MainframeRackBlock;
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
import java.util.List;

public final class ModBlocks {
    private ModBlocks() {}

    public static final List<Block> RACKS = new ArrayList<>();

    public static final ServerRackBlock RACK_BASIC = registerSmall("server_rack_basic");
    public static final BigServerRackBlock RACK_ADVANCED = registerBig("server_rack_advanced");
    public static final MainframeRackBlock RACK_MAINFRAME = registerMainframe("server_rack_mainframe");

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
