package dev.ftb.mods.ftbquests.client;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

import dev.ftb.mods.ftblibrary.client.util.ClientUtils;
import dev.ftb.mods.ftblibrary.icon.Icon;
import dev.ftb.mods.ftblibrary.util.NameMap;
import dev.ftb.mods.ftbquests.quest.BaseQuestFile;
import dev.ftb.mods.ftbquests.quest.Chapter;
import dev.ftb.mods.ftbquests.quest.QuestObject;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public enum NotificationStyle {
    TOAST("toast", NotificationStyle::completionToast, NotificationStyle::rewardToast),
    CHAT("chat", NotificationStyle::completionChat, NotificationStyle::rewardChat),
    ACTION_BAR("action_bar", NotificationStyle::completionActionBar, NotificationStyle::rewardActionBar),
    NONE("none", NotificationStyle::completionNone, NotificationStyle::rewardNone);

    public static final NotificationStyle[] VALUES = values();

    public static final NameMap<NotificationStyle> NAME_MAP = NameMap.of(TOAST, VALUES)
            .id(v -> v.id)
            .name(n -> Component.translatable("ftbquests.ui.notification." + n.id))
            .create();

    private final String id;
    private final Consumer<QuestObject> onComplete;
    private final BiConsumer<Component,Icon<?>> onReward;

    NotificationStyle(String id, Consumer<QuestObject> onComplete, BiConsumer<Component,Icon<?>> onReward) {
        this.id = id;
        this.onComplete = onComplete;
        this.onReward = onReward;
    }
    public boolean notifyCompletion(long id) {
        if (ClientQuestFile.exists()) {
            QuestObject object = ClientQuestFile.getInstance().get(id);
            if (object != null) {
                onComplete.accept(object);
                return true;
            }
        }
        return false;
    }

    public void notifyReward(Component text, Icon<?> icon) {
        onReward.accept(text, icon);
    }

    private static void completionToast(QuestObject qo) {
        FTBQuestsClient.showCompletionToast(qo);
    }

    private static void rewardToast(Component text, Icon<?> icon) {
        FTBQuestsClient.showRewardToast(text, icon);
    }

    private static void completionChat(QuestObject qo) {
        chatMsg(qo, false);
    }

    private static void rewardChat(Component component, Icon<?> icon) {
        ClientUtils.getClientPlayer().displayClientMessage(formatRewardMsg(component),false);
    }

    private static void completionActionBar(QuestObject qo) {
        chatMsg(qo, true);
    }

    private static void rewardActionBar(Component component, Icon<?> icon) {
        ClientUtils.getClientPlayer().displayClientMessage(formatRewardMsg(component), true);
    }

    private static void completionNone(QuestObject qo) {
    }

    private static void rewardNone(Component text, Icon<?> icon) {
    }

    private static Component formatRewardMsg(Component msg) {
        return Component.translatable("ftbquests.reward.collected").withStyle(QuestObjectType.REWARD.getColor())
                .append(" ")
                .append(msg.copy().withStyle(ChatFormatting.WHITE));
    }

    private static void chatMsg(QuestObject qo, boolean actionBar) {
        Player player = ClientUtils.getClientPlayer();
        MutableComponent msg = qo.getObjectType().getCompletedMessage().copy().withStyle(qo.getObjectType().getColor());
        player.displayClientMessage(msg.append(" ").append(qo.getTitle().copy().withStyle(ChatFormatting.WHITE)), actionBar);
        if (FTBQuestsClientConfig.COMPLETION_SOUNDS.get()) {
            if (qo instanceof Chapter || qo instanceof BaseQuestFile) {
                player.playSound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE);
            } else {
                player.playSound(SoundEvents.UI_TOAST_OUT);
            }
        }
    }
}
