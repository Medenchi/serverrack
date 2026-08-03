package com.denchy.serverrack.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * Full PC setup, 3 blocks wide x 4 tall: desk row with pc tower + 3x3 monitor video wall.
 * Placed as a single item (unfolds like the mainframe), breaks as a whole.
 * ALERT state = the whole wall shows one giant message instead of matrix rain.
 */
public class PcWallBlock extends Block {
    public static final MapCodec<PcWallBlock> CODEC = createCodec(PcWallBlock::new);
    @Override protected MapCodec<? extends Block> getCodec() { return CODEC; }

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty ALERT = BooleanProperty.of("alert");
    public static final EnumProperty<PcWallPart> PART = EnumProperty.of("part", PcWallPart.class);

    private static final VoxelShape DESK_SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.8125, 0.0, 1.0, 0.8875, 1.0),          // tabletop
            VoxelShapes.cuboid(0.03125, 0.0, 0.21875, 0.96875, 0.8125, 0.96875) // cabinet/tower silhouette
    );
    private static final VoxelShape MONITOR_SHAPE =
            VoxelShapes.cuboid(0.0, 0.0, 0.5, 1.0, 1.0, 1.0);               // wall chassis in back half

    public PcWallBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(ALERT, false)
                .with(PART, PcWallPart.DESK_CENTER));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ALERT, PART);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return state.get(PART).isDesk() ? DESK_SHAPE : MONITOR_SHAPE;
    }

    /** Horizontal axis the 3-wide layout is spread along. +1 = viewer-left. */
    private static Direction axis(Direction facing) {
        return facing.rotateYClockwise();
    }

    public static BlockPos targetPos(BlockPos origin, Direction facing, PcWallPart part) {
        return origin.offset(axis(facing), part.dx()).up(part.dy());
    }

    /** Position of the DESK_CENTER of the structure this block belongs to. */
    public static BlockPos getOrigin(BlockState state, BlockPos pos) {
        Direction facing = state.get(FACING);
        PcWallPart part = state.get(PART);
        return pos.offset(axis(facing), -part.dx()).up(-part.dy());
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos origin = ctx.getBlockPos();
        World world = ctx.getWorld();
        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
        if (origin.getY() + 3 >= world.getTopY()) return null;
        for (PcWallPart part : PcWallPart.values()) {
            if (!world.getBlockState(targetPos(origin, facing, part)).canReplace(ctx)) return null;
        }
        return getDefaultState()
                .with(FACING, facing)
                .with(PART, PcWallPart.DESK_CENTER)
                .with(ALERT, false);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.get(FACING);
        for (PcWallPart part : PcWallPart.values()) {
            if (part == PcWallPart.DESK_CENTER) continue;
            world.setBlockState(targetPos(pos, facing, part), state.with(PART, part), Block.NOTIFY_ALL);
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos origin = getOrigin(state, pos);
        Direction facing = state.get(FACING);
        for (PcWallPart part : PcWallPart.values()) {
            BlockPos t = targetPos(origin, facing, part);
            if (t.equals(pos)) continue;
            BlockState ts = world.getBlockState(t);
            if (ts.getBlock() == this && ts.get(PART) == part && ts.get(FACING) == facing) {
                world.setBlockState(t, world.getFluidState(t).getBlockState(), Block.NOTIFY_ALL);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        // Multiblock: rotating only the facing would skew the grid; keep parts as-is
        // (same simplification as the mainframe rack).
        return state;
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state;
    }

    /** Set ALERT on every part of the structure that has this origin. */
    public static void setAlertStructure(World world, BlockPos origin, boolean alert) {
        BlockState originState = world.getBlockState(origin);
        if (!(originState.getBlock() instanceof PcWallBlock)) return;
        Direction facing = originState.get(FACING);
        for (PcWallPart part : PcWallPart.values()) {
            BlockPos t = targetPos(origin, facing, part);
            BlockState ts = world.getBlockState(t);
            if (ts.getBlock() instanceof PcWallBlock && ts.get(FACING) == facing
                    && ts.get(PART) == part && ts.get(ALERT) != alert) {
                world.setBlockState(t, ts.with(ALERT, alert), Block.NOTIFY_ALL);
            }
        }
    }

    public static void playAlertSound(World world, BlockPos pos, boolean alert) {
        world.playSound(null, pos,
                alert ? SoundEvents.BLOCK_BEACON_POWER_SELECT : SoundEvents.BLOCK_BEACON_DEACTIVATE,
                SoundCategory.BLOCKS, 0.8f, alert ? 0.5f : 1.2f);
    }
}
