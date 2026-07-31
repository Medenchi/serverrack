package com.denchy.serverrack.network.payload;

import com.denchy.serverrack.ModId;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;

public record ToggleRackPayload(BlockPos pos) implements CustomPayload {

    public static final CustomPayload.Id<ToggleRackPayload> ID = new CustomPayload.Id<>(ModId.of("toggle_rack"));
    public static final PacketCodec<PacketByteBuf, ToggleRackPayload> CODEC =
            PacketCodec.tuple(
                    BlockPos.PACKET_CODEC, ToggleRackPayload::pos,
                    ToggleRackPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
