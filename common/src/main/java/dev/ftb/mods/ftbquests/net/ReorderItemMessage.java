package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;

/**
 * Sent by client to move a task or reward left/right from the quest view panel
 */
public class ReorderItemMessage extends BaseC2SMessage {
    private final long id;
    private final boolean moveRight;

    public ReorderItemMessage(long id, boolean moveRight) {
        this.id = id;
        this.moveRight = moveRight;
    }

    public ReorderItemMessage(FriendlyByteBuf buf) {
        id = buf.readLong();
        moveRight = buf.readBoolean();
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.REORDER_ITEM;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(id);
        buf.writeBoolean(moveRight);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            if (NetUtils.canEdit(context)) {
                QuestObjectBase object = ServerQuestFile.INSTANCE.getBase(id);
                if (object instanceof Task task) {
                    if (moveRight) {
                        task.getQuest().moveTaskRight(task);
                    } else {
                        task.getQuest().moveTaskLeft(task);
                    }
                    ReorderItemResponseMessage.tasks(task.getQuest()).sendToAll(context.getPlayer().getServer());
                } else if (object instanceof Reward reward) {
                    if (moveRight) {
                        reward.getQuest().moveRewardRight(reward);
                    } else {
                        reward.getQuest().moveRewardLeft(reward);
                    }
                    ReorderItemResponseMessage.rewards(reward.getQuest()).sendToAll(context.getPlayer().getServer());
                }
            }
        });
    }
}
