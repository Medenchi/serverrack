package com.denchy.serverrack.block;

import net.minecraft.util.StringIdentifiable;

/**
 * Parts of the 2x2 AK monitor: dx = cells along the placed axis
 * (facing.rotateYClockwise, +1 = viewer-left), dy = rows above the floor.
 * origin = viewer-right-bottom ("r0").
 */
public enum AkPart implements StringIdentifiable {
    R_BOTTOM("r0", 0, 0),
    L_BOTTOM("l0", 1, 0),
    R_TOP("r1", 0, 1),
    L_TOP("l1", 1, 1);

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
