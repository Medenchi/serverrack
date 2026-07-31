package com.denchy.serverrack.block;

import net.minecraft.util.StringIdentifiable;

public enum MainframePart implements StringIdentifiable {
    LOWER_LEFT("lower_left"),
    LOWER_RIGHT("lower_right"),
    UPPER_LEFT("upper_left"),
    UPPER_RIGHT("upper_right");

    private final String name;
    MainframePart(String name) { this.name = name; }
    @Override public String asString() { return name; }

    public boolean isLower() {
        return this == LOWER_LEFT || this == LOWER_RIGHT;
    }
    public boolean isLeft() {
        return this == LOWER_LEFT || this == UPPER_LEFT;
    }
    public boolean isRight() {
        return this == LOWER_RIGHT || this == UPPER_RIGHT;
    }
    public boolean isUpper() {
        return this == UPPER_LEFT || this == UPPER_RIGHT;
    }
}
