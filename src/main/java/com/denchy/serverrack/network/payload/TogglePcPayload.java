package com.denchy.serverrack.network.payload;

import com.denchy.serverrack.ModId;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record TogglePcPayload(BlockPos pos, int radius) implements CustomPayload {

    public static final CustomPayload.Id<TogglePcPayload> ID = new CustomPayload.Id<>(ModId.of("toggle_pc"));
    public static final PacketCodec<PacketByteBuf, TogglePcPayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, TogglePcPayload::pos,
                    PacketCodecs.VAR_INT, TogglePcPayload::radius,
                    TogglePcPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
