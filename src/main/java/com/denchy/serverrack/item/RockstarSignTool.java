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
            context.getPlayer().sendMessage(Text.literal("§a[Rockstar] Первая точка установлена"), false);
        } else {
            BlockPos pos2 = pos;
            // Создаём вывеску между двумя точками
            int minX = Math.min(pos1.getX(), pos2.getX());
            int maxX = Math.max(pos1.getX(), pos2.getX());
            int minY = Math.min(pos1.getY(), pos2.getY());
            int maxY = Math.max(pos1.getY(), pos2.getY());
            int minZ = Math.min(pos1.getZ(), pos2.getZ());
            int maxZ = Math.max(pos1.getZ(), pos2.getZ());

            // Простая реализация: ставим блок в центре
            BlockPos center = new BlockPos(
                    (minX + maxX) / 2,
                    (minY + maxY) / 2,
                    (minZ + maxZ) / 2
            );

            world.setBlockState(center, RockstarSignBlock.INSTANCE.getDefaultState());
            context.getPlayer().sendMessage(Text.literal("§a[Rockstar] Вывеска создана!"), false);
            pos1 = null;
        }
        return ActionResult.SUCCESS;
    }
}