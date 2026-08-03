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

/**
 * Простой декоративный блок.
 * ВСЕГДА имеет facing. Без условной логики.
 */
public class DecorBlock extends Block {

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    private final VoxelShape shape;

    public DecorBlock(Settings settings, VoxelShape shape) {
        super(settings);
        this.shape = shape;
        // Устанавливаем дефолтный стейт сразу
        this.setDefaultState(this.getStateManager().getDefaultState().with(FACING, net.minecraft.util.math.Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return shape;
    }
}