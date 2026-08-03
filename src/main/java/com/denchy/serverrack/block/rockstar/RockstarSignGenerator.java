package com.denchy.serverrack.block.rockstar;

import com.denchy.serverrack.registry.ModBlocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class RockstarSignGenerator {

    // Простой 5x3 шрифт для ROCKSTAR
    private static final boolean[][] R = {
            {true,true,true}, {true,false,true}, {true,true,true}, {true,false,true}, {true,false,true}
    };
    private static final boolean[][] O = {
            {true,true,true}, {true,false,true}, {true,false,true}, {true,false,true}, {true,true,true}
    };
    private static final boolean[][] C = {
            {true,true,true}, {true,false,false}, {true,false,false}, {true,false,false}, {true,true,true}
    };
    private static final boolean[][] K = {
            {true,false,true}, {true,true,false}, {true,false,false}, {true,true,false}, {true,false,true}
    };
    private static final boolean[][] S = {
            {true,true,true}, {true,false,false}, {true,true,true}, {false,false,true}, {true,true,true}
    };
    private static final boolean[][] T = {
            {true,true,true}, {false,true,false}, {false,true,false}, {false,true,false}, {false,true,false}
    };
    private static final boolean[][] A = {
            {false,true,false}, {true,false,true}, {true,true,true}, {true,false,true}, {true,false,true}
    };

    public static void generateRockstarSign(World world, BlockPos min, BlockPos max) {
        int startX = min.getX();
        int startY = min.getY();
        int startZ = min.getZ();

        char[] letters = {'R','O','C','K','S','T','A','R'};
        boolean[][][] font = {R, O, C, K, S, T, A, R};

        int letterWidth = 3;
        int letterHeight = 5;
        int spacing = 1;

        for (int i = 0; i < letters.length; i++) {
            int letterStartX = startX + i * (letterWidth + spacing);
            boolean[][] letter = font[i];

            for (int ly = 0; ly < letterHeight; ly++) {
                for (int lx = 0; lx < letterWidth; lx++) {
                    if (letter[ly][lx]) {
                        BlockPos pos = new BlockPos(letterStartX + lx, startY + ly, startZ);
                        world.setBlockState(pos, ModBlocks.ROCKSTAR_SIGN.getDefaultState());
                    }
                }
            }
        }
    }
}
