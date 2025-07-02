package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.ClientQuestFile;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.List;
import java.util.Objects;

public record ReorderItemResponseMessage(long questId, List<Long> itemIds, boolean task) implements CustomPacketPayload {
    public static final Type<ReorderItemResponseMessage> TYPE = new Type<>(FTBQuestsAPI.rl("reorder_item_response"));
    public static final StreamCodec<FriendlyByteBuf, ReorderItemResponseMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, ReorderItemResponseMessage::questId,
            ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list()), ReorderItemResponseMessage::itemIds,
            ByteBufCodecs.BOOL, ReorderItemResponseMessage::task,
            ReorderItemResponseMessage::new
    );

    public static ReorderItemResponseMessage tasks(Quest quest) {
        return new ReorderItemResponseMessage(quest.id, quest.getTasks().stream().map(QuestObjectBase::getId).toList(), true);
    }

    public static ReorderItemResponseMessage rewards(Quest quest) {
        return new ReorderItemResponseMessage(quest.id, quest.getRewards().stream().map(QuestObjectBase::getId).toList(), false);
    }

    public static void handle(ReorderItemResponseMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            ClientQuestFile file = ClientQuestFile.INSTANCE;
            Quest q = file.getQuest(message.questId);
            if (q != null) {
                if (message.task) {
                    List<Task> tasks = message.itemIds.stream().map(file::getTask).filter(Objects::nonNull).toList();
                    if (tasks.size() == message.itemIds.size()) {
                        q.setTaskList(tasks);
                    }
                } else {
                    List<Reward> rewards = message.itemIds.stream().map(file::getReward).filter(Objects::nonNull).toList();
                    if (rewards.size() == message.itemIds.size()) {
                        q.setRewardList(rewards);
                    }
                }
                q.editedFromGUI();
            }
        });
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
