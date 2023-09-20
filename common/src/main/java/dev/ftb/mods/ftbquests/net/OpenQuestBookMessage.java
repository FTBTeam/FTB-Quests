package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import net.minecraft.network.FriendlyByteBuf;

public class OpenQuestBookMessage extends BaseS2CMessage {
    private final long id;

    public OpenQuestBookMessage(long id) {
        this.id = id;
    }

    OpenQuestBookMessage(FriendlyByteBuf buf) {
        id = buf.readLong();
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.OPEN_QUEST_BOOK;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(id);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        ClientQuestFile.openBookToQuestObject(id);
    }
}
