package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;

public class CopyQuestMessage extends BaseC2SMessage {
    private final long id;
    private final long chapterId;
    private final double qx;
    private final double qy;
    private final boolean copyDeps;

    public CopyQuestMessage(Quest toCopy, Chapter chapter, double qx, double qy, boolean copyDeps) {
        id = toCopy.id;
        chapterId = chapter.id;
        this.qx = qx;
        this.qy = qy;
        this.copyDeps = copyDeps;
    }

    public CopyQuestMessage(FriendlyByteBuf buf) {
        this.id = buf.readLong();
        this.chapterId = buf.readLong();
        this.qx = buf.readDouble();
        this.qy = buf.readDouble();
        this.copyDeps = buf.readBoolean();
    }

    @Override
    public MessageType getType() {
        return FTBQuestsNetHandler.COPY_QUEST;
    }

    @Override
    public void write(FriendlyByteBuf buf) {
        buf.writeLong(id);
        buf.writeLong(chapterId);
        buf.writeDouble(qx);
        buf.writeDouble(qy);
        buf.writeBoolean(copyDeps);
    }

    @Override
    public void handle(NetworkManager.PacketContext context) {
        BaseQuestFile file = ServerQuestFile.INSTANCE;
        if (file.get(id) instanceof Quest toCopy && file.get(chapterId) instanceof Chapter chapter) {
            // deep copy of the quest
            Quest newQuest = Objects.requireNonNull(QuestObjectBase.copy(toCopy, () -> new Quest(file.newID(), chapter)));
            if (!copyDeps) {
                newQuest.clearDependencies();
            }
            newQuest.setX(qx);
            newQuest.setY(qy);
            newQuest.onCreated();

            // deep copy of all tasks and rewards
            toCopy.getTasks().forEach(task -> {
                Task newTask = QuestObjectBase.copy(task, () -> TaskType.createTask(file.newID(), newQuest, task.getType().getTypeForNBT()));
                if (newTask != null) {
                    newTask.onCreated();
                }
            });
            for (Reward reward : toCopy.getRewards()) {
                Reward newReward = QuestObjectBase.copy(reward, () -> RewardType.createReward(file.newID(), newQuest, reward.getType().getTypeForNBT()));
                if (newReward != null) {
                    newReward.onCreated();
                }
            }

            // sync new objects to clients
            MinecraftServer server = context.getPlayer().getServer();
            new CreateObjectResponseMessage(newQuest, null).sendToAll(server);
            newQuest.getTasks().forEach(task -> {
                CompoundTag extra = new CompoundTag();
                extra.putString("type", task.getType().getTypeForNBT());
                new CreateObjectResponseMessage(task, extra).sendToAll(server);
            });
            newQuest.getRewards().forEach(reward -> {
                CompoundTag extra = new CompoundTag();
                extra.putString("type", reward.getType().getTypeForNBT());
                new CreateObjectResponseMessage(reward, extra).sendToAll(server);
            });

            // and update the server quest map etc.
            ServerQuestFile.INSTANCE.refreshIDMap();
            ServerQuestFile.INSTANCE.clearCachedData();
            ServerQuestFile.INSTANCE.markDirty();
        }
    }
}
