package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import net.minecraft.network.FriendlyByteBuf;

public class ToggleChapterPinnedResponseMessage extends BaseS2CMessage {
    private final boolean pinned;

    public ToggleChapterPinnedResponseMessage(boolean pinned) {
        this.pinned = pinned;
    }

    ToggleChapterPinnedResponseMessage(FriendlyByteBuf buffer) {
        pinned = buffer.readBoolean();
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.TOGGLE_CHAPTER_PINNED_RESPONSE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeBoolean(pinned);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        FTBQuestsNetClient.toggleChapterPinned(pinned);
    }
}
