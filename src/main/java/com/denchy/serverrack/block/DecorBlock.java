package com.denchy.serverrack.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;

/** Generic small room decor: fixed voxel shape, optional horizontal facing. */
public class DecorBlock extends Block {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    private final VoxelShape shape;
    private final boolean hasFacing;

    public DecorBlock(Settings settings, VoxelShape shape, boolean hasFacing) {
        super(settings);
        this.shape = shape;
        this.hasFacing = hasFacing;
        if (hasFacing) {
            setDefaultState(getStateManager().getDefaultState().with(FACING,
                    net.minecraft.util.math.Direction.NORTH));
        }
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        if (hasFacing) {
            builder.add(FACING);
        }
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shape;
    }

    @Override
    public BlockState getPlacementState(net.minecraft.item.ItemPlacementContext ctx) {
        if (!hasFacing) return getDefaultState();
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
}
