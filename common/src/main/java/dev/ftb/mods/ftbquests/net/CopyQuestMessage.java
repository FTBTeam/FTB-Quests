package dev.ftb.mods.ftbquests.net;

import de.marhali.json5.Json5Object;
import dev.ftb.mods.ftblibrary.json5.Json5Ops;
import dev.ftb.mods.ftblibrary.platform.network.PacketContext;
import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
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
        BaseQuestFile file = ServerQuestFile.getInstance();
        if (file.get(message.id) instanceof Quest toCopy && file.get(message.chapterId) instanceof Chapter chapter) {
            // deep copy of the quest
            Quest newQuest = Objects.requireNonNull(QuestObjectBase.copy(toCopy, () -> new Quest(file.newID(), chapter)));
            if (!message.copyDeps) {
                newQuest.clearDependencies();
            }
            newQuest.setX(message.qx);
            newQuest.setY(message.qy);
            newQuest.onCreated();

            // deep copy of all tasks and rewards
            toCopy.getTasks().forEach(task -> {
                Task newTask = QuestObjectBase.copy(task,
                        () -> TaskType.createTask(file.newID(), newQuest, task.getType().getTypeForSerialization()));
                newTask.onCreated();
            });
            for (Reward reward : toCopy.getRewards()) {
                Reward newReward = QuestObjectBase.copy(reward,
                        () -> RewardType.createReward(file.newID(), newQuest, reward.getType().getTypeForSerialization()));
                newReward.onCreated();
            }

            // sync new objects to clients
            MinecraftServer server = Objects.requireNonNull(context.player().level().getServer());
            Server2PlayNetworking.sendToAllPlayers(server, CreateObjectResponseMessage.create(newQuest, null));
            newQuest.getTasks().forEach(task -> {
                Json5Object extra = new Json5Object();
                extra.addProperty("type", task.getType().getTypeForSerialization());
                Server2PlayNetworking.sendToAllPlayers(server, CreateObjectResponseMessage.create(task, extra));
            });
            newQuest.getRewards().forEach(reward -> {
                Json5Object extra = new Json5Object();
                extra.addProperty("type", reward.getType().getTypeForSerialization());
                Server2PlayNetworking.sendToAllPlayers(server, CreateObjectResponseMessage.create(reward, extra));
            });

            // and update the server quest map etc.
            ServerQuestFile.getInstance().refreshIDMap();
            ServerQuestFile.getInstance().clearCachedData();
            ServerQuestFile.getInstance().markDirty();
        }
    }
}
