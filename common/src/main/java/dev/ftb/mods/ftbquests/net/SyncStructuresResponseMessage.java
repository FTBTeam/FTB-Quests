package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.task.StructureTask;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;

import java.util.ArrayList;
import java.util.List;

public class SyncStructuresResponseMessage extends BaseS2CMessage {
    private final List<String> data = new ArrayList<>();

    public SyncStructuresResponseMessage(MinecraftServer server) {
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
    }

    public SyncStructuresResponseMessage(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        for (int i = 0; i < size; i++) {
            data.add(buf.readUtf(Short.MAX_VALUE));
        }
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.SYNC_STRUCTURES_RESPONSE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeVarInt(data.size());
        data.forEach(buf::writeUtf);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        StructureTask.syncKnownStructureList(data);
    }
}
