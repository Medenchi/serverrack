package com.denchy.serverrack.block;

import net.minecraft.util.StringIdentifiable;

/**
 * 2×1 AK monitor (для GTA-сцены)
 */
public enum AkPart implements StringIdentifiable {
    BOTTOM("bottom", 0, 0),
    TOP("top", 0, 1);

    private final String name;
    private final int dx;
    private final int dy;

    AkPart(String name, int dx, int dy) {
        this.name = name;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public String asString() { return name; }

    public int dx() { return dx; }
    public int dy() { return dy; }
}