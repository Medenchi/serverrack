package com.denchy.serverrack;

import net.minecraft.util.Identifier;

public final class ModId {
    private ModId() {}

    public static Identifier of(String path) {
        return Identifier.of(ServerRackMod.MOD_ID, path);
    }
}
