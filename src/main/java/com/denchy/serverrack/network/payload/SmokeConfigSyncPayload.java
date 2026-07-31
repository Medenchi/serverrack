package com.denchy.serverrack.network.payload;

import com.denchy.serverrack.ModId;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;

public record SmokeConfigSyncPayload(int frequency, int count, int radius, int rise, int density) implements CustomPayload {

    public static final CustomPayload.Id<SmokeConfigSyncPayload> ID = new CustomPayload.Id<>(ModId.of("smoke_config_sync"));
    public static final PacketCodec<PacketByteBuf, SmokeConfigSyncPayload> CODEC =
            PacketCodec.tuple(
                    PacketCodecs.INTEGER, SmokeConfigSyncPayload::frequency,
                    PacketCodecs.INTEGER, SmokeConfigSyncPayload::count,
                    PacketCodecs.INTEGER, SmokeConfigSyncPayload::radius,
                    PacketCodecs.INTEGER, SmokeConfigSyncPayload::rise,
                    PacketCodecs.INTEGER, SmokeConfigSyncPayload::density,
                    SmokeConfigSyncPayload::new
            );

    @Override
    public Id<? extends CustomPayload> getId() { return ID; }

    public static SmokeConfigSyncPayload from(com.denchy.serverrack.smoke.SmokeConfig cfg) {
        return new SmokeConfigSyncPayload(cfg.frequencyTicks, cfg.particlesPerEmit, cfg.ceilingRadius, cfg.maxRiseHeight, cfg.maxDensity);
    }

    public static SmokeConfigSyncPayload from(SmokeConfigPayload p) {
        return new SmokeConfigSyncPayload(p.frequency(), p.count(), p.radius(), p.rise(), p.density());
    }
}
