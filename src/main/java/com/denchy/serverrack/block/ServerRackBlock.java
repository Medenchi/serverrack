package com.denchy.serverrack.block;

import com.denchy.serverrack.blockentity.ServerRackBlockEntity;
import com.denchy.serverrack.registry.ModBlockEntities;
import net.minecraft.block.Block;
import com.mojang.serialization.MapCodec;
import net.minecraft.block.BlockEntityProvider;
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
 * Small rack - 2 blocks tall.
 */
public class ServerRackBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<RackHalf> HALF = EnumProperty.of("half", RackHalf.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    private static final VoxelShape SHAPE =
            VoxelShapes.cuboid(0.0625, 0.0, 0.0625, 0.9375, 1.0, 0.9375);

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(ServerRackBlock::new);
    }

    public ServerRackBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(HALF, RackHalf.LOWER)
                .with(ACTIVE, false));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, ACTIVE);
    }

    @Override
    protected BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext ctx) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        if (pos.getY() >= world.getTopY() - 1) return null;
        if (!world.getBlockState(pos.up()).canReplace(ctx)) return null;
        return getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(HALF, RackHalf.LOWER)
                .with(ACTIVE, false);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        world.setBlockState(pos.up(), state.with(HALF, RackHalf.UPPER), Block.NOTIFY_ALL);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        RackHalf half = state.get(HALF);
        BlockPos other = half == RackHalf.LOWER ? pos.up() : pos.down();
        BlockState otherState = world.getBlockState(other);
        if (otherState.getBlock() == this && otherState.get(HALF) != half) {
            world.setBlockState(other, world.getFluidState(other).getBlockState(), Block.NOTIFY_ALL);
        }
        return super.onBreak(world, pos, state, player);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        if (state.get(HALF) == RackHalf.UPPER) {
            BlockState below = world.getBlockState(pos.down());
            return below.isOf(this) && below.get(HALF) == RackHalf.LOWER;
        }
        return super.canPlaceAt(state, world, pos);
    }

    public BlockPos lowerPos(BlockState state, BlockPos pos) {
        return state.get(HALF) == RackHalf.UPPER ? pos.down() : pos;
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
        return state.get(HALF) == RackHalf.LOWER
                ? new ServerRackBlockEntity(pos, state)
                : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (state.get(HALF) != RackHalf.LOWER) return null;
        return validateTicker(type, ModBlockEntities.SERVER_RACK,
                (w, p, s, be) -> {
                    if (w.isClient) {
                        ServerRackBlockEntity.clientTick(w, p, s, (ServerRackBlockEntity) be);
                    } else {
                        ServerRackBlockEntity.serverTick(w, p, s, (ServerRackBlockEntity) be);
                    }
                });
    }

    public static void toggleActive(World world, BlockPos lowerPos) {
        BlockState state = world.getBlockState(lowerPos);
        if (!(state.getBlock() instanceof ServerRackBlock)) return;
        boolean now = !state.get(ACTIVE);
        world.setBlockState(lowerPos, state.with(ACTIVE, now), Block.NOTIFY_ALL);
        BlockState upper = world.getBlockState(lowerPos.up());
        if (upper.getBlock() instanceof ServerRackBlock && upper.get(HALF) == RackHalf.UPPER) {
            world.setBlockState(lowerPos.up(), upper.with(ACTIVE, now), Block.NOTIFY_ALL);
        }
        world.playSound(null, lowerPos, now ? SoundEvents.BLOCK_BEACON_ACTIVATE
                : SoundEvents.BLOCK_BEACON_DEACTIVATE, SoundCategory.BLOCKS, 0.5f, now ? 1.6f : 0.8f);
    }
}
