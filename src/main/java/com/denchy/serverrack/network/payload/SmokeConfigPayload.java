package com.denchy.serverrack.network.payload;

import com.denchy.serverrack.ModId;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Shared config payload used both C2S and S2C, but we define separate IDs for clarity.
 * This one is C2S -> server updates config.
 */
public record SmokeConfigPayload(int frequency, int count, int radius, int rise, int density) implements CustomPayload {

    public static final CustomPayload.Id<SmokeConfigPayload> ID = new CustomPayload.Id<>(ModId.of("smoke_config"));
    public static final PacketCodec<PacketByteBuf, SmokeConfigPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, SmokeConfigPayload::frequency,
                    PacketCodecs.INTEGER, SmokeConfigPayload::count,
                    PacketCodecs.INTEGER, SmokeConfigPayload::radius,
                    PacketCodecs.INTEGER, SmokeConfigPayload::rise,
                    PacketCodecs.INTEGER, SmokeConfigPayload::density,
                    SmokeConfigPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
