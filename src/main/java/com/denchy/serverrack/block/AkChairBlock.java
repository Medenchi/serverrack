package com.denchy.serverrack.block;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

/**
 * Gaming chair. RMB = sit (invisible marker armor stand as the saddle),
 * sneak = stand up (vanilla dismount). One ass per chair.
 */
public class AkChairBlock extends Block {
    public static final DirectionProperty FACING = Properties.HORIZONTAL_FACING;

    private static final VoxelShape SHAPE = VoxelShapes.union(
            VoxelShapes.cuboid(0.1875, 0.4, 0.1875, 0.8125, 0.55, 0.8125), // seat
            VoxelShapes.cuboid(0.1875, 0.55, 0.72, 0.8125, 1.15, 0.9)      // backrest
    );

    /** pos -> the saddle. Janitor-thinned in the tick loop. */
    private static final java.util.Map<BlockPos, ArmorStandEntity> SEATS = new java.util.concurrent.ConcurrentHashMap<>();

    public AkChairBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (player.isSneaking()) {
            return ActionResult.PASS;
        }
        if (!world.isClient && player instanceof ServerPlayerEntity) {
            ArmorStandEntity seat = SEATS.get(pos);
            if (seat != null) {
                if (seat.hasPassengers()) {
                    return ActionResult.SUCCESS; // occupied, don't squeeze in
                }
                SEATS.remove(pos);
                seat.discard();
            }
            ArmorStandEntity saddle = new ArmorStandEntity(world,
                    pos.getX() + 0.5, pos.getY() + 0.26, pos.getZ() + 0.5);
            saddle.setInvisible(true);
            saddle.setMarker(true);
            saddle.setNoGravity(true);
            world.spawnEntity(saddle);
            player.startRiding(saddle, true);
            SEATS.put(pos.toImmutable(), saddle);
        }
        return ActionResult.SUCCESS;
    }

    @Override
    public BlockState onBreak(World world, BlockPos pos, BlockState state, PlayerEntity player) {
        ArmorStandEntity seat = SEATS.remove(pos);
        if (seat != null) {
            seat.discard(); // riders dismount automatically when the saddle dies
        }
        return super.onBreak(world, pos, state, player);
    }

    /** Called once from the mod initializer. */
    public static void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            // every 40 ticks: sweep empty saddles so chairs never ghost
            if (server.getTicks() % 40 != 0) return;
            SEATS.forEach((pos, seat) -> {
                if (!seat.hasPassengers() || !seat.isAlive()) {
                    seat.discard();
                }
            });
            SEATS.entrySet().removeIf(e -> !e.getValue().hasPassengers());
        });
    }
}
