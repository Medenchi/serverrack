package com.denchy.serverrack.block;

import net.minecraft.util.StringIdentifiable;

public enum TripleBlockPart implements StringIdentifiable {
    LOWER("lower"),
    MIDDLE("middle"),
    UPPER("upper");

    private final String name;
    TripleBlockPart(String name) { this.name = name; }

    @Override public String asString() { return name; }
}
