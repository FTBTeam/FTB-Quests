package dev.ftb.mods.ftbquests.net;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import dev.architectury.networking.NetworkManager;

import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.NetUtils;

/**
 * Sent by client to move a task or reward left/right from the quest view panel
 * @param id ID of the task or reward
 * @param moveRight true to move right, false to move left
 */
public record ReorderItemMessage(long id, boolean moveRight) implements CustomPacketPayload {
    public static final Type<ReorderItemMessage> TYPE = new Type<>(FTBQuestsAPI.id("reorder_item"));
    public static final StreamCodec<FriendlyByteBuf, ReorderItemMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, ReorderItemMessage::id,
            ByteBufCodecs.BOOL, ReorderItemMessage::moveRight,
            ReorderItemMessage::new
    );

    public static void handle(ReorderItemMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (NetUtils.canEdit(context)) {
                QuestObjectBase object = ServerQuestFile.getInstance().getBase(message.id);
                if (object instanceof Task task) {
                    if (message.moveRight) {
                        task.getQuest().moveTaskRight(task);
                    } else {
                        task.getQuest().moveTaskLeft(task);
                    }
                    NetworkHelper.sendToAll(context.getPlayer().level().getServer(), ReorderItemResponseMessage.tasks(task.getQuest()));
                } else if (object instanceof Reward reward) {
                    if (message.moveRight) {
                        reward.getQuest().moveRewardRight(reward);
                    } else {
                        reward.getQuest().moveRewardLeft(reward);
                    }
                    NetworkHelper.sendToAll(context.getPlayer().level().getServer(), ReorderItemResponseMessage.rewards(reward.getQuest()));
                }
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
