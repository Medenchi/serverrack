package com.denchy.serverrack.network.payload;

import com.denchy.serverrack.ModId;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record NukeConfigPayload(int stem, int cap, int ring, int density) implements CustomPayload {

    public static final CustomPayload.Id<NukeConfigPayload> ID = new CustomPayload.Id<>(ModId.of("nuke_config"));
    public static final PacketCodec<PacketByteBuf, NukeConfigPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, NukeConfigPayload::stem,
                    PacketCodecs.INTEGER, NukeConfigPayload::cap,
                    PacketCodecs.INTEGER, NukeConfigPayload::ring,
                    PacketCodecs.INTEGER, NukeConfigPayload::density,
                    NukeConfigPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
