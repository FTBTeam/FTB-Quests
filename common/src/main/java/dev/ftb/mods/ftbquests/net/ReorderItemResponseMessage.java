package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseS2CMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.network.FriendlyByteBuf;

import java.util.List;
import java.util.Objects;

public final class ReorderItemResponseMessage extends BaseS2CMessage {
    private final long questId;
    private final List<Long> itemIds;
    private final boolean task;

    public ReorderItemResponseMessage(long questId, List<Long> itemIds, boolean task) {
        this.questId = questId;
        this.itemIds = itemIds;
        this.task = task;
    }

    public ReorderItemResponseMessage(FriendlyByteBuf buf) {
        questId = buf.readLong();
        itemIds = buf.readList(FriendlyByteBuf::readLong);
        task = buf.readBoolean();
    }

    public static ReorderItemResponseMessage tasks(Quest quest) {
        return new ReorderItemResponseMessage(quest.id, quest.getTasks().stream().map(QuestObjectBase::getId).toList(), true);
    }

    public static ReorderItemResponseMessage rewards(Quest quest) {
        return new ReorderItemResponseMessage(quest.id, quest.getRewards().stream().map(QuestObjectBase::getId).toList(), false);
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.REORDER_ITEM_RESPONSE;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(questId);
        buf.writeCollection(itemIds, FriendlyByteBuf::writeLong);
        buf.writeBoolean(task);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        context.queue(() -> {
            ClientQuestFile file = ClientQuestFile.INSTANCE;
            Quest q = file.getQuest(questId);
            if (q != null) {
                if (task) {
                    List<Task> tasks = itemIds.stream().map(file::getTask).filter(Objects::nonNull).toList();
                    if (tasks.size() == itemIds.size()) {
                        q.setTaskList(tasks);
                    }
                } else {
                    List<Reward> rewards = itemIds.stream().map(file::getReward).filter(Objects::nonNull).toList();
                    if (rewards.size() == itemIds.size()) {
                        q.setRewardList(rewards);
                    }
                }
                q.editedFromGUI();
            }
        });
    }
}
