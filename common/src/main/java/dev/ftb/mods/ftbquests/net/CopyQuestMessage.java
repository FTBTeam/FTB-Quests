package dev.ftb.mods.ftbquests.net;

import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.integration.PermissionsHelper;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.CreateOrDeleteRecord;
import dev.ftb.mods.ftbquests.quest.history.events.CreateQuestObjects;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record CopyQuestMessage(long id, long chapterId, double qx, double qy, boolean copyDeps) implements CustomPacketPayload {
    public static final Type<CopyQuestMessage> TYPE = new Type<>(FTBQuestsAPI.id("copy_quest_message"));

    public static final StreamCodec<FriendlyByteBuf, CopyQuestMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, CopyQuestMessage::id,
            ByteBufCodecs.VAR_LONG, CopyQuestMessage::chapterId,
            ByteBufCodecs.DOUBLE, CopyQuestMessage::qx,
            ByteBufCodecs.DOUBLE, CopyQuestMessage::qy,
            ByteBufCodecs.BOOL, CopyQuestMessage::copyDeps,
            CopyQuestMessage::new
    );

    @Override
    public Type<CopyQuestMessage> type() {
        return TYPE;
    }

    public static void handle(CopyQuestMessage message, PacketContext context) {
        ServerQuestFile.ifExists(sqf -> {
            if (PermissionsHelper.canPlayerEdit(context) && sqf.get(message.id) instanceof Quest toCopy && sqf.get(message.chapterId) instanceof Chapter chapter) {
                // deep copy of the quest
                Quest newQuest = QuestObjectBase.copy(toCopy, () -> new Quest(sqf.newID(), chapter));
                if (!message.copyDeps) {
                    newQuest.clearDependencies();
                }
                newQuest.setPosition(message.qx, message.qy);
                newQuest.setRawSubtitle(toCopy.getRawSubtitle());
                newQuest.setRawDescription(toCopy.getRawDescription());

                // deep copy of all tasks and rewards
                List<CreateOrDeleteRecord> newTasks = new ArrayList<>();
                toCopy.getTasks().forEach(task -> {
                    Task newTask = QuestObjectBase.copy(task, () -> Objects.requireNonNull(TaskType.createTask(sqf.newID(), newQuest, task.getType().getTypeForSerialization())));
                    newTasks.add(CreateOrDeleteRecord.ofQuestObject(newTask));
                });
                List<CreateOrDeleteRecord> newRewards = new ArrayList<>();
                for (Reward reward : toCopy.getRewards()) {
                    Reward newReward = QuestObjectBase.copy(reward, () -> Objects.requireNonNull(RewardType.createReward(sqf.newID(), newQuest, reward.getType().getTypeForSerialization())));
                    newRewards.add(CreateOrDeleteRecord.ofQuestObject(newReward));
                }

                List<CreateOrDeleteRecord> recs = new ArrayList<>();
                recs.add(CreateOrDeleteRecord.ofQuestObject(newQuest));
                recs.addAll(newTasks);
                recs.addAll(newRewards);

                sqf.getHistoryStack().addAndApply(sqf, new CreateQuestObjects(recs, null));
            }
        });
    }
}
