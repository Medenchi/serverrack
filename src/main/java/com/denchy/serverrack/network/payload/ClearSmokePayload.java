package com.denchy.serverrack.network.payload;

import com.denchy.serverrack.ModId;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record ClearSmokePayload() implements CustomPayload {

    public static final CustomPayload.Id<ClearSmokePayload> ID = new CustomPayload.Id<>(ModId.of("clear_smoke_c2s"));
    public static final PacketCodec<PacketByteBuf, ClearSmokePayload> CODEC = PacketCodec.unit(new ClearSmokePayload());

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
