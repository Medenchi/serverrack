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
 * Big rack - 3 blocks tall.
 */
public class BigServerRackBlock extends BlockWithEntity {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;
    public static final EnumProperty<TripleBlockPart> PART = EnumProperty.of("part", TripleBlockPart.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.of("active");

    private static final VoxelShape SHAPE = VoxelShapes.cuboid(0.05, 0.0, 0.05, 0.95, 1.0, 0.95);

    @Override
    protected MapCodec<? extends BlockWithEntity> getCodec() {
        return createCodec(BigServerRackBlock::new);
    }

    public BigServerRackBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(PART, TripleBlockPart.LOWER)
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

    @Nullable
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        BlockPos pos = ctx.getBlockPos();
        World world = ctx.getWorld();
        if (pos.getY() >= world.getTopY() - 2) return null;
        if (!world.getBlockState(pos.up()).canReplace(ctx)) return null;
        if (!world.getBlockState(pos.up(2)).canReplace(ctx)) return null;
        return getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(PART, TripleBlockPart.LOWER)
                .with(ACTIVE, false);
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        world.setBlockState(pos.up(), state.with(PART, TripleBlockPart.MIDDLE), Block.NOTIFY_ALL);
        world.setBlockState(pos.up(2), state.with(PART, TripleBlockPart.UPPER), Block.NOTIFY_ALL);
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        TripleBlockPart part = state.get(PART);
        BlockPos lower = lowerPos(state, pos);
        // break all 3
        for (int i = 0; i < 3; i++) {
            BlockPos p = lower.up(i);
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
        TripleBlockPart part = state.get(PART);
        if (part == TripleBlockPart.LOWER) return super.canPlaceAt(state, world, pos);
        BlockState below = world.getBlockState(pos.down());
        if (!below.isOf(this)) return false;
        if (part == TripleBlockPart.MIDDLE) return below.get(PART) == TripleBlockPart.LOWER;
        if (part == TripleBlockPart.UPPER) return below.get(PART) == TripleBlockPart.MIDDLE;
        return false;
    }

    public BlockPos lowerPos(BlockState state, BlockPos pos) {
        return switch (state.get(PART)) {
            case LOWER -> pos;
            case MIDDLE -> pos.down();
            case UPPER -> pos.down(2);
        };
    }

    public int height() { return 3; }

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
        return state.get(PART) == TripleBlockPart.LOWER ? new ServerRackBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
        if (state.get(PART) != TripleBlockPart.LOWER) return null;
        return validateTicker(type, ModBlockEntities.SERVER_RACK,
                (w,p,s,be) -> {
                    if (w.isClient) ServerRackBlockEntity.clientTick(w,p,s,(ServerRackBlockEntity)be);
                    else ServerRackBlockEntity.serverTick(w,p,s,(ServerRackBlockEntity)be);
                });
    }

    public static void toggleActive(World world, BlockPos lowerPos) {
        BlockState state = world.getBlockState(lowerPos);
        if (!(state.getBlock() instanceof BigServerRackBlock)) return;
        boolean now = !state.get(ACTIVE);
        for (int i = 0; i < 3; i++) {
            BlockPos p = lowerPos.up(i);
            BlockState s = world.getBlockState(p);
            if (s.getBlock() instanceof BigServerRackBlock) {
                world.setBlockState(p, s.with(ACTIVE, now), Block.NOTIFY_ALL);
            }
        }
        world.playSound(null, lowerPos, now ? SoundEvents.BLOCK_BEACON_ACTIVATE : SoundEvents.BLOCK_BEACON_DEACTIVATE,
                SoundCategory.BLOCKS, 0.5f, now ? 1.7f : 0.9f);
    }
}
