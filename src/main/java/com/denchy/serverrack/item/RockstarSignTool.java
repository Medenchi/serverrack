package com.denchy.serverrack.item;

import com.denchy.serverrack.block.rockstar.RockstarSignBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RockstarSignTool extends Item {

    private static BlockPos pos1 = null;
    private static int signType = 0; // 0 = ROCKSTAR, 1 = SERVER, 2 = PC, 3 = AK

    private static final String[] TYPES = {"ROCKSTAR", "SERVER", "PC", "AK"};

    public RockstarSignTool(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();

        if (world.isClient) return ActionResult.SUCCESS;

        if (context.getPlayer().isSneaking()) {
            // Смена типа вывески
            signType = (signType + 1) % TYPES.length;
            context.getPlayer().sendMessage(Text.literal("§e[Rockstar Tool] Тип: §b" + TYPES[signType]), false);
            return ActionResult.SUCCESS;
        }

        if (pos1 == null) {
            pos1 = pos;
            context.getPlayer().sendMessage(Text.literal("§a[Rockstar Tool] Первая точка §7(" + TYPES[signType] + ")"), false);
        } else {
            createSign(world, pos1, pos, TYPES[signType]);
            context.getPlayer().sendMessage(Text.literal("§a[Rockstar Tool] Вывеска создана!"), false);
            pos1 = null;
        }
        return ActionResult.SUCCESS;
    }

    private void createSign(World world, BlockPos p1, BlockPos p2, String type) {
        BlockPos min = new BlockPos(
                Math.min(p1.getX(), p2.getX()),
                Math.min(p1.getY(), p2.getY()),
                Math.min(p1.getZ(), p2.getZ())
        );
        BlockPos max = new BlockPos(
                Math.max(p1.getX(), p2.getX()),
                Math.max(p1.getY(), p2.getY()),
                Math.max(p1.getZ(), p2.getZ())
        );

        if (type.equals("ROCKSTAR")) {
            com.denchy.serverrack.block.rockstar.RockstarSignGenerator.generateRockstarSign(world, min, max);
        } else {
            // Для остальных типов пока просто заполняем
            for (int x = min.getX(); x <= max.getX(); x++) {
                for (int y = min.getY(); y <= max.getY(); y++) {
                    BlockPos pos = new BlockPos(x, y, min.getZ());
                    world.setBlockState(pos, RockstarSignBlock.INSTANCE.getDefaultState());
                }
            }
        }
    }
}