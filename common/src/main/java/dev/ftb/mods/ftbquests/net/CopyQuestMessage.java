package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.client.FTBQuestsClient;
import dev.ftb.mods.ftbquests.quest.*;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.reward.RewardType;
import dev.ftb.mods.ftbquests.quest.task.Task;
import dev.ftb.mods.ftbquests.quest.task.TaskType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.MinecraftServer;

import java.util.Objects;

public record CopyQuestMessage(long id, long chapterId, double qx, double qy, boolean copyDeps) implements CustomPacketPayload {
    public static final Type<CopyQuestMessage> TYPE = new Type<>(FTBQuestsAPI.rl("copy_quest_message"));

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

    public static void handle(CopyQuestMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> {
            BaseQuestFile file = ServerQuestFile.INSTANCE;
            if (file.get(message.id) instanceof Quest toCopy && file.get(message.chapterId) instanceof Chapter chapter) {
                // deep copy of the quest
                Quest newQuest = Objects.requireNonNull(QuestObjectBase.copy(toCopy,
                        () -> new Quest(file.newID(), chapter),
                        FTBQuestsClient.holderLookup()));
                if (!message.copyDeps) {
                    newQuest.clearDependencies();
                }
                newQuest.setX(message.qx);
                newQuest.setY(message.qy);
                newQuest.onCreated();

                // deep copy of all tasks and rewards
                toCopy.getTasks().forEach(task -> {
                    Task newTask = QuestObjectBase.copy(task,
                            () -> TaskType.createTask(file.newID(), newQuest, task.getType().getTypeForNBT()),
                            FTBQuestsClient.holderLookup());
                    if (newTask != null) {
                        newTask.onCreated();
                    }
                });
                for (Reward reward : toCopy.getRewards()) {
                    Reward newReward = QuestObjectBase.copy(reward,
                            () -> RewardType.createReward(file.newID(), newQuest, reward.getType().getTypeForNBT()),
                            FTBQuestsClient.holderLookup());
                    if (newReward != null) {
                        newReward.onCreated();
                    }
                }

                // sync new objects to clients
                MinecraftServer server = context.getPlayer().getServer();
                NetworkHelper.sendToAll(server, CreateObjectResponseMessage.create(newQuest, null));
                newQuest.getTasks().forEach(task -> {
                    CompoundTag extra = new CompoundTag();
                    extra.putString("type", task.getType().getTypeForNBT());
                    NetworkHelper.sendToAll(server, CreateObjectResponseMessage.create(task, extra));
                });
                newQuest.getRewards().forEach(reward -> {
                    CompoundTag extra = new CompoundTag();
                    extra.putString("type", reward.getType().getTypeForNBT());
                    NetworkHelper.sendToAll(server, CreateObjectResponseMessage.create(reward, extra));
                });

                // and update the server quest map etc.
                ServerQuestFile.INSTANCE.refreshIDMap();
                ServerQuestFile.INSTANCE.clearCachedData();
                ServerQuestFile.INSTANCE.markDirty();
            }
        });
    }
}
