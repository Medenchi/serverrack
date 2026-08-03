package com.denchy.serverrack.network.payload;

import com.denchy.serverrack.ModId;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record BoomActionPayload(String action) implements CustomPayload {

    public static final CustomPayload.Id<BoomActionPayload> ID = new CustomPayload.Id<>(ModId.of("boom_action"));
    public static final PacketCodec<PacketByteBuf, BoomActionPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.STRING, BoomActionPayload::action,
                    BoomActionPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
