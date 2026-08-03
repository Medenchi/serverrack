package com.denchy.serverrack.block.rockstar;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;

public class RockstarSignBlock extends Block {
    public static final RockstarSignBlock INSTANCE = new RockstarSignBlock(Settings.create().strength(3.0f).nonOpaque().luminance(s -> 8));

    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0, 0, 0, 1, 1, 0.4);

    public RockstarSignBlock(Settings settings) {
        super(settings);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }
}