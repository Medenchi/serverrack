package com.denchy.serverrack.block;

import net.minecraft.util.StringIdentifiable;

/**
 * Parts of the 3x4 PC setup: desk row (3 blocks) + 3x3 monitor wall.
 * dx = offset along the placed axis (facing.rotateYClockwise), +1 = viewer-left.
 * dy = rows above the desk (desk = 0, bottom monitor row = 1, top = 3).
 */
public enum PcWallPart implements StringIdentifiable {
    DESK_LEFT("desk_left", 1, 0),
    DESK_CENTER("desk_center", 0, 0),
    DESK_RIGHT("desk_right", -1, 0),
    M_L1("m_l1", 1, 1),
    M_C1("m_c1", 0, 1),
    M_R1("m_r1", -1, 1),
    M_L2("m_l2", 1, 2),
    M_C2("m_c2", 0, 2),
    M_R2("m_r2", -1, 2),
    M_L3("m_l3", 1, 3),
    M_C3("m_c3", 0, 3),
    M_R3("m_r3", -1, 3);

    private final String name;
    private final int dx;
    private final int dy;

    PcWallPart(String name, int dx, int dy) {
        this.name = name;
        this.dx = dx;
        this.dy = dy;
    }

    @Override
    public String asString() { return name; }

    public int dx() { return dx; }
    public int dy() { return dy; }

    public boolean isDesk() {
        return dy == 0;
    }

    /** UV column of the shared wall texture: 0 = viewer-left, 2 = viewer-right. Monitors only. */
    public int uvCol() { return 1 - dx; }

    /** UV row of the shared wall texture: 0 = top, 2 = bottom. Monitors only. */
    public int uvRow() { return 3 - dy; }
}
