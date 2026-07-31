package com.denchy.serverrack.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * PC with a 3x3 monitor video wall on a desk.
 * Single block. ALERT state = the wall shows one giant message instead of matrix rain.
 */
public class PcWallBlock extends Block {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty ALERT = BooleanProperty.of("alert");

    private static final VoxelShape SHAPE = VoxelShapes.union(
            // Desk top + cabinets
            VoxelShapes.cuboid(0.03125, 0.0, 0.15625, 0.96875, 0.359375, 0.90625),
            // Monitor wall (sits on the desk, centered depth-wise)
            VoxelShapes.cuboid(0.03125, 0.40625, 0.453125, 0.96875, 0.953125, 0.546875)
    );

    public PcWallBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(ALERT, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ALERT);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(ALERT, false);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    /** Set ALERT on/off without sound (used by radius toggles). */
    public static void setAlert(World world, BlockPos pos, boolean alert) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof PcWallBlock)) return;
        if (state.get(ALERT) == alert) return;
        world.setBlockState(pos, state.with(ALERT, alert), Block.NOTIFY_ALL);
    }

    public static void playAlertSound(World world, BlockPos pos, boolean alert) {
        world.playSound(null, pos,
                alert ? SoundEvents.BLOCK_BEACON_POWER_SELECT : SoundEvents.BLOCK_BEACON_DEACTIVATE,
                SoundCategory.BLOCKS, 0.8f, alert ? 0.5f : 1.2f);
    }

    public static void toggleAlert(World world, BlockPos pos) {
        BlockState state = world.getBlockState(pos);
        if (!(state.getBlock() instanceof PcWallBlock)) return;
        boolean now = !state.get(ALERT);
        setAlert(world, pos, now);
        playAlertSound(world, pos, now);
    }
}
