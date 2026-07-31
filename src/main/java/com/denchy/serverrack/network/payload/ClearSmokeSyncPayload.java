package com.denchy.serverrack.network.payload;

import com.denchy.serverrack.ModId;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;

public record ClearSmokeSyncPayload() implements CustomPayload {

    public static final CustomPayload.Id<ClearSmokeSyncPayload> ID = new CustomPayload.Id<>(ModId.of("clear_smoke_s2c"));
    public static final PacketCodec<PacketByteBuf, ClearSmokeSyncPayload> CODEC = PacketCodec.unit(new ClearSmokeSyncPayload());

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
