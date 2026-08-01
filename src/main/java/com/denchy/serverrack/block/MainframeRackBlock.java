package com.denchy.serverrack.block;

import com.denchy.serverrack.blockentity.ServerRackBlockEntity;
import com.denchy.serverrack.registry.ModBlockEntities;
import net.minecraft.block.Block;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.ShapeContext;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
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
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

/**
 * Mainframe - 2 tall x 2 wide (4 blocks). Master is LOWER_LEFT.
 * Occupies footprint 2x1 in horizontal plane, perpendicular to facing.
 */
public class MainframeRackBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<MainframePart> PART = EnumProperty.of("part", MainframePart.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.0, 0.0, 0.0, 1.0, 1.0, 1.0);

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(MainframeRackBlock::new);
    }

    public MainframeRackBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(PART, MainframePart.LOWER_LEFT)
                .with(ACTIVE, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, ACTIVE);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        return SHAPE;
    }

    private Direction getRight(Direction facing) {
        return facing.rotateYClockwise();
    }

    private Direction getLeft(Direction facing) {
        return facing.rotateYCounterclockwise();
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        Direction facing = ctx.getHorizontalPlayerFacing().getOpposite();
        Direction right = getRight(facing);

        BlockPos rightPos = pos.offset(right);
        BlockPos upPos = pos.up();
        BlockPos upRightPos = rightPos.up();

        if (pos.getY() >= world.getTopY() - 1) return null;
        if (!world.getBlockState(pos).canReplace(ctx)) return null;
        if (!world.getBlockState(rightPos).canReplace(ctx)) return null;
        if (!world.getBlockState(upPos).canReplace(ctx)) return null;
        if (!world.getBlockState(upRightPos).canReplace(ctx)) return null;

        return getDefaultState()
                .with(FACING, facing)
                .with(PART, MainframePart.LOWER_LEFT)
                .with(ACTIVE, false);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        Direction facing = state.get(FACING);
        Direction right = getRight(facing);
        BlockPos rightPos = pos.offset(right);
        world.setBlockState(rightPos, state.with(PART, MainframePart.LOWER_RIGHT), Block.NOTIFY_ALL);
        world.setBlockState(pos.up(), state.with(PART, MainframePart.UPPER_LEFT), Block.NOTIFY_ALL);
        world.setBlockState(rightPos.up(), state.with(PART, MainframePart.UPPER_RIGHT), Block.NOTIFY_ALL);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        BlockPos lowerLeft = lowerPos(state, pos);
        Direction facing = state.get(FACING);
        Direction right = getRight(facing);
        BlockPos[] all = new BlockPos[]{
                lowerLeft,
                lowerLeft.offset(right),
                lowerLeft.up(),
                lowerLeft.offset(right).up()
        };
        for (BlockPos p : all) {
            if (p.equals(pos)) continue;
            BlockState s = world.getBlockState(p);
            if (s.getBlock() == this) {
                world.setBlockState(p, world.getFluidState(p).getBlockState(), Block.NOTIFY_ALL);
            }
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (state.get(PART) == MainframePart.LOWER_LEFT) return super.canPlaceAt(state, world, pos);
        BlockPos lowerLeft = lowerPos(state, pos);
        BlockState master = world.getBlockState(lowerLeft);
        return master.isOf(this) && master.get(PART) == MainframePart.LOWER_LEFT;
    }

    public BlockPos lowerPos(BlockState state, BlockPos pos) {
        MainframePart part = state.get(PART);
        Direction facing = state.get(FACING);
        Direction right = getRight(facing);
        Direction left = getLeft(facing);
        return switch (part) {
            case LOWER_LEFT -> pos;
            case LOWER_RIGHT -> pos.offset(left);
            case UPPER_LEFT -> pos.down();
            case UPPER_RIGHT -> pos.down().offset(left);
        };
    }

    public int height() { return 2; }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Nullable
    @Override
    public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return state.get(PART) == MainframePart.LOWER_LEFT ? new ServerRackBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (state.get(PART) != MainframePart.LOWER_LEFT) return null;
        return validateTicker(type, ModBlockEntities.SERVER_RACK,
                (w,p,s,be) -> {
                    if (w.isClient) ServerRackBlockEntity.clientTick(w,p,s,(ServerRackBlockEntity)be);
                    else ServerRackBlockEntity.serverTick(w,p,s,(ServerRackBlockEntity)be);
                });
    }

    public static void toggleActive(World world, BlockPos lowerLeftPos) {
        BlockState state = world.getBlockState(lowerLeftPos);
        if (!(state.getBlock() instanceof MainframeRackBlock)) return;
        boolean now = !state.get(ACTIVE);
        Direction facing = state.get(FACING);
        Direction right = facing.rotateYClockwise();
        BlockPos[] all = new BlockPos[]{
                lowerLeftPos,
                lowerLeftPos.offset(right),
                lowerLeftPos.up(),
                lowerLeftPos.offset(right).up()
        };
        for (BlockPos p : all) {
            BlockState s = world.getBlockState(p);
            if (s.getBlock() instanceof MainframeRackBlock) {
                world.setBlockState(p, s.with(ACTIVE, now), Block.NOTIFY_ALL);
            }
        }
        world.playSound(null, lowerLeftPos, now ? SoundEvents.BLOCK_BEACON_ACTIVATE : SoundEvents.BLOCK_BEACON_DEACTIVATE,
                SoundCategory.BLOCKS, 0.6f, now ? 1.5f : 0.7f);
    }
}
