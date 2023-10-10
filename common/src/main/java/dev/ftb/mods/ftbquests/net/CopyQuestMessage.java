package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.architectury.networking.simple.BaseC2SMessage;
import dev.architectury.networking.simple.MessageType;
import dev.ftb.mods.ftbquests.FTBQuests;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.Quest;
import dev.ftb.mods.ftbquests.quest.QuestFile;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.MinecraftServer;

public class CopyQuestMessage extends BaseC2SMessage {
    private final long id;
    private final long chapterId;
    private final double qx;
    private final double qy;
    private boolean copyDeps;

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

    public void setCopyDeps(boolean newCopyDeps) {
        this.copyDeps = newCopyDeps;
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
        QuestFile file = FTBQuests.PROXY.getQuestFile(false);
        if (file.get(id) instanceof Quest toCopy && file.get(chapterId) instanceof Chapter chapter) {
            // deep copy of the quest
            CompoundTag tag = new CompoundTag();
            toCopy.writeData(tag);
            Quest newQuest = new Quest(chapter);
            newQuest.readData(tag);
            if (!copyDeps) {
                newQuest.clearDependencies();
            }
            newQuest.id = file.newID();
            newQuest.x = qx;
            newQuest.y = qy;
            newQuest.onCreated();

            // deep copy of all tasks and rewards
            for (Task task : toCopy.tasks) {
                Task newTask = TaskType.createTask(newQuest, task.getType().getTypeForNBT());
                if (newTask != null) {
                    CompoundTag tag1 = new CompoundTag();
                    task.writeData(tag1);
                    newTask.readData(tag1);
                    newTask.id = file.newID();
                    newTask.onCreated();
                }
            }
            for (Reward reward : toCopy.rewards) {
                Reward newReward = RewardType.createReward(newQuest, reward.getType().getTypeForNBT());
                if (newReward != null) {
                    CompoundTag tag1 = new CompoundTag();
                    reward.writeData(tag1);
                    newReward.readData(tag1);
                    newReward.id = file.newID();
                    newReward.onCreated();
                }
            }

            // sync new objects to clients
            MinecraftServer server = context.getPlayer().getServer();
            new CreateObjectResponseMessage(newQuest, null).sendToAll(server);
            newQuest.tasks.forEach(task -> {
                CompoundTag extra = new CompoundTag();
                extra.putString("type", task.getType().getTypeForNBT());
                new CreateObjectResponseMessage(task, extra).sendToAll(server);
            });
            newQuest.rewards.forEach(reward -> {
                CompoundTag extra = new CompoundTag();
                extra.putString("type", reward.getType().getTypeForNBT());
                new CreateObjectResponseMessage(reward, extra).sendToAll(server);
            });

            // and update the server quest map etc.
            ServerQuestFile.INSTANCE.refreshIDMap();
            ServerQuestFile.INSTANCE.clearCachedData();
            ServerQuestFile.INSTANCE.save();
        }
    }
}
