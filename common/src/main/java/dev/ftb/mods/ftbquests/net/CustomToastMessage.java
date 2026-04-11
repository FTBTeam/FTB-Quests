package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.client.FTBQuestsNetClient;
import dev.ftb.mods.ftbquests.quest.reward.ToastReward;
import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import net.minecraft.network.FriendlyByteBuf;

public class CustomToastMessage extends BaseS2CMessage {
    private final long id;

    CustomToastMessage(FriendlyByteBuf buffer) {
        id = buffer.readLong();
    }

    public CustomToastMessage(long _id) {
        id = _id;
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.CUSTOM_TOAST_MESSAGE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(id);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (ClientQuestFile.exists() && ClientQuestFile.INSTANCE.getBase(this.id) instanceof ToastReward toastReward) {
                FTBQuestsNetClient.displayCustomToast(toastReward);
            }
        });
    }
}
