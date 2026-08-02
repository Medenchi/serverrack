package com.denchy.serverrack.block.rockstar;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RockstarSignGenerator {

    public static void generateRockstarSign(World world, BlockPos min, BlockPos max) {
        int width = max.getX() - min.getX() + 1;
        int height = max.getY() - min.getY() + 1;
        int depth = max.getZ() - min.getZ() + 1;

        // Простая генерация слова ROCKSTAR (каждая буква 3x5)
        String text = "ROCKSTAR";
        int letterWidth = 3;
        int letterHeight = 5;
        int spacing = 1;

        int startX = min.getX();
        int startY = min.getY();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            int letterStartX = startX + i * (letterWidth + spacing);

            // Рисуем простую букву (заглушка, потом улучшим)
            for (int lx = 0; lx < letterWidth; lx++) {
                for (int ly = 0; ly < letterHeight; ly++) {
                    if (shouldDrawPixel(c, lx, ly)) {
                        BlockPos pos = new BlockPos(letterStartX + lx, startY + ly, min.getZ());
                        world.setBlockState(pos, RockstarSignBlock.INSTANCE.getDefaultState());
                    }
                }
            }
        }
    }

    private static boolean shouldDrawPixel(char c, int x, int y) {
        // Очень простая заглушка (все пиксели)
        return true;
    }
}