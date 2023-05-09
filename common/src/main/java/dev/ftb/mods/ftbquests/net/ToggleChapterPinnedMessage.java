package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.TeamData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public class ToggleChapterPinnedMessage extends BaseC2SMessage {
    public ToggleChapterPinnedMessage() {
    }

    public ToggleChapterPinnedMessage(FriendlyByteBuf buf) {
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.TOGGLE_CHAPTER_PINNED;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        ServerPlayer player = (ServerPlayer) context.getPlayer();
        TeamData data = ServerQuestFile.INSTANCE.getData(player);
        boolean newPinned = !data.isChapterPinned(player);
        data.setChapterPinned(player, newPinned);
        new ToggleChapterPinnedResponseMessage(newPinned).sendTo(player);
    }
}
