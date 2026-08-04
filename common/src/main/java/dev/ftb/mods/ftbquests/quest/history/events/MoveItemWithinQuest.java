package dev.ftb.mods.ftbquests.quest.history.events;

import dev.ftb.mods.ftblibrary.platform.network.Server2PlayNetworking;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.net.ReorderItemResponseMessage;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.QuestObjectBase;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.ChangeType;
import dev.ftb.mods.ftbquests.quest.history.QuestBookEditEvent;
import dev.ftb.mods.ftbquests.quest.reward.Reward;
import dev.ftb.mods.ftbquests.quest.task.Task;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

public record MoveItemWithinQuest(long taskOrRewardId, QuestObjectType questObjectType, boolean moveRight) implements QuestBookEditEvent {
    @Override
    public void apply(ServerQuestFile file) {
        applyChange(file, moveRight);
    }

    @Override
    public void applyUndo(ServerQuestFile file) {
        applyChange(file, !moveRight);
    }

    @Override
    public List<Component> description(BaseQuestFile file, ChangeType changeType) {
        return List.of(Component.translatable("ftbquests.event.move_object",
                QuestObjectType.NAME_MAP.getDisplayName(questObjectType).copy().withStyle(ChatFormatting.AQUA),
                getTitle(file, taskOrRewardId),
                moveRight ? "▶" : "◀")
        );
    }

    private void applyChange(ServerQuestFile file, boolean dir) {
        QuestObjectBase object = file.getBase(taskOrRewardId);
        if (object instanceof Task task) {
            task.getQuest().moveTask(task, dir);
            Server2PlayNetworking.sendToAllPlayers(file.server, ReorderItemResponseMessage.tasks(task.getQuest()));
        } else if (object instanceof Reward reward) {
            reward.getQuest().moveReward(reward, dir);
            Server2PlayNetworking.sendToAllPlayers(file.server, ReorderItemResponseMessage.rewards(reward.getQuest()));
        }
    }
}
