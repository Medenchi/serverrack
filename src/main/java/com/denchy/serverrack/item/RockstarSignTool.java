package com.denchy.serverrack.item;

import com.denchy.serverrack.block.rockstar.RockstarSignBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RockstarSignTool extends Item {

    private static BlockPos pos1 = null;
    private static String currentType = "rockstar"; // rockstar, server, pc, ak

    public RockstarSignTool(Settings settings) {
        super(settings);
    }

    @Override
    public ActionResult useOnBlock(ItemUsageContext context) {
        World world = context.getWorld();
        BlockPos pos = context.getBlockPos();

        if (world.isClient) return ActionResult.SUCCESS;

        if (pos1 == null) {
            pos1 = pos;
            context.getPlayer().sendMessage(Text.literal("§a[Rockstar Tool] Первая точка установлена §7(" + currentType + ")"), false);
        } else {
            BlockPos pos2 = pos;
            createSign(world, pos1, pos2, currentType);
            context.getPlayer().sendMessage(Text.literal("§a[Rockstar Tool] Вывеска создана!"), false);
            pos1 = null;
        }
        return ActionResult.SUCCESS;
    }

    private void createSign(World world, BlockPos p1, BlockPos p2, String type) {
        int minX = Math.min(p1.getX(), p2.getX());
        int maxX = Math.max(p1.getX(), p2.getX());
        int minY = Math.min(p1.getY(), p2.getY());
        int maxY = Math.max(p1.getY(), p2.getY());
        int minZ = Math.min(p1.getZ(), p2.getZ());
        int maxZ = Math.max(p1.getZ(), p2.getZ());

        int width = maxX - minX + 1;
        int height = maxY - minY + 1;

        // Простая генерация вывески ROCKSTAR (позже сделаем нормальные буквы)
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                BlockPos placePos = new BlockPos(minX + x, minY + y, minZ);
                if (type.equals("rockstar")) {
                    world.setBlockState(placePos, RockstarSignBlock.INSTANCE.getDefaultState());
                }
            }
        }
    }

    // Можно будет расширить: выбор типа через shift + ПКМ
}