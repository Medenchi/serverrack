package com.denchy.serverrack.network.payload;

import com.denchy.serverrack.ModId;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record AkErrorPayload(BlockPos pos, int radius) implements CustomPayload {

    public static final CustomPayload.Id<AkErrorPayload> ID = new CustomPayload.Id<>(ModId.of("ak_error"));
    public static final PacketCodec<PacketByteBuf, AkErrorPayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, AkErrorPayload::pos,
                    PacketCodecs.VAR_INT, AkErrorPayload::radius,
                    AkErrorPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
