package com.denchy.serverrack.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

/**
 * 2×1 AK монитор (для GTA-сцены)
 */
public class AkMonitorBlock extends Block {
    public static final MapCodec<AkMonitorBlock> CODEC = createCodec(AkMonitorBlock::new);
    @Override protected MapCodec<? extends Block> getCodec() { return CODEC; }

    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final BooleanProperty ERROR = BooleanProperty.of("error");
    public static final EnumProperty<AkPart> PART = EnumProperty.of("part", AkPart.class);

    private static final VoxelShape SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0.0, 0.0, 0.4, 1.0, 1.0, 0.62),
            VoxelShapes.cuboid(0.32, 0.0, 0.45, 0.68, 0.4, 0.9)
    );

    public AkMonitorBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(ERROR, false)
                .with(PART, AkPart.BOTTOM));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, ERROR, PART);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    private static Direction axis(Direction facing) {
        return facing.rotateYClockwise();
    }

    public static BlockPos targetPos(BlockPos origin, Direction facing, AkPart part) {
        return origin.offset(axis(facing), part.dx()).up(part.dy());
    }

    public static BlockPos getOrigin(BlockState state, BlockPos pos) {
        Direction facing = state.get(FACING);
        AkPart part = state.get(PART);
        return pos.offset(axis(facing), -part.dx()).up(-part.dy());
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos origin = ctx.getBlockPos();
        World world = ctx.getWorld();
        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
        if (origin.getY() + 1 >= world.getTopY()) return null;
        for (AkPart part : AkPart.values()) {
            if (!world.getBlockState(targetPos(origin, facing, part)).canReplace(ctx)) return null;
        }
        return getDefaultState()
                .with(FACING, facing)
                .with(PART, AkPart.BOTTOM)
                .with(ERROR, false);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.get(FACING);
        for (AkPart part : AkPart.values()) {
            if (part == AkPart.BOTTOM) continue;
            world.setBlockState(targetPos(pos, facing, part), state.with(PART, part), Block.NOTIFY_ALL);
        }
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos origin = getOrigin(state, pos);
        Direction facing = state.get(FACING);
        for (AkPart part : AkPart.values()) {
            BlockPos t = targetPos(origin, facing, part);
            if (t.equals(pos)) continue;
            BlockState ts = world.getBlockState(t);
            if (ts.getBlock() == this && ts.get(PART) == part && ts.get(FACING) == facing) {
                world.setBlockState(t, world.getFluidState(t).getBlockState(), Block.NOTIFY_ALL);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    public static void setErrorStructure(World world, BlockPos origin, boolean error) {
        BlockState originState = world.getBlockState(origin);
        if (!(originState.getBlock() instanceof AkMonitorBlock)) return;
        Direction facing = originState.get(FACING);
        for (AkPart part : AkPart.values()) {
            BlockPos t = targetPos(origin, facing, part);
            BlockState ts = world.getBlockState(t);
            if (ts.getBlock() instanceof AkMonitorBlock && ts.get(FACING) == facing
                    && ts.get(PART) == part && ts.get(ERROR) != error) {
                world.setBlockState(t, ts.with(ERROR, error), Block.NOTIFY_ALL);
            }
        }
    }
}