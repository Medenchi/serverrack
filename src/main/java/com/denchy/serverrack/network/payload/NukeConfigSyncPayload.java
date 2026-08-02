package com.denchy.serverrack.network.payload;

import com.denchy.serverrack.ModId;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record NukeConfigSyncPayload(int stem, int cap, int ring, int density) implements CustomPayload {

    public static final CustomPayload.Id<NukeConfigSyncPayload> ID = new CustomPayload.Id<>(ModId.of("nuke_config_sync"));
    public static final PacketCodec<PacketByteBuf, NukeConfigSyncPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, NukeConfigSyncPayload::stem,
                    PacketCodecs.INTEGER, NukeConfigSyncPayload::cap,
                    PacketCodecs.INTEGER, NukeConfigSyncPayload::ring,
                    PacketCodecs.INTEGER, NukeConfigSyncPayload::density,
                    NukeConfigSyncPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }
}
