package com.denchy.serverrack.block;

import com.mojang.serialization.MapCodec;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ItemActionResult;
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
    public static final MapCodec<AkChairBlock> CODEC = createCodec(AkChairBlock::new);
    @Override protected MapCodec<? extends Block> getCodec() { return CODEC; }

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

    /**
     * Build the invisible saddle with Marker:1b straight off NBT —
     * ArmorStandEntity.setMarker() is private since 1.20.5, and the marker
     * flag is what gives the stand a zero-size hitbox so the rider sits
     * exactly at the saddle point instead of balancing on its helmet.
     */
    private static ArmorStandEntity createSaddle(World world, BlockPos pos) {
        NbtCompound nbt = new NbtCompound();
        nbt.putString("id", "minecraft:armor_stand");
        nbt.putBoolean("Marker", true);
        nbt.putBoolean("Invisible", true);
        nbt.putBoolean("NoGravity", true);
        nbt.putBoolean("Silent", true);
        nbt.putBoolean("Invulnerable", true);
        NbtList posTag = new NbtList();
        posTag.add(NbtDouble.of(pos.getX() + 0.5));
        posTag.add(NbtDouble.of(pos.getY() + 0.33));
        posTag.add(NbtDouble.of(pos.getZ() + 0.5));
        nbt.put("Pos", posTag);
        Entity spawned = EntityType.getEntityFromNbt(nbt, world).orElse(null);
        return spawned instanceof ArmorStandEntity stand ? stand : null;
    }

    @Override
    protected ItemActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (player.isSneaking()) {
            return ItemActionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!world.isClient && player instanceof ServerPlayerEntity) {
            ArmorStandEntity seat = SEATS.get(pos);
            if (seat != null) {
                if (seat.hasPassengers()) {
                    return ItemActionResult.SUCCESS; // occupied, don't squeeze in
                }
                SEATS.remove(pos);
                seat.discard();
            }
            ArmorStandEntity saddle = createSaddle(world, pos);
            if (saddle == null) {
                return ItemActionResult.SUCCESS;
            }
            world.spawnEntity(saddle);
            player.startRiding(saddle, true);
            SEATS.put(pos.toImmutable(), saddle);
        }
        return ItemActionResult.SUCCESS;
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
