package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public record SyncStructuresResponseMessage(List<String> data) implements CustomPacketPayload {
    public static final Type<SyncStructuresResponseMessage> TYPE = new Type<>(FTBQuestsAPI.rl("sync_structures_response_message"));

    public static final StreamCodec<FriendlyByteBuf, SyncStructuresResponseMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()), SyncStructuresResponseMessage::data,
            SyncStructuresResponseMessage::new
    );

    public static SyncStructuresResponseMessage create(MinecraftServer server) {
        List<String> data = new ArrayList<>();
        data.addAll(server.registryAccess()
                .registryOrThrow(Registries.STRUCTURE).registryKeySet().stream()
                .map(o -> o.location().toString())
                .sorted(String::compareTo)
                .toList()
        );
        data.addAll(server.registryAccess()
                .registryOrThrow(Registries.STRUCTURE).getTagNames()
                .map(o -> "#" + o.location())
                .sorted(String::compareTo)
                .toList()
        );
        return new SyncStructuresResponseMessage(data);
    }

    @Override
    public Type<SyncStructuresResponseMessage> type() {
        return TYPE;
    }

    public static void handle(SyncStructuresResponseMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> StructureTask.syncKnownStructureList(message.data));
    }
}
