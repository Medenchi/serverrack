package com.denchy.serverrack.block;

import net.minecraft.util.StringIdentifiable;

public enum RackHalf implements StringIdentifiable {
    LOWER("lower"),
    UPPER("upper");

    private final String name;

    RackHalf(String name) {
        this.name = name;
    }

    @Override
    public String asString() {
        return name;
    }
}
