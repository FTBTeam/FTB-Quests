package dev.ftb.mods.ftbquests.net;

import dev.architectury.networking.NetworkManager;
import dev.ftb.mods.ftblibrary.util.NetworkHelper;
import dev.ftb.mods.ftbquests.api.FTBQuestsAPI;
import dev.ftb.mods.ftbquests.quest.QuestObjectType;
import dev.ftb.mods.ftbquests.quest.ServerQuestFile;
import dev.ftb.mods.ftbquests.quest.history.events.MoveItemWithinQuest;
import dev.ftb.mods.ftbquests.util.NetUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/**
 * Sent by client to move a task or reward within its quest. Task/reward ordering is shown in the quest view panel.
 * @param taskOrRewardId ID of the task or reward
 * @param questObjectType the object type (must be TASK or REWARD)
 * @param moveRight true to move right, false to move left
 */
public record ReorderItemMessage(long taskOrRewardId, QuestObjectType questObjectType, boolean moveRight) implements CustomPacketPayload {
    public static final Type<ReorderItemMessage> TYPE = new Type<>(FTBQuestsAPI.rl("reorder_item"));
    public static final StreamCodec<FriendlyByteBuf, ReorderItemMessage> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_LONG, ReorderItemMessage::taskOrRewardId,
            NetworkHelper.enumStreamCodec(QuestObjectType.class), ReorderItemMessage::questObjectType,
            ByteBufCodecs.BOOL, ReorderItemMessage::moveRight,
            ReorderItemMessage::new
    );

    public static void handle(ReorderItemMessage message, NetworkManager.PacketContext context) {
        context.queue(() -> ServerQuestFile.getInstance().ifPresent(sqf -> {
            if (NetUtils.canEdit(context)) {
                sqf.getHistoryStack().addAndApply(sqf, new MoveItemWithinQuest(message.taskOrRewardId, message.questObjectType, message.moveRight));
            }
        }));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
